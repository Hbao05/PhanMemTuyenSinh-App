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
        if (!thiSinhDAO.checkCccdExists(cccd)) return "CCCD \"" + cccd + "\" khong ton tai trong he thong!";

        boolean isNew = excludeId < 0;
        if (isNew && dao.existsByCccd(cccd))
            return "CCCD \"" + cccd + "\" da co ban ghi diem thi!";
        if (!isNew && dao.existsByCccdExcludeId(cccd, excludeId))
            return "CCCD \"" + cccd + "\" da co ban ghi diem thi khac!";

        if (dt.getPhuongThuc() == null || dt.getPhuongThuc().isBlank())
            return "Phuong thuc khong duoc de trong!";

        String pt = dt.getPhuongThuc();
        if ("THPT".equals(pt)) return validateRange(dt, 0, 10);
        if ("VSAT".equals(pt)) return validateRange(dt, 0, 150);
        return null; // DGNL: no strict range check per plan
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
        Map.entry("Tieng Anh", DiemThiXetTuyen::getDiemTiengAnh),
        Map.entry("NL1",       DiemThiXetTuyen::getNl1),
        Map.entry("NK1",       DiemThiXetTuyen::getNk1),
        Map.entry("NK2",       DiemThiXetTuyen::getNk2),
        Map.entry("CNCN",      DiemThiXetTuyen::getCncn),
        Map.entry("CNNN",      DiemThiXetTuyen::getCnnn),
        Map.entry("KTPL",      DiemThiXetTuyen::getDiemKtpl)
    );

    public static String[] getMonListForPhuongThuc(String pt) {
        if ("DGNL".equals(pt)) return new String[]{"NL1","NK1","NK2","CNCN","CNNN","KTPL"};
        return new String[]{"Toan","Ly","Hoa","Sinh","Van","Su","Dia","Tieng Anh"};
    }

    public Stats getStats(String phuongThuc, String mon) {
        List<DiemThiXetTuyen> list = dao.getAllByPhuongThuc(phuongThuc);
        Function<DiemThiXetTuyen, Double> getter = MON_GETTERS.get(mon);
        if (getter == null) return new Stats();

        double[] values = list.stream()
                .map(getter).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).toArray();
        return Stats.compute(values);
    }

    public double[] getRawValues(String phuongThuc, String mon) {
        List<DiemThiXetTuyen> list = dao.getAllByPhuongThuc(phuongThuc);
        Function<DiemThiXetTuyen, Double> getter = MON_GETTERS.get(mon);
        if (getter == null) return new double[0];
        return list.stream()
                .map(getter).filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue).toArray();
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
