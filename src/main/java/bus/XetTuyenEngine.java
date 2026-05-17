package bus;

import entity.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Engine tính điểm và xét trúng tuyển — tất cả static, độc lập với UI.
 * <p>
 * Quy trình 7 bước:
 * 1. Lấy nguyện vọng (batch)
 * 2. Lấy điểm thi (1 TS có thể nhiều PT: 2=ĐGNL, 3=VSAT, 4=THPT)
 * 3. Lấy tổ hợp môn theo mã ngành
 * 4. Tính điểm tổ hợp thang 30 (ĐGNL quy đổi a,b,c,d; THPT/VSAT = Σ(điểm×hệ số))
 * 5. Điểm cộng + điểm ưu tiên (giảm ĐƯT khi vượt ngưỡng 22.5)
 * 6. ĐXT = ĐTHXT + ĐC + ĐƯT, chỉ giữ max per (cccd, maNganh)
 * 7. Xét trúng tuyển theo chỉ tiêu + điểm sàn, ưu tiên thứ tự NV
 */
public class XetTuyenEngine {

    // ── Mã kết quả ────────────────────────────────────────────────────
    public static final String KQ_TRUNG_TUYEN = "TRUNG_TUYEN";
    public static final String KQ_TRUOT_NV = "TRUOT_NV";
    public static final String KQ_DUOI_SAN = "DUOI_SAN";
    public static final String KQ_TRUOT_NGANH = "TRUOT_NGANH";
    public static final String KQ_KHONG_XET = "KHONG_XET";
    public static final String KQ_CHUA_XET = "CHUA_XET";

    // ── Ngưỡng quy đổi điểm ưu tiên ──────────────────────────────────
    private static final double NGUONG_UU_TIEN = 22.5;

    // ── Điểm ưu tiên khu vực ─────────────────────────────────────────
    private static double diemUuTienKhuVuc(String khuVuc) {
        if (khuVuc == null) return 0;
        return switch (khuVuc.trim().toUpperCase()) {
            case "1", "KV1" -> 0.75;
            case "2NT", "KV2NT" -> 0.50;
            case "2", "KV2" -> 0.25;
            default -> 0.0;
        };
    }

    // ── Điểm ưu tiên đối tượng ───────────────────────────────────────
    private static double diemUuTienDoiTuong(String doiTuong) {
        if (doiTuong == null) return 0;
        return switch (doiTuong.trim()) {
            case "01", "02" -> 2.0;
            case "03", "04", "05", "06", "07" -> 1.0;
            default -> 0.0;
        };
    }

    /**
     * Tính mức điểm ưu tiên gốc (MĐƯT = khu vực + đối tượng).
     */
    public static double tinhMucUuTien(ThiSinh ts) {
        if (ts == null) return 0;
        double kv = diemUuTienKhuVuc(ts.getKhuVuc());
        double dt = diemUuTienDoiTuong(ts.getDoiTuong());
        return kv + dt;
    }

    /**
     * Tính điểm ưu tiên thực tế sau khi áp dụng quy đổi theo ngưỡng.
     * Nếu ĐTHXT + ĐC > 22.5 → ĐƯT = [(30 - ĐTHXT - ĐC) / 7.5] × MĐƯT
     * Nếu không → ĐƯT = MĐƯT
     */
    public static double tinhUuTien(ThiSinh ts, double diemToHop, double diemCong) {
        double mdut = tinhMucUuTien(ts);
        if (mdut == 0) return 0;

        double tongTruocUT = diemToHop + diemCong;
        if (tongTruocUT > NGUONG_UU_TIEN) {
            double dut = ((30.0 - tongTruocUT) / 7.5) * mdut;
            return Math.max(0, round2(dut));
        }
        return mdut;
    }

    // ── TÍNH ĐIỂM TỔ HỢP THPT / VSAT (thang 30) ─────────────────────
    /**
     * Tính ĐTHXT cho THPT và VSAT: Σ(điểm_môn × hệ_số) / Σ(hệ_số) × 3
     */
    public static double tinhDTHXT_THPT(DiemThiXetTuyen dt, NganhToHop nth) {
        if (dt == null || nth == null) return 0;

        double sumWeighted = 0;
        int totalWeight = 0;

        // Các cột hệ số cố định trong NganhToHop
        sumWeighted += safeWD(nth.getTo(), dt.getDiemToan());
        totalWeight += safeW(nth.getTo());
        sumWeighted += safeWD(nth.getLi(), dt.getDiemLy());
        totalWeight += safeW(nth.getLi());
        sumWeighted += safeWD(nth.getHo(), dt.getDiemHoa());
        totalWeight += safeW(nth.getHo());
        sumWeighted += safeWD(nth.getSi(), dt.getDiemSinh());
        totalWeight += safeW(nth.getSi());
        sumWeighted += safeWD(nth.getVa(), dt.getDiemVan());
        totalWeight += safeW(nth.getVa());
        sumWeighted += safeWD(nth.getSu(), dt.getDiemSu());
        totalWeight += safeW(nth.getSu());
        sumWeighted += safeWD(nth.getDi(), dt.getDiemDia());
        totalWeight += safeW(nth.getDi());
        sumWeighted += safeWD(nth.getTi(), dt.getDiemTiengAnh());
        totalWeight += safeW(nth.getTi());
        sumWeighted += safeWD(nth.getKtpl(), dt.getDiemKtpl());
        totalWeight += safeW(nth.getKtpl());

        // N1: lấy điểm CAO HƠN nếu cả 2 không null
        Double n1Score = maxNullable(dt.getN1Thi(), dt.getN1Cc());
        sumWeighted += safeWD(nth.getN1(), n1Score);
        totalWeight += safeW(nth.getN1());

        // KHAC: ánh xạ qua thMon1/thMon2/thMon3 cho GDCD, NK1-NK10, CNCN, CNNN...
        if (nth.getKhac() != null && nth.getKhac() > 0) {
            // Tìm môn nào trong thMon1/2/3 không phải môn chuẩn → đó là môn KHAC
            Double khacScore = findKhacScore(dt, nth);
            sumWeighted += safeWD(nth.getKhac(), khacScore);
            totalWeight += safeW(nth.getKhac());
        }

        if (totalWeight == 0) return 0;
        return (sumWeighted / totalWeight) * 3.0;
    }

    /**
     * Lấy điểm cao hơn giữa 2 giá trị nullable.
     */
    private static Double maxNullable(Double a, Double b) {
        if (a == null) return b;
        if (b == null) return a;
        return Math.max(a, b);
    }

    /**
     * Tìm điểm cho môn KHAC dựa trên thMon1/thMon2/thMon3 trong NganhToHop.
     * Các môn chuẩn (TO, LI, HO...) đã có cột riêng → tìm môn không thuộc nhóm chuẩn.
     */
    private static Double findKhacScore(DiemThiXetTuyen dt, NganhToHop nth) {
        String[] monFields = {nth.getThMon1(), nth.getThMon2(), nth.getThMon3()};
        java.util.Set<String> standardSubjects = java.util.Set.of(
                "TO", "LI", "HO", "SI", "VA", "SU", "DI", "TI", "N1", "KTPL");
        for (String mon : monFields) {
            if (mon != null && !standardSubjects.contains(mon.trim().toUpperCase())) {
                Double score = getScoreBySubject(dt, mon);
                if (score != null) return score;
            }
        }
        return null;
    }

    /**
     * Ánh xạ mã môn → cột điểm trong DiemThiXetTuyen.
     * Hỗ trợ tất cả các môn: GDCD, NK1-NK10, CNCN, CNNN, NL1, v.v.
     */
    private static Double getScoreBySubject(DiemThiXetTuyen dt, String subject) {
        if (subject == null || dt == null) return null;
        return switch (subject.trim().toUpperCase()) {
            case "TO" -> dt.getDiemToan();
            case "LI" -> dt.getDiemLy();
            case "HO" -> dt.getDiemHoa();
            case "SI" -> dt.getDiemSinh();
            case "VA" -> dt.getDiemVan();
            case "SU" -> dt.getDiemSu();
            case "DI" -> dt.getDiemDia();
            case "TI" -> dt.getDiemTiengAnh();
            case "GDCD" -> dt.getDiemGdcd();
            case "KTPL" -> dt.getDiemKtpl();
            case "N1" -> maxNullable(dt.getN1Thi(), dt.getN1Cc());
            case "NL1" -> dt.getNl1();
            case "CNCN" -> dt.getCncn();
            case "CNNN" -> dt.getCnnn();
            case "NK1" -> dt.getNk1();
            case "NK2" -> dt.getNk2();
            case "NK3" -> dt.getNk3();
            case "NK4" -> dt.getNk4();
            case "NK5" -> dt.getNk5();
            case "NK6" -> dt.getNk6();
            case "NK7" -> dt.getNk7();
            case "NK8" -> dt.getNk8();
            case "NK9" -> dt.getNk9();
            case "NK10" -> dt.getNk10();
            default -> null;
        };
    }

    // ── TÍNH ĐIỂM TỔ HỢP ĐGNL (thang 30) ────────────────────────────
    /**
     * Quy đổi điểm ĐGNL về thang 30 bằng bảng quy đổi.
     * Tìm dòng BangQuyDoi sao cho: phuongThuc + toHop khớp và diemA ≤ score ≤ diemB
     * Nội suy tuyến tính: result = diemC + (score - diemA)/(diemB - diemA) * (diemD - diemC)
     */
    public static double tinhDTHXT_DGNL(double diemDGNL, String maToHop,
                                         Map<String, List<BangQuyDoi>> bqdMap) {
        // Tìm BQD phù hợp: key = phuongThuc + "_" + toHop
        List<BangQuyDoi> candidates = bqdMap.get("DGNL_" + maToHop);
        if (candidates == null || candidates.isEmpty()) {
            // Fallback: tìm theo phuongThuc only
            candidates = bqdMap.get("DGNL_");
        }
        if (candidates == null || candidates.isEmpty()) return 0;

        // Tìm dòng có diemA <= diemDGNL <= diemB
        for (BangQuyDoi bqd : candidates) {
            Double a = bqd.getDiemA(), b = bqd.getDiemB();
            Double c = bqd.getDiemC(), d = bqd.getDiemD();
            if (a == null || b == null || c == null || d == null) continue;

            if (diemDGNL >= a && diemDGNL <= b) {
                // Nội suy tuyến tính
                if (b.equals(a)) return c;
                double ratio = (diemDGNL - a) / (b - a);
                return c + ratio * (d - c);
            }
        }

        // Nếu không tìm thấy khoảng phù hợp, thử extrapolate
        // Lấy dòng đầu (thấp nhất) hoặc cuối (cao nhất)
        BangQuyDoi first = candidates.get(0);
        BangQuyDoi last = candidates.get(candidates.size() - 1);
        if (diemDGNL < first.getDiemA() && first.getDiemC() != null) {
            return first.getDiemC(); // dưới min → lấy diemC min
        }
        if (diemDGNL > last.getDiemB() && last.getDiemD() != null) {
            return last.getDiemD(); // trên max → lấy diemD max
        }
        return 0;
    }

    /**
     * Lấy điểm ĐGNL tổng từ DiemThiXetTuyen.
     * ĐGNL lưu tổng điểm vào field diemToan (repurposed).
     */
    private static Double getDiemDGNL(DiemThiXetTuyen dt) {
        // Ưu tiên: diemToan (thường dùng lưu tổng điểm ĐGNL)
        if (dt.getNl1() != null) return dt.getNl1();
        return null;
    }

    private static double safeWD(Integer w, Double d) {
        if (w == null || w <= 0 || d == null) return 0;
        return w * d;
    }

    private static int safeW(Integer w) {
        return (w != null && w > 0) ? w : 0;
    }

    // ── KẾT QUẢ TÍNH TOÁN MỘT NGUYỆN VỌNG ──────────────────────────
    public record KetQuaTinh(double dthxt, double diemCong,
                             double uuTien, double dxt,
                             String phuongThuc, String toHopMon) {
    }

    // ── PROGRESS CALLBACK ────────────────────────────────────────────
    public interface ProgressCallback {
        void update(int done, int total, String phase);
    }

    // ── CHẠY XÉT TUYỂN TOÀN BỘ ──────────────────────────────────────
    /**
     * Chạy toàn bộ quy trình xét tuyển 7 bước.
     * Trả về danh sách NguyenVong đã được cập nhật điểm và kết quả.
     */
    public static List<NguyenVongXetTuyen> runAll(
            List<NguyenVongXetTuyen> allNV,
            Map<String, List<DiemThiXetTuyen>> diemThiMap,   // key = cccd → list điểm theo PT
            Map<String, List<NganhToHop>> nganhToHopMap,      // key = maNganh → list tổ hợp
            Map<String, DiemCongXetTuyen> diemCongMap,        // key = cccd_maNganh_maToHop
            Map<String, ThiSinh> thiSinhMap,                  // key = cccd
            Map<String, Nganh> nganhMap,                      // key = maNganh
            Map<String, List<BangQuyDoi>> bqdMap,             // key = phuongThuc_toHop
            ProgressCallback callback) {

        int total = allNV.size(), done = 0;

        // ─ Bước 1-6: Tính điểm cho từng NV, chọn tổ hợp + PT tốt nhất ─
        if (callback != null) callback.update(0, total, "Đang tính điểm...");

        for (NguyenVongXetTuyen nv : allNV) {
            nv.setKetQua(KQ_CHUA_XET);
            nv.setDiemThxt(null);
            nv.setDiemCong(null);
            nv.setDiemUtqd(null);
            nv.setDiemXetTuyen(null);

            String cccd = nv.getCccd();
            String maNganh = nv.getMaNganh();
            ThiSinh ts = thiSinhMap.get(cccd);

            // Lấy tất cả điểm thi của thí sinh này
            List<DiemThiXetTuyen> danhSachDiem = diemThiMap.get(cccd);
            if (danhSachDiem == null || danhSachDiem.isEmpty()) {
                done++;
                if (callback != null && done % 1000 == 0) callback.update(done, total, "Đang tính điểm...");
                continue;
            }

            // Lấy tất cả tổ hợp cho mã ngành
            List<NganhToHop> danhSachToHop = nganhToHopMap.get(maNganh);
            if (danhSachToHop == null || danhSachToHop.isEmpty()) {
                done++;
                if (callback != null && done % 1000 == 0) callback.update(done, total, "Đang tính điểm...");
                continue;
            }

            // Tìm tổ hợp × phương thức cho ĐXT cao nhất
            KetQuaTinh best = null;

            for (DiemThiXetTuyen diemThi : danhSachDiem) {
                String pt = diemThi.getPhuongThuc();
                if (pt == null) continue;

                for (NganhToHop nth : danhSachToHop) {
                    String maToHop = nth.getMaToHop();

                    // Bước 4: Tính ĐTHXT theo phương thức
                    double dthxt;
                    if ("2".equals(pt)) {
                        // ĐGNL: quy đổi bằng bảng quy đổi → thang 30
                        Double rawScore = getDiemDGNL(diemThi);
                        if (rawScore == null) continue;
                        dthxt = tinhDTHXT_DGNL(rawScore, maToHop, bqdMap);
                    } else {
                        // THPT (4) và VSAT (3): tính trực tiếp
                        dthxt = tinhDTHXT_THPT(diemThi, nth);
                    }

                    if (dthxt <= 0) continue;

                    // Bước 5: Điểm cộng (lookup bằng key cccd_manganh_tohop)
                    String dcKey = cccd + "_" + maNganh + "_" + maToHop;
                    DiemCongXetTuyen dc = diemCongMap.get(dcKey);
                    double diemCong = (dc != null && dc.getDiemTong() != null) ? dc.getDiemTong() : 0.0;

                    // Điểm ưu tiên (có quy đổi theo ngưỡng)
                    double uuTien = tinhUuTien(ts, dthxt, diemCong);

                    // Bước 6: Tính ĐXT
                    double dxt = dthxt + diemCong + uuTien;

                    if (best == null || dxt > best.dxt()) {
                        best = new KetQuaTinh(
                                round2(dthxt), round2(diemCong),
                                round2(uuTien), round2(dxt),
                                pt, maToHop);
                    }
                }
            }

            // Cập nhật NV với kết quả tốt nhất
            if (best != null) {
                nv.setDiemThxt(best.dthxt());
                nv.setDiemCong(best.diemCong());
                nv.setDiemUtqd(best.uuTien());
                nv.setDiemXetTuyen(best.dxt());
                nv.setPhuongThuc(best.phuongThuc());
                nv.setToHopMon(best.toHopMon());
            }

            done++;
            if (callback != null && done % 1000 == 0) callback.update(done, total, "Đang tính điểm...");
        }

        if (callback != null) callback.update(total, total, "Đang xét trúng tuyển...");

        // ─ Bước 7: Xét trúng tuyển ─
        xetTrungTuyen(allNV, nganhMap, callback);

        return allNV;
    }

    /**
     * Bước 7: Xét trúng tuyển theo chỉ tiêu + điểm sàn,
     * ưu tiên thứ tự nguyện vọng (nếu đậu NV trước thì không xét NV sau).
     * <p>
     * Dùng thuật toán lặp nhiều vòng (deferred acceptance):
     * - Vòng 1: xét tất cả NV, chọn top chỉ tiêu per ngành
     * - Thí sinh đậu NV ưu tiên cao hơn → giải phóng slot ở ngành NV thấp hơn
     * - Vòng tiếp: lấp slot trống bằng thí sinh tiếp theo đủ điều kiện
     * - Lặp đến khi ổn định (không còn thay đổi)
     */
    private static void xetTrungTuyen(List<NguyenVongXetTuyen> allNV,
                                       Map<String, Nganh> nganhMap,
                                       ProgressCallback callback) {
        // Chỉ xét NV có điểm
        List<NguyenVongXetTuyen> nvCoDiem = allNV.stream()
                .filter(nv -> nv.getDiemXetTuyen() != null)
                .toList();

        // Group theo ngành: maNganh → list NV sorted by ĐXT giảm dần
        Map<String, List<NguyenVongXetTuyen>> byNganh = new HashMap<>();
        for (NguyenVongXetTuyen nv : nvCoDiem) {
            String mn = nv.getMaNganh() != null ? nv.getMaNganh() : "";
            byNganh.computeIfAbsent(mn, k -> new ArrayList<>()).add(nv);
        }
        // Sort mỗi ngành theo ĐXT giảm dần
        for (List<NguyenVongXetTuyen> list : byNganh.values()) {
            list.sort(Comparator.comparingDouble(NguyenVongXetTuyen::getDiemXetTuyen).reversed());
        }

        // Group theo thí sinh: cccd → list NV sorted by thứ tự NV tăng dần
        Map<String, List<NguyenVongXetTuyen>> byThiSinh = new HashMap<>();
        for (NguyenVongXetTuyen nv : nvCoDiem) {
            String cccd = nv.getCccd() != null ? nv.getCccd() : "";
            byThiSinh.computeIfAbsent(cccd, k -> new ArrayList<>()).add(nv);
        }
        for (List<NguyenVongXetTuyen> list : byThiSinh.values()) {
            list.sort(Comparator.comparingInt(NguyenVongXetTuyen::getThuTuNguyenVong));
        }

        // ── Lặp nhiều vòng cho đến khi ổn định ──
        Set<String> daAdmit = new HashSet<>();    // cccd đã trúng tuyển
        Map<String, Integer> admitted = new HashMap<>(); // idNv → ngành đã admit
        int maxRounds = 100; // chống infinite loop

        for (int round = 0; round < maxRounds; round++) {
            boolean changed = false;

            // Bước A: Per ngành, chọn top chỉ tiêu (bỏ qua thí sinh đã trúng ngành khác)
            Set<Integer> dauSetRound = new HashSet<>();
            for (Map.Entry<String, List<NguyenVongXetTuyen>> entry : byNganh.entrySet()) {
                String maNganh = entry.getKey();
                Nganh nganh = nganhMap.get(maNganh);
                int chiTieu = nganh != null ? nganh.getChiTieu() : 0;
                double diemSan = nganh != null && nganh.getDiemSan() != null ? nganh.getDiemSan() : 0;

                int count = 0;
                for (NguyenVongXetTuyen nv : entry.getValue()) {
                    if (count >= chiTieu) break;
                    if (nv.getDiemXetTuyen() < diemSan) continue;
                    // Bỏ qua thí sinh đã trúng tuyển ở ngành KHÁC
                    String cccd = nv.getCccd();
                    if (daAdmit.contains(cccd)) {
                        Integer admittedId = admitted.get(cccd);
                        if (admittedId != null && admittedId != nv.getIdNv()) continue;
                    }
                    dauSetRound.add(nv.getIdNv());
                    count++;
                }
            }

            // Bước B: Per thí sinh, chọn NV ưu tiên cao nhất trong dauSetRound
            daAdmit.clear();
            admitted.clear();

            for (Map.Entry<String, List<NguyenVongXetTuyen>> entry : byThiSinh.entrySet()) {
                for (NguyenVongXetTuyen nv : entry.getValue()) {
                    if (dauSetRound.contains(nv.getIdNv())) {
                        daAdmit.add(nv.getCccd());
                        admitted.put(nv.getCccd(), nv.getIdNv());
                        break; // chỉ lấy NV đầu tiên (ưu tiên cao nhất)
                    }
                }
            }

            // Bước C: Kiểm tra ổn định
            if (round == 0) {
                changed = true; // vòng đầu luôn cần chạy tiếp
            } else {
                // Nếu dauSetRound không thay đổi so với vòng trước → ổn định
                // (thực tế: nếu không có thí sinh nào bị "giải phóng" slot → ổn định)
                // Ta dùng cách đơn giản: chạy thêm 1 vòng nữa để verify
                changed = (dauSetRound.size() != admitted.size() * 0 + dauSetRound.size()); // always check
            }

            // Nếu vòng 2+ mà kết quả giống vòng trước → break
            if (round > 0) {
                // Đếm số slot trống per ngành
                boolean hasVacancy = false;
                for (Map.Entry<String, List<NguyenVongXetTuyen>> entry : byNganh.entrySet()) {
                    Nganh nganh = nganhMap.get(entry.getKey());
                    int chiTieu = nganh != null ? nganh.getChiTieu() : 0;
                    long admittedCount = entry.getValue().stream()
                            .filter(nv -> dauSetRound.contains(nv.getIdNv()) && admitted.containsValue(nv.getIdNv()))
                            .count();
                    long eligibleNotAdmitted = entry.getValue().stream()
                            .filter(nv -> !daAdmit.contains(nv.getCccd())
                                    && nv.getDiemXetTuyen() >= (nganh != null && nganh.getDiemSan() != null ? nganh.getDiemSan() : 0))
                            .count();
                    if (admittedCount < chiTieu && eligibleNotAdmitted > 0) {
                        hasVacancy = true;
                        break;
                    }
                }
                if (!hasVacancy) break;
            }
        }

        // ── Gán kết quả cuối cùng ──
        Set<Integer> finalAdmitIds = new HashSet<>(admitted.values());

        for (NguyenVongXetTuyen nv : allNV) {
            if (nv.getDiemXetTuyen() == null) {
                nv.setKetQua(KQ_CHUA_XET);
                continue;
            }

            if (finalAdmitIds.contains(nv.getIdNv())) {
                nv.setKetQua(KQ_TRUNG_TUYEN);
            } else if (daAdmit.contains(nv.getCccd())) {
                // Thí sinh đã trúng NV khác → NV này không xét
                Integer admittedNvId = admitted.get(nv.getCccd());
                if (admittedNvId != null) {
                    // Tìm thứ tự NV đã trúng
                    NguyenVongXetTuyen admittedNv = allNV.stream()
                            .filter(x -> x.getIdNv() == admittedNvId)
                            .findFirst().orElse(null);
                    if (admittedNv != null && nv.getThuTuNguyenVong() > admittedNv.getThuTuNguyenVong()) {
                        nv.setKetQua(KQ_KHONG_XET);
                    } else {
                        // NV trước NV trúng tuyển → trượt
                        Nganh ng = nganhMap.get(nv.getMaNganh());
                        double diemSan = ng != null && ng.getDiemSan() != null ? ng.getDiemSan() : 0;
                        nv.setKetQua(nv.getDiemXetTuyen() < diemSan ? KQ_DUOI_SAN : KQ_TRUOT_NGANH);
                    }
                } else {
                    nv.setKetQua(KQ_KHONG_XET);
                }
            } else {
                // Thí sinh không trúng NV nào
                Nganh ng = nganhMap.get(nv.getMaNganh());
                double diemSan = ng != null && ng.getDiemSan() != null ? ng.getDiemSan() : 0;
                nv.setKetQua(nv.getDiemXetTuyen() < diemSan ? KQ_DUOI_SAN : KQ_TRUOT_NGANH);
            }
        }
    }

    private static String nvStr(String s) {
        return s != null ? s : "";
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
