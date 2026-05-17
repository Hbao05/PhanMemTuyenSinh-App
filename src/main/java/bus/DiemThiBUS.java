package bus;

import dao.DiemThiDAO;
import dao.ThiSinhDAO;
import entity.DiemThiXetTuyen;

import java.util.*;
import java.util.function.Function;

public class DiemThiBUS {

    private static final int ROWS_PER_PAGE = 20;

    private final DiemThiDAO  dao         = new DiemThiDAO();
    private final ThiSinhDAO  thiSinhDAO  = new ThiSinhDAO();

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<DiemThiXetTuyen> getList(int page, String phuongThuc) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        if (phuongThuc == null || phuongThuc.isEmpty()) {
            return dao.getPaginatedList(offset, ROWS_PER_PAGE);
        }
        return dao.getPaginatedListByPhuongThuc(phuongThuc, offset, ROWS_PER_PAGE);
    }

    public int calculateTotalPages(String phuongThuc) {
        long total = (phuongThuc == null || phuongThuc.isEmpty())
                ? dao.countTotal() : dao.countByPhuongThuc(phuongThuc);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getTotalCount(String phuongThuc) {
        return (phuongThuc == null || phuongThuc.isEmpty())
                ? dao.countTotal() : dao.countByPhuongThuc(phuongThuc);
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<DiemThiXetTuyen> search(int page, String keyword, String phuongThuc) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.search(offset, ROWS_PER_PAGE, keyword, phuongThuc);
    }

    public int calculateSearchTotalPages(String keyword, String phuongThuc) {
        long total = dao.countSearch(keyword, phuongThuc);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getSearchCount(String keyword, String phuongThuc) {
        return dao.countSearch(keyword, phuongThuc);
    }

    // ── GET BY ID ─────────────────────────────────────────
    public DiemThiXetTuyen getById(int id) {
        return dao.getById(id);
    }

    // ── THÊM ─────────────────────────────────────────────
    public String addDiemThi(DiemThiXetTuyen dt) {
        String err = validate(dt, -1);
        if (err != null) return "Error: " + err;
        return dao.insert(dt) ? "Success" : "Error: Khong the them vao co so du lieu!";
    }

    // ── SỬA ──────────────────────────────────────────────
    public String updateDiemThi(DiemThiXetTuyen dt) {
        String err = validate(dt, dt.getIdDiemThi());
        if (err != null) return "Error: " + err;
        return dao.update(dt) ? "Success" : "Error: Khong the cap nhat co so du lieu!";
    }

    // ── XÓA ──────────────────────────────────────────────
    public String deleteDiemThi(int id) {
        return dao.delete(id) ? "Success" : "Error: Khong the xoa ban ghi nay!";
    }

    // ── VALIDATION ────────────────────────────────────────
    private String validate(DiemThiXetTuyen dt, int excludeId) {
        String cccd = dt.getCccd() != null ? dt.getCccd().trim() : "";
        if (cccd.isEmpty()) return "CCCD khong duoc de trong!";
        if (!thiSinhDAO.checkCccdExists(cccd)) return "CCCD \"" + cccd + "\" khong ton tai!";

        String pt = dt.getPhuongThuc();
        if (pt == null || pt.isBlank())
            return "Phuong thuc khong duoc de trong!";

        boolean isNew = excludeId < 0;
        if (isNew && dao.existsByCccdAndPhuongThuc(cccd, pt))
            return "CCCD \"" + cccd + "\" da co diem phuong thuc nay roi!";
        if (!isNew && dao.existsByCccdAndPhuongThucExcludeId(cccd, pt, excludeId))
            return "CCCD \"" + cccd + "\" da co diem phuong thuc nay o ban ghi khac!";

        // Mã phương thức: '4'=THPT, '3'=VSAT, '2'=ĐGNL
        if ("4".equals(pt)) return validateRange(dt, 0, 10);  // THPT thang 10
        if ("3".equals(pt)) return validateRange(dt, 0, 10);  // VSAT đã quy đổi thang 10
        return null; // '2' = ĐGNL thang 1200, không check range môn
    }

    // Validate tất cả điểm khác null phải trong [min, max]
    private String validateRange(DiemThiXetTuyen dt, double min, double max) {
        Double[] scores = {dt.getDiemToan(), dt.getDiemLy(), dt.getDiemHoa(), dt.getDiemSinh(),
                           dt.getDiemVan(), dt.getDiemSu(), dt.getDiemDia(), dt.getDiemTiengAnh()};
        for (Double s : scores) {
            if (s != null && (s < min || s > max))
                return "Diem phai trong khoang [" + (int)min + " - " + (int)max + "]!";
        }
        return null;
    }

    /**
     * Import danh sách điểm thi từ file Excel đã join.
     * Mỗi dòng = 1 thí sinh x 1 phương thức.
     * Bỏ qua dòng trùng (cccd + d_phuongthuc) đã tồn tại.
     */
    public String importDiemThi(List<DiemThiXetTuyen> list) {
        if (list == null || list.isEmpty())
            return "Lỗi: File không có dữ liệu!";

        // 1. Load toàn bộ key đang tồn tại về Java — CHỈ 1 query duy nhất
        Set<String> existingKeys = dao.getAllExistingKeys();

        // 2. Phân loại phía Java, không hỏi DB từng dòng nữa
        List<DiemThiXetTuyen> toInsert = new ArrayList<>();
        int skip = 0;
        for (DiemThiXetTuyen dt : list) {
            if (dt.getCccd() == null || dt.getCccd().isBlank() ||
                    dt.getPhuongThuc() == null || dt.getPhuongThuc().isBlank()) {
                skip++; continue;
            }
            String key = dt.getCccd() + "_" + dt.getPhuongThuc();
            if (existingKeys.contains(key)) {
                skip++; continue;
            }
            toInsert.add(dt);
        }

        // 3. Insert batch — 1 transaction duy nhất
        int[] result = dao.insertBatch(toInsert);

        return String.format(
                "Import hoàn tất!\n✓ Thêm mới : %d dòng\n⚠ Bỏ qua (trùng): %d dòng\n✗ Lỗi DB   : %d dòng",
                result[0], skip, result[1]);
    }

    // ── THỐNG KÊ ─────────────────────────────────────────

    /** Ánh xạ tên môn hiển thị → getter của entity. */
    private static final Map<String, Function<DiemThiXetTuyen, Double>> MON_GETTERS = Map.ofEntries(
        Map.entry("Toan",      DiemThiXetTuyen::getDiemToan),
        Map.entry("Ly",        DiemThiXetTuyen::getDiemLy),
        Map.entry("Hoa",       DiemThiXetTuyen::getDiemHoa),
        Map.entry("Sinh",      DiemThiXetTuyen::getDiemSinh),
        Map.entry("Van",       DiemThiXetTuyen::getDiemVan),
        Map.entry("Su",        DiemThiXetTuyen::getDiemSu),
        Map.entry("Dia",       DiemThiXetTuyen::getDiemDia),
        Map.entry("Tieng Anh", DiemThiXetTuyen::getN1Thi),
        Map.entry("LI",        DiemThiXetTuyen::getDiemTiengAnh),
        Map.entry("NL1",       DiemThiXetTuyen::getNl1),
        Map.entry("NK1",       DiemThiXetTuyen::getNk1),
        Map.entry("NK2",  DiemThiXetTuyen::getNk2),
        Map.entry("NK3",  DiemThiXetTuyen::getNk3),
        Map.entry("NK4",  DiemThiXetTuyen::getNk4),
        Map.entry("NK5",  DiemThiXetTuyen::getNk5),
        Map.entry("NK6",  DiemThiXetTuyen::getNk6),
        Map.entry("NK7",  DiemThiXetTuyen::getNk7),
        Map.entry("NK8",  DiemThiXetTuyen::getNk8),
        Map.entry("NK9",  DiemThiXetTuyen::getNk9),
        Map.entry("NK10", DiemThiXetTuyen::getNk10),
        Map.entry("GDCD", DiemThiXetTuyen::getDiemGdcd),
        Map.entry("CNCN",      DiemThiXetTuyen::getCncn),
        Map.entry("CNNN",      DiemThiXetTuyen::getCnnn),
        Map.entry("KTPL",      DiemThiXetTuyen::getDiemKtpl)
    );

    public static String[] getMonListForPhuongThuc(String pt) {
        if ("2".equals(pt)) {
            return new String[]{"NL1", "NK1", "NK2", "NK3", "NK4", "NK5", "NK6", "NK7", "NK8", "NK9", "NK10", "CNCN", "CNNN", "KTPL", "LI"};
        }
        if ("4".equals(pt) || "3".equals(pt)) {
            return new String[]{"Toan", "Ly", "Hoa", "Sinh", "Van", "Su", "Dia", "GDCD", "Tieng Anh", "LI"};
        }
        return new String[0];
    }

    public List<DiemThiXetTuyen> getAllByPhuongThuc(String phuongThuc) {
        return dao.getAllByPhuongThuc(phuongThuc);
    }

    /**
     * Phương thức thống kê tối ưu: Truyền thẳng danh sách đã được load 1 lần duy nhất từ DB vào,
     * tránh lặp lại vòng lặp I/O gây đơ máy.
     */
    public Stats getStatsFromLoadedList(List<DiemThiXetTuyen> preloadedList, String mon) {
        Function<DiemThiXetTuyen, Double> getter = MON_GETTERS.get(mon);
        if (getter == null || preloadedList == null) return new Stats();

        double[] values = preloadedList.stream()
                .map(getter).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).toArray();
        return Stats.compute(values);
    }

    // Giữ nguyên các hàm cũ để tương thích ngược nếu cần, nhưng không khuyến khích gọi lặp lại trong loop.
    public Stats getStats(String phuongThuc, String mon) {
        List<DiemThiXetTuyen> list = dao.getAllByPhuongThuc(phuongThuc);
        return getStatsFromLoadedList(list, mon);
    }

    public double[] getRawValues(String phuongThuc, String mon) {
        List<DiemThiXetTuyen> list = dao.getAllByPhuongThuc(phuongThuc);
        Function<DiemThiXetTuyen, Double> getter = MON_GETTERS.get(mon);
        if (getter == null) return new double[0];
        return list.stream()
                .map(getter).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).toArray();
    }

    public double[] getScoresForThongKe(String phuongThuc, String mon) {
        if ("VSAT".equals(phuongThuc) && "Tiếng Anh".equals(mon)) {
            List<DiemThiXetTuyen> list = dao.getAllByPhuongThuc(phuongThuc);
            return list.stream()
                    .map(DiemThiXetTuyen::getN1Thi).filter(Objects::nonNull)
                    .mapToDouble(Double::doubleValue).toArray();
        }
        return getRawValues(phuongThuc, mon);
    }

    // ── STATS INNER CLASS ─────────────────────────────────
    public static class Stats {
        public double min, max, avg, median, stdDev;
        public int count;

        public static Stats compute(double[] values) {
            Stats s = new Stats();
            s.count = values.length;
            if (s.count == 0) return s;

            double sum = 0;
            s.min = values[0]; s.max = values[0];
            for (double v : values) {
                sum += v;
                if (v < s.min) s.min = v;
                if (v > s.max) s.max = v;
            }
            s.avg = sum / s.count;

            double[] sorted = Arrays.copyOf(values, values.length);
            Arrays.sort(sorted);
            int mid = sorted.length / 2;
            s.median = (sorted.length % 2 == 0)
                    ? (sorted[mid - 1] + sorted[mid]) / 2.0 : sorted[mid];

            double variance = 0;
            for (double v : values) variance += (v - s.avg) * (v - s.avg);
            s.stdDev = Math.sqrt(variance / s.count);

            return s;
        }
    }
}
