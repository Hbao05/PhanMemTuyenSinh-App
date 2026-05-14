package bus;

import dao.BangQuyDoiDAO;
import entity.BangQuyDoi;

import java.util.List;

public class BangQuyDoiBUS {

    private static final int ROWS_PER_PAGE = 20;
    private final BangQuyDoiDAO dao = new BangQuyDoiDAO();

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<BangQuyDoi> getList(int page) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.getPaginatedList(offset, ROWS_PER_PAGE);
    }

    public int calculateTotalPages() {
        long total = dao.countTotal();
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getTotalCount() {
        return dao.countTotal();
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<BangQuyDoi> search(int page, String keyword) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.search(offset, ROWS_PER_PAGE, keyword);
    }

    public int calculateSearchTotalPages(String keyword) {
        long total = dao.countSearch(keyword);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getSearchCount(String keyword) {
        return dao.countSearch(keyword);
    }

    // ── GET BY ID ─────────────────────────────────────────
    public BangQuyDoi getById(int id) {
        return dao.getById(id);
    }

    // ── THÊM ─────────────────────────────────────────────
    public String addBangQuyDoi(BangQuyDoi bqd) {
        String ma = (bqd.getMaQuyDoi() != null) ? bqd.getMaQuyDoi().trim() : "";
        if (ma.isEmpty()) return "Error: Ma quy doi khong duoc de trong!";

        if (dao.existsByMaQuyDoi(ma)) return "Error: Ma quy doi \"" + ma + "\" da ton tai!";

        if (bqd.getPhuongThuc() == null || bqd.getPhuongThuc().isBlank())
            return "Error: Phuong thuc khong duoc de trong!";

        boolean ok = dao.insert(bqd);
        return ok ? "Success" : "Error: Khong the them vao co so du lieu!";
    }

    // ── SỬA ──────────────────────────────────────────────
    public String updateBangQuyDoi(BangQuyDoi bqd) {
        String ma = (bqd.getMaQuyDoi() != null) ? bqd.getMaQuyDoi().trim() : "";
        if (ma.isEmpty()) return "Error: Ma quy doi khong duoc de trong!";

        if (dao.existsByMaQuyDoiExcludeId(ma, bqd.getIdQd()))
            return "Error: Ma quy doi \"" + ma + "\" da ton tai!";

        if (bqd.getPhuongThuc() == null || bqd.getPhuongThuc().isBlank())
            return "Error: Phuong thuc khong duoc de trong!";

        boolean ok = dao.update(bqd);
        return ok ? "Success" : "Error: Khong the cap nhat co so du lieu!";
    }

    // ── LẤY TẤT CẢ (cho XetTuyenEngine) ─────────────────
    public java.util.List<entity.BangQuyDoi> getAll() {
        return dao.getPaginatedList(0, Integer.MAX_VALUE);
    }

    // ── XÓA ──────────────────────────────────────────────
    public String deleteBangQuyDoi(int id) {
        boolean ok = dao.delete(id);
        return ok ? "Success" : "Error: Khong the xoa ban ghi nay!";
    }
}
