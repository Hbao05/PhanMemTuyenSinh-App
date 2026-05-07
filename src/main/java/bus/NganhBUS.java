package bus;

import dao.NganhDAO;
import entity.Nganh;

import java.util.List;

public class NganhBUS {
    private final NganhDAO nganhDAO;
    private static final int ROWS_PER_PAGE = 20;

    public NganhBUS() {
        this.nganhDAO = new NganhDAO();
    }

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<Nganh> getList(int page) {
        if (page < 1) page = 1;
        int offset = (page - 1) * ROWS_PER_PAGE;
        return nganhDAO.getPaginatedList(offset, ROWS_PER_PAGE);
    }

    public int calculateTotalPages() {
        long total = nganhDAO.countTotal();
        int pages = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }

    public long getTotalCount() {
        return nganhDAO.countTotal();
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<Nganh> search(int page, String keyword) {
        if (page < 1) page = 1;
        if (keyword == null) keyword = "";
        int offset = (page - 1) * ROWS_PER_PAGE;
        return nganhDAO.search(offset, ROWS_PER_PAGE, keyword.trim());
    }

    public int calculateSearchTotalPages(String keyword) {
        if (keyword == null) keyword = "";
        long total = nganhDAO.countSearch(keyword.trim());
        int pages = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }

    public long getSearchCount(String keyword) {
        if (keyword == null) keyword = "";
        return nganhDAO.countSearch(keyword.trim());
    }

    // ── LẤY THEO ID ──────────────────────────────────────
    public Nganh getById(int id) {
        return nganhDAO.getById(id);
    }

    // ── LẤY TẤT CẢ (ComboBox) ────────────────────────────
    public List<Nganh> getAll() {
        return nganhDAO.getAll();
    }

    // ── THÊM MỚI ─────────────────────────────────────────
    public String addNganh(Nganh nganh) {
        if (nganh.getMaNganh() == null || nganh.getMaNganh().trim().isEmpty()) {
            return "Error: Mã ngành không được để trống!";
        }
        if (nganh.getTenNganh() == null || nganh.getTenNganh().trim().isEmpty()) {
            return "Error: Tên ngành không được để trống!";
        }
        if (nganh.getChiTieu() < 0) {
            return "Error: Chỉ tiêu không được âm!";
        }
        if (nganhDAO.existsByMaNganh(nganh.getMaNganh().trim())) {
            return "Error: Mã ngành \"" + nganh.getMaNganh() + "\" đã tồn tại!";
        }
        boolean ok = nganhDAO.insert(nganh);
        return ok ? "Success" : "Error: Không thể lưu vào cơ sở dữ liệu!";
    }

    // ── CẬP NHẬT ─────────────────────────────────────────
    public String updateNganh(Nganh nganh) {
        if (nganh.getIdNganh() <= 0) {
            return "Error: Không xác định được ngành cần cập nhật!";
        }
        if (nganh.getMaNganh() == null || nganh.getMaNganh().trim().isEmpty()) {
            return "Error: Mã ngành không được để trống!";
        }
        if (nganh.getTenNganh() == null || nganh.getTenNganh().trim().isEmpty()) {
            return "Error: Tên ngành không được để trống!";
        }
        if (nganh.getChiTieu() < 0) {
            return "Error: Chỉ tiêu không được âm!";
        }
        boolean ok = nganhDAO.update(nganh);
        return ok ? "Success" : "Error: Không thể cập nhật cơ sở dữ liệu!";
    }

    // ── XÓA ───────────────────────────────────────────────
    public String deleteNganh(int id) {
        if (id <= 0) return "Error: Không xác định được ngành cần xóa!";
        boolean ok = nganhDAO.delete(id);
        return ok ? "Success" : "Error: Không thể xóa. Ngành có thể đang được liên kết dữ liệu.";
    }

    // ── HELPER PARSE ─────────────────────────────────────
    public static int parseIntSafe(String s) {
        try { return Integer.parseInt(s.trim()); }
        catch (Exception e) { return 0; }
    }

    public static Double parseDoubleSafe(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try { return Double.parseDouble(s.trim()); }
        catch (Exception e) { return null; }
    }

    // ── IMPORT BATCH ──────────────────────────────────────
    public String importNganh(List<Nganh> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
        }

        // Lấy toàn bộ mã ngành đã có trong DB lên RAM
        List<Nganh> existing = nganhDAO.getAll();
        java.util.Set<String> existingCodes = new java.util.HashSet<>();
        if (existing != null) {
            for (Nganh n : existing) existingCodes.add(n.getMaNganh().trim().toUpperCase());
        }

        int added = 0, skipped = 0;
        for (Nganh n : importList) {
            String key = n.getMaNganh().trim().toUpperCase();
            if (existingCodes.contains(key)) {
                skipped++;
            } else {
                boolean ok = nganhDAO.insert(n);
                if (ok) { added++; existingCodes.add(key); }
                else skipped++;
            }
        }

        return String.format("Import hoàn tất!\n- Thêm mới thành công: %d ngành.\n- Bỏ qua (trùng mã / lỗi): %d dòng.",
                added, skipped);
    }
}
