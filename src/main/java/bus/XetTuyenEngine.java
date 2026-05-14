package bus;

import dao.DiemCongDAO;
import dao.DiemThiDAO;
import dao.NganhDAO;
import dao.NganhToHopDAO;
import entity.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine tính điểm và xét trúng tuyển — tất cả static, độc lập với UI.
 *
 * Quy trình:
 *  1. Lấy DiemThiXetTuyen theo cccd
 *  2. Áp công thức ĐTHXT (hệ số từ NganhToHop)
 *  3. Áp doLech từ NganhToHop → ĐTHGXT
 *  4. Lấy DiemCong → ĐC
 *  5. Tính ĐƯT từ khuVuc + doiTuong của ThiSinh
 *  6. ĐXT = ĐTHGXT + ĐC + ĐƯT
 *  7. Xét trúng tuyển theo chiTieu từng ngành
 */
public class XetTuyenEngine {

    // ── Mã kết quả ────────────────────────────────────────────────────
    public static final String KQ_TRUNG_TUYEN       = "TRUNG_TUYEN";
    public static final String KQ_KHONG_XET         = "KHONG_XET";
    public static final String KQ_TRUOT_NV          = "TRUOT_NV";
    public static final String KQ_TRUOT_NGANH       = "TRUOT_NGANH";
    public static final String KQ_CHUA_XET          = "CHUA_XET";

    // ── Điểm ưu tiên khu vực ─────────────────────────────────────────
    private static double diemUuTienKhuVuc(String khuVuc) {
        if (khuVuc == null) return 0;
        return switch (khuVuc.trim().toUpperCase()) {
            case "1", "KV1"           -> 0.75;
            case "2NT", "KV2NT"       -> 0.50;
            case "2", "KV2"           -> 0.25;
            default                   -> 0.0;
        };
    }

    // ── Điểm ưu tiên đối tượng ───────────────────────────────────────
    private static double diemUuTienDoiTuong(String doiTuong) {
        if (doiTuong == null) return 0;
        return switch (doiTuong.trim()) {
            case "01", "02"                     -> 2.0;
            case "03", "04", "05", "06", "07"  -> 1.0;
            default                             -> 0.0;
        };
    }

    /**
     * Tính tổng điểm ưu tiên (ĐƯT = điểm khu vực + điểm đối tượng).
     * Đã capped ở mức 3.0 theo quy định.
     */
    public static double tinhUuTien(ThiSinh ts) {
        if (ts == null) return 0;
        double kv = diemUuTienKhuVuc(ts.getKhuVuc());
        double dt = diemUuTienDoiTuong(ts.getDoiTuong());
        return Math.min(kv + dt, 3.0);
    }

    /**
     * Tính ĐTHXT theo công thức: [(d1*w1 + d2*w2 + d3*w3) / W] * 3
     * Dùng hệ số từ NganhToHop (TO, LI, HO, SI, VA, SU, DI, TI, N1, KTPL).
     * Trả về 0 nếu không đủ dữ liệu.
     */
    public static double tinhDTHXT(DiemThiXetTuyen dt, NganhToHop nth) {
        if (dt == null || nth == null) return 0;

        // Map (hệ số, điểm) cho tất cả môn có hệ số > 0
        double sumWeighted = 0;
        int    totalWeight = 0;

        sumWeighted += safeWD(nth.getTo(),   dt.getDiemToan());
        totalWeight += safeW(nth.getTo());
        sumWeighted += safeWD(nth.getLi(),   dt.getDiemLy());
        totalWeight += safeW(nth.getLi());
        sumWeighted += safeWD(nth.getHo(),   dt.getDiemHoa());
        totalWeight += safeW(nth.getHo());
        sumWeighted += safeWD(nth.getSi(),   dt.getDiemSinh());
        totalWeight += safeW(nth.getSi());
        sumWeighted += safeWD(nth.getVa(),   dt.getDiemVan());
        totalWeight += safeW(nth.getVa());
        sumWeighted += safeWD(nth.getSu(),   dt.getDiemSu());
        totalWeight += safeW(nth.getSu());
        sumWeighted += safeWD(nth.getDi(),   dt.getDiemDia());
        totalWeight += safeW(nth.getDi());
        sumWeighted += safeWD(nth.getTi(),   dt.getDiemTiengAnh());
        totalWeight += safeW(nth.getTi());
        // N1: ưu tiên điểm thi, nếu không có dùng điểm chứng chỉ
        Double n1Score = dt.getN1Thi() != null ? dt.getN1Thi() : dt.getN1Cc();
        sumWeighted += safeWD(nth.getN1(), n1Score);
        totalWeight += safeW(nth.getN1());
        sumWeighted += safeWD(nth.getKtpl(), dt.getDiemKtpl());
        totalWeight += safeW(nth.getKtpl());

        if (totalWeight == 0) return 0;
        // Quy chuẩn về thang 30 (3 môn hệ số 1)
        return (sumWeighted / totalWeight) * 3.0;
    }

    private static double safeWD(Integer w, Double d) {
        if (w == null || w <= 0 || d == null) return 0;
        return w * d;
    }
    private static int safeW(Integer w) {
        return (w != null && w > 0) ? w : 0;
    }

    /**
     * Quy đổi điểm V-SAT / ĐGNL về thang THPT bằng nội suy tuyến tính.
     * Dùng 4 điểm neo (A, B, C, D) trong BangQuyDoi để xác định THPT tương đương.
     *
     * THPT tương đương cố định tại các mốc: A→5.0, B→6.5, C→7.5, D→9.0
     * (Admin cần nhập đúng giá trị diemA/B/C/D trong BangQuyDoi để khớp tài liệu chính thức)
     */
    public static double quyDoiVeSAThoTP(double score, BangQuyDoi bqd) {
        if (bqd == null) return score; // không có dữ liệu → dùng nguyên điểm
        Double a = bqd.getDiemA(), b = bqd.getDiemB(), c = bqd.getDiemC(), d = bqd.getDiemD();
        if (a == null || b == null || c == null || d == null) return score;

        // Các mốc THPT tương đương (cố định theo quy định)
        final double TA = 5.0, TB = 6.5, TC = 7.5, TD = 9.0;
        final double T_MIN = 0.0, T_MAX = 10.0;

        if (score <= a) return lerp(0, a, T_MIN, TA, score);
        if (score <= b) return lerp(a, b, TA, TB, score);
        if (score <= c) return lerp(b, c, TB, TC, score);
        if (score <= d) return lerp(c, d, TC, TD, score);
        return lerp(d, d + (d - c), TD, T_MAX, score); // extrapolate above D
    }

    /** Nội suy tuyến tính: tại x trong [x1,x2] → y trong [y1,y2] */
    private static double lerp(double x1, double x2, double y1, double y2, double x) {
        if (x2 == x1) return y1;
        double t = (x - x1) / (x2 - x1);
        t = Math.max(0, Math.min(1, t));
        return y1 + t * (y2 - y1);
    }

    // ── KẾT QUẢ TÍNH TOÁN MỘT NGUYỆN VỌNG ──────────────────────────
    public record KetQuaTinh(double dthxt, double dthgxt, double diemCong,
                             double uuTien, double dxt) {}

    /**
     * Tính đầy đủ điểm cho một nguyện vọng.
     * Trả về null nếu thiếu dữ liệu cơ bản (không có điểm thi hoặc NganhToHop).
     */
    public static KetQuaTinh tinhDXT(NguyenVongXetTuyen nv,
                                     DiemThiXetTuyen    diemThi,
                                     NganhToHop         nth,
                                     DiemCongXetTuyen   diemCong,
                                     ThiSinh            thiSinh,
                                     List<BangQuyDoi>   bangQuyDoiList) {
        if (diemThi == null || nth == null) return null;

        String pt = nv.getPhuongThuc() != null ? nv.getPhuongThuc() : diemThi.getPhuongThuc();

        DiemThiXetTuyen diemTinhToan = diemThi;

        // Nếu V-SAT hoặc ĐGNL: chuyển đổi từng điểm về thang THPT trước khi tính ĐTHXT
        if ("VSAT".equals(pt) || "DGNL".equals(pt)) {
            diemTinhToan = quyDoiDiemVeSAT(diemThi, nv.getToHopMon(), pt, bangQuyDoiList);
        }

        double dthxt  = tinhDTHXT(diemTinhToan, nth);
        // doLech từ NganhToHop = độ điều chỉnh giữa tổ hợp thi và tổ hợp gốc của ngành
        double doLech = nth.getDoLech() != null ? nth.getDoLech() : 0.0;
        double dthgxt = dthxt + doLech;

        double dc     = diemCong != null && diemCong.getDiemTong() != null ? diemCong.getDiemTong() : 0.0;
        double uuTien = tinhUuTien(thiSinh);
        double dxt    = dthgxt + dc + uuTien;

        return new KetQuaTinh(dthxt, dthgxt, dc, uuTien, dxt);
    }

    /** Tạo bản sao DiemThiXetTuyen với điểm đã quy đổi về THPT */
    private static DiemThiXetTuyen quyDoiDiemVeSAT(DiemThiXetTuyen dt, String toHopMon,
                                                    String pt, List<BangQuyDoi> bqdList) {
        DiemThiXetTuyen converted = new DiemThiXetTuyen();
        converted.setIdDiemThi(dt.getIdDiemThi());
        converted.setCccd(dt.getCccd());
        converted.setPhuongThuc("THPT_QD"); // đã quy đổi

        // Chuyển đổi từng môn dùng BangQuyDoi tương ứng
        converted.setDiemToan(    convertMon(dt.getDiemToan(),     "Toan",     toHopMon, pt, bqdList));
        converted.setDiemLy(      convertMon(dt.getDiemLy(),       "Ly",       toHopMon, pt, bqdList));
        converted.setDiemHoa(     convertMon(dt.getDiemHoa(),      "Hoa",      toHopMon, pt, bqdList));
        converted.setDiemSinh(    convertMon(dt.getDiemSinh(),     "Sinh",     toHopMon, pt, bqdList));
        converted.setDiemVan(     convertMon(dt.getDiemVan(),      "Van",      toHopMon, pt, bqdList));
        converted.setDiemSu(      convertMon(dt.getDiemSu(),       "Su",       toHopMon, pt, bqdList));
        converted.setDiemDia(     convertMon(dt.getDiemDia(),      "Dia",      toHopMon, pt, bqdList));
        converted.setDiemTiengAnh(convertMon(dt.getDiemTiengAnh(),"TiengAnh", toHopMon, pt, bqdList));
        converted.setN1Thi(       convertMon(dt.getN1Thi(),        "N1",       toHopMon, pt, bqdList));
        converted.setDiemKtpl(    convertMon(dt.getDiemKtpl(),     "KTPL",     toHopMon, pt, bqdList));
        converted.setNl1(dt.getNl1()); converted.setNk1(dt.getNk1()); converted.setNk2(dt.getNk2());
        converted.setCncn(dt.getCncn()); converted.setCnnn(dt.getCnnn());
        return converted;
    }

    private static Double convertMon(Double rawScore, String monName, String toHop,
                                     String pt, List<BangQuyDoi> bqdList) {
        if (rawScore == null) return null;
        BangQuyDoi bqd = bqdList.stream()
                .filter(b -> pt.equals(b.getPhuongThuc())
                          && monName.equalsIgnoreCase(b.getMon())
                          && (toHop == null || toHop.equals(b.getToHop()) || b.getToHop() == null))
                .findFirst()
                // fallback: chỉ khớp PT + môn
                .or(() -> bqdList.stream()
                        .filter(b -> pt.equals(b.getPhuongThuc()) && monName.equalsIgnoreCase(b.getMon()))
                        .findFirst())
                .orElse(null);
        return quyDoiVeSAThoTP(rawScore, bqd);
    }

    // ── CHẠY XÉT TUYỂN TOÀN BỘ ──────────────────────────────────────

    public interface ProgressCallback { void update(int done, int total); }

    /**
     * Chạy toàn bộ quy trình xét tuyển.
     * Trả về danh sách NguyenVong đã được cập nhật điểm và kết quả (chưa lưu vào DB).
     */
    public static List<NguyenVongXetTuyen> runAll(
            List<NguyenVongXetTuyen> allNV,
            Map<String, DiemThiXetTuyen>  diemThiMap,      // key = cccd
            Map<String, NganhToHop>       nganhToHopMap,   // key = maNganh|maToHop
            Map<String, DiemCongXetTuyen> diemCongMap,     // key = cccd|maNganh|maToHop|pt
            Map<String, ThiSinh>          thiSinhMap,       // key = cccd
            Map<String, Nganh>            nganhMap,         // key = maNganh
            List<BangQuyDoi>              bangQuyDoiList,
            ProgressCallback              callback) {

        int total = allNV.size(), done = 0;

        // ─ Bước 1: Tính điểm từng nguyện vọng ─
        for (NguyenVongXetTuyen nv : allNV) {
            DiemThiXetTuyen  dt  = diemThiMap.get(nv.getCccd());
            NganhToHop       nth = nganhToHopMap.get(nv.getMaNganh() + "|" + nv.getToHopMon());
            ThiSinh          ts  = thiSinhMap.get(nv.getCccd());
            String dcKey = nv.getCccd() + "|" + nv.getMaNganh() + "|"
                         + nvStr(nv.getToHopMon()) + "|" + nvStr(nv.getPhuongThuc());
            DiemCongXetTuyen dc  = diemCongMap.get(dcKey);

            nv.setKetQua(KQ_CHUA_XET);
            KetQuaTinh kq = tinhDXT(nv, dt, nth, dc, ts, bangQuyDoiList);
            if (kq != null) {
                nv.setDiemThxt(round2(kq.dthgxt()));      // lưu ĐTHGXT
                nv.setDiemCong(round2(kq.diemCong()));
                nv.setDiemUtqd(round2(kq.uuTien()));
                nv.setDiemXetTuyen(round2(kq.dxt()));
            }

            done++;
            if (callback != null) callback.update(done, total);
        }

        // ─ Bước 2: Xác định trúng/rớt ─
        // Group theo ngành → sort ĐXT giảm → đánh dấu top N = DAU (internal)
        Map<String, List<NguyenVongXetTuyen>> byNganh = allNV.stream()
                .collect(Collectors.groupingBy(nv -> nv.getMaNganh() != null ? nv.getMaNganh() : ""));

        // Tập NV được đánh dấu "đậu" ở cấp ngành
        Set<Integer> dauSet = new HashSet<>();
        for (Map.Entry<String, List<NguyenVongXetTuyen>> entry : byNganh.entrySet()) {
            String maNganh = entry.getKey();
            Nganh  nganh   = nganhMap.get(maNganh);
            int chiTieu = nganh != null ? nganh.getChiTieu() : 0;
            double diemSan = nganh != null && nganh.getDiemSan() != null ? nganh.getDiemSan() : 0;

            List<NguyenVongXetTuyen> sorted = entry.getValue().stream()
                    .filter(nv -> nv.getDiemXetTuyen() != null)
                    .sorted(Comparator.comparingDouble(NguyenVongXetTuyen::getDiemXetTuyen).reversed())
                    .toList();

            int count = 0;
            for (NguyenVongXetTuyen nv : sorted) {
                if (count < chiTieu && nv.getDiemXetTuyen() >= diemSan) {
                    dauSet.add(nv.getIdNv());
                    count++;
                }
            }
        }

        // ─ Bước 3: Xác định kết quả cuối per thí sinh ─
        Map<String, List<NguyenVongXetTuyen>> byThiSinh = allNV.stream()
                .collect(Collectors.groupingBy(nv -> nv.getCccd() != null ? nv.getCccd() : ""));

        for (List<NguyenVongXetTuyen> nvList : byThiSinh.values()) {
            // Sắp xếp theo thứ tự nguyện vọng tăng dần
            List<NguyenVongXetTuyen> sorted = nvList.stream()
                    .sorted(Comparator.comparingInt(NguyenVongXetTuyen::getThuTuNguyenVong))
                    .toList();

            // Tìm NV đầu tiên có "đậu" ở cấp ngành
            int trungTuyenTT = Integer.MAX_VALUE;
            for (NguyenVongXetTuyen nv : sorted) {
                if (dauSet.contains(nv.getIdNv())) {
                    trungTuyenTT = nv.getThuTuNguyenVong();
                    break;
                }
            }

            for (NguyenVongXetTuyen nv : sorted) {
                int tt = nv.getThuTuNguyenVong();
                if (tt == trungTuyenTT && dauSet.contains(nv.getIdNv())) {
                    nv.setKetQua(KQ_TRUNG_TUYEN);
                } else if (tt < trungTuyenTT) {
                    // NV thứ tự thấp hơn mà không trúng → trượt NV đó
                    nv.setKetQua(dauSet.contains(nv.getIdNv()) ? KQ_TRUOT_NGANH : KQ_TRUOT_NV);
                } else if (tt > trungTuyenTT) {
                    nv.setKetQua(KQ_KHONG_XET);
                } else {
                    nv.setKetQua(dauSet.contains(nv.getIdNv()) ? KQ_TRUOT_NGANH : KQ_TRUOT_NV);
                }
            }
        }

        return allNV;
    }

    private static String nvStr(String s) { return s != null ? s : ""; }
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; }
}
