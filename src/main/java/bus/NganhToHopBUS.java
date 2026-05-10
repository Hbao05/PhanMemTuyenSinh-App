package bus;

import dao.NganhToHopDAO;
import entity.NganhToHop;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NganhToHopBUS {
    private final NganhToHopDAO dao;
    private static final int ROWS_PER_PAGE = 20;

    public NganhToHopBUS() {
        this.dao = new NganhToHopDAO();
    }

    // ── Phân trang ──
    public List<NganhToHop> getList(int page) {
        if (page < 1) page = 1;
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.getPaginatedList(offset, ROWS_PER_PAGE);
    }

    public int calculateTotalPages() {
        long total = dao.countTotal();
        int pages = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }

    public long getTotalCount() {
        return dao.countTotal();
    }

    // ── Tìm kiếm ──
    public List<NganhToHop> search(int page, String keyword) {
        if (page < 1) page = 1;
        if (keyword == null) keyword = "";
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.search(offset, ROWS_PER_PAGE, keyword.trim());
    }

    public int calculateSearchTotalPages(String keyword) {
        if (keyword == null) keyword = "";
        long total = dao.countSearch(keyword.trim());
        int pages = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }

    public long getSearchCount(String keyword) {
        if (keyword == null) keyword = "";
        return dao.countSearch(keyword.trim());
    }

    // ── CRUD ──
    public NganhToHop getById(int id) {
        return dao.getById(id);
    }

    public String addNganhToHop(NganhToHop item) {
        if (item.getMaNganh() == null || item.getMaNganh().trim().isEmpty()) {
            return "Error: Mã ngành không được để trống!";
        }
        if (item.getMaToHop() == null || item.getMaToHop().trim().isEmpty()) {
            return "Error: Mã tổ hợp không được để trống!";
        }
        if (item.getTbKeys() != null && dao.existsByTbKeys(item.getTbKeys().trim())) {
            return "Error: Liên kết Ngành-Tổ hợp \"" + item.getTbKeys() + "\" đã tồn tại!";
        }
        boolean ok = dao.insert(item);
        return ok ? "Success" : "Error: Không thể lưu vào cơ sở dữ liệu!";
    }

    public String updateNganhToHop(NganhToHop item) {
        if (item.getId() <= 0) {
            return "Error: Không xác định được bản ghi cần cập nhật!";
        }
        boolean ok = dao.update(item);
        return ok ? "Success" : "Error: Không thể cập nhật cơ sở dữ liệu!";
    }

    public String deleteNganhToHop(int id) {
        if (id <= 0) return "Error: Không xác định được bản ghi cần xóa!";
        boolean ok = dao.delete(id);
        return ok ? "Success" : "Error: Không thể xóa bản ghi.";
    }

    // ── Import hàng loạt ──
    public String importNganhToHop(List<NganhToHop> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
        }

        // Lấy toàn bộ tbKeys đã tồn tại trong DB
        List<NganhToHop> existing = dao.getAll();
        Set<String> existingKeys = new HashSet<>();
        if (existing != null) {
            for (NganhToHop n : existing) {
                if (n.getTbKeys() != null) existingKeys.add(n.getTbKeys().trim());
            }
        }

        int added = 0, skipped = 0;
        for (NganhToHop item : importList) {
            String key = item.getTbKeys() != null ? item.getTbKeys().trim() : "";
            if (!key.isEmpty() && existingKeys.contains(key)) {
                skipped++;
            } else {
                boolean ok = dao.insert(item);
                if (ok) {
                    added++;
                    if (!key.isEmpty()) existingKeys.add(key);
                } else {
                    skipped++;
                }
            }
        }

        return String.format("Import hoàn tất!\n- Thêm mới thành công: %d liên kết Ngành-Tổ hợp.\n- Bỏ qua (trùng / lỗi): %d dòng.", added, skipped);
    }
}
