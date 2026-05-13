package bus;

import dao.NguoiDungDAO;
import entity.NguoiDung;
import util.PasswordUtil;

import java.util.List;

public class NguoiDungBUS {
    private final NguoiDungDAO dao;
    private static final int ROWS_PER_PAGE = 20;

    public NguoiDungBUS() {
        this.dao = new NguoiDungDAO();
    }

    // ── ĐĂNG NHẬP ────────────────────────────────────────────────────
    public NguoiDung login(String username, String password) {
        if (username == null || username.isBlank()) return null;
        if (password == null || password.isBlank()) return null;
        NguoiDung nd = dao.findByUsername(username.trim());
        if (nd == null) return null;
        if (!nd.isKichHoat()) return null;
        if (!PasswordUtil.verify(password, nd.getPassword())) return null;
        return nd;
    }

    // ── PHÂN TRANG ────────────────────────────────────────────────────
    public List<NguoiDung> getList(int page) {
        if (page < 1) page = 1;
        return dao.getPaginatedList((page - 1) * ROWS_PER_PAGE, ROWS_PER_PAGE);
    }

    public int calculateTotalPages() {
        long total = dao.countTotal();
        int pages = (int) Math.ceil((double) total / ROWS_PER_PAGE);
        return pages == 0 ? 1 : pages;
    }

    public long getTotalCount() {
        return dao.countTotal();
    }

    // ── TÌM KIẾM ─────────────────────────────────────────────────────
    public List<NguoiDung> search(int page, String keyword) {
        if (page < 1) page = 1;
        if (keyword == null) keyword = "";
        return dao.search((page - 1) * ROWS_PER_PAGE, ROWS_PER_PAGE, keyword.trim());
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

    public NguoiDung getById(int id) {
        return dao.getById(id);
    }

    // ── THÊM MỚI ─────────────────────────────────────────────────────
    public String addUser(NguoiDung nd, String plainPassword) {
        String err = validateFields(nd.getUsername(), plainPassword, nd.getEmail());
        if (err != null) return err;
        if (dao.existsByUsername(nd.getUsername().trim()))
            return "Error: Username \"" + nd.getUsername() + "\" đã tồn tại!";
        nd.setUsername(nd.getUsername().trim());
        nd.setPassword(PasswordUtil.hash(plainPassword));
        return dao.insert(nd) ? "Success" : "Error: Không thể lưu vào cơ sở dữ liệu!";
    }

    // ── CẬP NHẬT THÔNG TIN (không đổi password) ─────────────────────
    public String updateUser(NguoiDung nd) {
        if (nd.getId() == null || nd.getId() <= 0)
            return "Error: Không xác định được người dùng!";
        if (nd.getHoTen() == null || nd.getHoTen().isBlank())
            return "Error: Họ tên không được để trống!";
        return dao.update(nd) ? "Success" : "Error: Không thể cập nhật!";
    }

    // ── ĐỔI MẬT KHẨU ─────────────────────────────────────────────────
    public String changePassword(int id, String oldPwd, String newPwd) {
        if (oldPwd == null || oldPwd.isBlank()) return "Error: Vui lòng nhập mật khẩu cũ!";
        if (newPwd == null || newPwd.length() < 6) return "Error: Mật khẩu mới phải có ít nhất 6 ký tự!";
        NguoiDung nd = dao.getById(id);
        if (nd == null) return "Error: Không tìm thấy người dùng!";
        if (!PasswordUtil.verify(oldPwd, nd.getPassword())) return "Error: Mật khẩu cũ không đúng!";
        nd.setPassword(PasswordUtil.hash(newPwd));
        return dao.update(nd) ? "Success" : "Error: Không thể cập nhật mật khẩu!";
    }

    // ── ĐỔI QUYỀN ────────────────────────────────────────────────────
    public String toggleRole(int id) {
        NguoiDung nd = dao.getById(id);
        if (nd == null) return "Error: Không tìm thấy người dùng!";
        nd.setQuyen(nd.getQuyen() == NguoiDung.Quyen.ADMIN ? NguoiDung.Quyen.USER : NguoiDung.Quyen.ADMIN);
        return dao.update(nd) ? "Success" : "Error: Không thể cập nhật quyền!";
    }

    // ── ENABLE / DISABLE ─────────────────────────────────────────────
    public String toggleActive(int id) {
        NguoiDung nd = dao.getById(id);
        if (nd == null) return "Error: Không tìm thấy người dùng!";
        nd.setKichHoat(!nd.isKichHoat());
        return dao.update(nd) ? "Success" : "Error: Không thể cập nhật trạng thái!";
    }

    // ── XÓA ───────────────────────────────────────────────────────────
    public String deleteUser(int id) {
        if (id <= 0) return "Error: Không xác định được người dùng!";
        return dao.delete(id) ? "Success" : "Error: Không thể xóa người dùng!";
    }

    // ── VALIDATE ─────────────────────────────────────────────────────
    private String validateFields(String username, String password, String email) {
        if (username == null || username.isBlank())
            return "Error: Username không được để trống!";
        if (username.trim().length() < 3)
            return "Error: Username phải có ít nhất 3 ký tự!";
        if (password == null || password.length() < 6)
            return "Error: Mật khẩu phải có ít nhất 6 ký tự!";
        if (email != null && !email.isBlank() && !email.contains("@"))
            return "Error: Email không hợp lệ!";
        return null;
    }
}
