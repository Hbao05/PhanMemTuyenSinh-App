package bus;

import dao.DiemCongDAO;
import dao.NganhDAO;
import dao.ThiSinhDAO;
import entity.DiemCongXetTuyen;

import java.util.List;

public class DiemCongBUS {

    private static final int ROWS_PER_PAGE = 20;
    private static final double MAX_DIEM_TONG = 3.0;

    private final DiemCongDAO dao   = new DiemCongDAO();
    private final ThiSinhDAO  thiSinhDAO = new ThiSinhDAO();
    private final NganhDAO    nganhDAO   = new NganhDAO();

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<DiemCongXetTuyen> getList(int page) {
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
    public List<DiemCongXetTuyen> search(int page, String keyword) {
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
    public DiemCongXetTuyen getById(int id) {
        return dao.getById(id);
    }

    // ── THÊM ─────────────────────────────────────────────
    public String addDiemCong(DiemCongXetTuyen dc) {
        String err = validate(dc, -1);
        if (err != null) return "Error: " + err;

        autoFill(dc);

        if (dao.existsByDcKeys(dc.getDcKeys()))
            return "Error: Ban ghi nay (CCCD + Nganh + ToHop + PT) da ton tai!";

        return dao.insert(dc) ? "Success" : "Error: Khong the them vao co so du lieu!";
    }

    // ── SỬA ──────────────────────────────────────────────
    public String updateDiemCong(DiemCongXetTuyen dc) {
        String err = validate(dc, dc.getIdDiemCong());
        if (err != null) return "Error: " + err;

        autoFill(dc);

        if (dao.existsByDcKeysExcludeId(dc.getDcKeys(), dc.getIdDiemCong()))
            return "Error: Ban ghi nay (CCCD + Nganh + ToHop + PT) da ton tai!";

        return dao.update(dc) ? "Success" : "Error: Khong the cap nhat co so du lieu!";
    }

    // ── XÓA ──────────────────────────────────────────────
    public String deleteDiemCong(int id) {
        return dao.delete(id) ? "Success" : "Error: Khong the xoa ban ghi nay!";
    }

    // ── VALIDATION ────────────────────────────────────────
    private String validate(DiemCongXetTuyen dc, int excludeId) {
        String cccd = dc.getCccd() != null ? dc.getCccd().trim() : "";
        if (cccd.isEmpty()) return "CCCD khong duoc de trong!";
        if (!thiSinhDAO.checkCccdExists(cccd)) return "CCCD \"" + cccd + "\" khong ton tai trong he thong!";

        String maNganh = dc.getMaNganh() != null ? dc.getMaNganh().trim() : "";
        if (maNganh.isEmpty()) return "Ma nganh khong duoc de trong!";
        if (!nganhDAO.existsByMaNganh(maNganh)) return "Ma nganh \"" + maNganh + "\" khong ton tai!";

        if (dc.getPhuongThuc() == null || dc.getPhuongThuc().isBlank())
            return "Phuong thuc khong duoc de trong!";

        if (dc.getDiemCc() != null && (dc.getDiemCc() < 0 || dc.getDiemCc() > 10))
            return "Diem chung chi phai trong khoang 0 - 10!";

        if (dc.getDiemUtXt() != null && (dc.getDiemUtXt() < 0 || dc.getDiemUtXt() > 3))
            return "Diem uu tien xet tuyen phai trong khoang 0 - 3!";

        return null;
    }

    // ── AUTO FILL ─────────────────────────────────────────
    private void autoFill(DiemCongXetTuyen dc) {
        dc.setCccd(dc.getCccd().trim());
        dc.setMaNganh(dc.getMaNganh().trim());

        // diemTong = diemCc + diemUtXt, capped at MAX_DIEM_TONG
        double cc   = dc.getDiemCc()   != null ? dc.getDiemCc()   : 0.0;
        double utxt = dc.getDiemUtXt() != null ? dc.getDiemUtXt() : 0.0;
        dc.setDiemTong(Math.min(cc + utxt, MAX_DIEM_TONG));

        // dcKeys = cccd|maNganh|maToHop|phuongThuc
        String toHop = dc.getMaToHop()    != null ? dc.getMaToHop()    : "";
        String pt    = dc.getPhuongThuc() != null ? dc.getPhuongThuc() : "";
        dc.setDcKeys(dc.getCccd() + "|" + dc.getMaNganh() + "|" + toHop + "|" + pt);
    }
}
