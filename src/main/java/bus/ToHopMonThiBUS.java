package bus;

import dao.ToHopMonThiDAO;
import entity.ToHopMonThi;

import java.util.List;

public class ToHopMonThiBUS {
    private final ToHopMonThiDAO dao;
    private static final int ROWS_PER_PAGE = 20;

    public ToHopMonThiBUS() {
        this.dao = new ToHopMonThiDAO();
    }

    public List<ToHopMonThi> getList(int page) {
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

    public List<ToHopMonThi> search(int page, String keyword) {
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

    public ToHopMonThi getById(int id) {
        return dao.getById(id);
    }

    public List<ToHopMonThi> getAll() {
        return dao.getAll();
    }
    
    public String addToHop(ToHopMonThi toHop) {
        if (toHop.getMaToHop() == null || toHop.getMaToHop().trim().isEmpty()) {
            return "Error: Mã tổ hợp không được để trống!";
        }
        if (hasDuplicateSubjects(toHop)) {
            return "Error: Các môn trong tổ hợp không được trùng nhau!";
        }
        if (toHop.getMon1() == null || toHop.getMon1().trim().isEmpty() ||
            toHop.getMon2() == null || toHop.getMon2().trim().isEmpty() ||
            toHop.getMon3() == null || toHop.getMon3().trim().isEmpty()) {
            return "Error: 3 môn học không được để trống!";
        }
        if (dao.existsByMaToHop(toHop.getMaToHop().trim())) {
            return "Error: Mã tổ hợp \"" + toHop.getMaToHop() + "\" đã tồn tại!";
        }
        boolean ok = dao.insert(toHop);
        return ok ? "Success" : "Error: Không thể lưu vào cơ sở dữ liệu!";
    }

    public String updateToHop(ToHopMonThi toHop) {
        if (toHop.getIdToHop() <= 0) {
            return "Error: Không xác định được tổ hợp cần cập nhật!";
        }
        if (toHop.getMaToHop() == null || toHop.getMaToHop().trim().isEmpty()) {
            return "Error: Mã tổ hợp không được để trống!";
        }
        if (hasDuplicateSubjects(toHop)) {
            return "Error: Các môn trong tổ hợp không được trùng nhau!";
        }
        if (toHop.getMon1() == null || toHop.getMon1().trim().isEmpty() ||
            toHop.getMon2() == null || toHop.getMon2().trim().isEmpty() ||
            toHop.getMon3() == null || toHop.getMon3().trim().isEmpty()) {
            return "Error: 3 môn học không được để trống!";
        }
        boolean ok = dao.update(toHop);
        return ok ? "Success" : "Error: Không thể cập nhật cơ sở dữ liệu!";
    }

    public String deleteToHop(int id) {
        if (id <= 0) return "Error: Không xác định được tổ hợp cần xóa!";
        boolean ok = dao.delete(id);
        return ok ? "Success" : "Error: Không thể xóa. Tổ hợp có thể đang được liên kết dữ liệu.";
    }

    public String importToHop(List<ToHopMonThi> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
        }

        List<ToHopMonThi> existing = dao.getAll();
        java.util.Set<String> existingCodes = new java.util.HashSet<>();
        if (existing != null) {
            for (ToHopMonThi t : existing) existingCodes.add(t.getMaToHop().trim().toUpperCase());
        }

        int added = 0, skipped = 0;
        for (ToHopMonThi t : importList) {
            String key = t.getMaToHop().trim().toUpperCase();
            if (existingCodes.contains(key)) {
                skipped++;
            } else if (hasDuplicateSubjects(t)) {
                skipped++;
            } else {
                boolean ok = dao.insert(t);
                if (ok) { added++; existingCodes.add(key); }
                else skipped++;
            }
        }

        return String.format("Import hoàn tất!\n- Thêm mới thành công: %d tổ hợp.\n- Bỏ qua (trùng mã / lỗi): %d dòng.", added, skipped);
    }
    private boolean hasDuplicateSubjects(ToHopMonThi t) {
        String m1 = t.getMon1() != null ? t.getMon1().trim() : "";
        String m2 = t.getMon2() != null ? t.getMon2().trim() : "";
        String m3 = t.getMon3() != null ? t.getMon3().trim() : "";
        
        if (m1.isEmpty() || m2.isEmpty() || m3.isEmpty()) return false;
        return m1.equals(m2) || m1.equals(m3) || m2.equals(m3);
    }
}
