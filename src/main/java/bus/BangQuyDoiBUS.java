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

    // ── IMPORT EXCEL ─────────────────────────────────────
    /**
     * Import bảng quy đổi từ file Excel.
     * Cột theo thứ tự: phuongThuc, toHop, mon, diemA, diemB, diemC, diemD, phanVi
     * maQuyDoi tự tạo = phuongThuc_toHop_phanVi
     * @param replaceAll true = xóa tất cả trước khi import
     */
    public String importExcel(java.io.File file, boolean replaceAll) {
        try {
            java.util.List<BangQuyDoi> list = new java.util.ArrayList<>();

            try (java.io.InputStream is = new java.io.FileInputStream(file);
                 org.apache.poi.ss.usermodel.Workbook wb = com.github.pjfanning.xlsx.StreamingReader.builder()
                         .rowCacheSize(100).bufferSize(4096).open(is)) {

                org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(0);
                boolean firstRow = true;

                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    if (firstRow) { firstRow = false; continue; } // skip header

                    String pt     = cellStr(row, 0);
                    String toHop  = cellStr(row, 1);
                    String mon    = cellStr(row, 2);
                    Double diemA  = cellDbl(row, 3);
                    Double diemB  = cellDbl(row, 4);
                    Double diemC  = cellDbl(row, 5);
                    Double diemD  = cellDbl(row, 6);
                    String phanVi = cellStr(row, 8);

                    if (pt == null || pt.isBlank()) continue; // bỏ dòng trống

                    BangQuyDoi bqd = new BangQuyDoi();
                    bqd.setPhuongThuc(pt.trim());
                    bqd.setToHop(toHop != null ? toHop.trim() : "");
                    bqd.setMon(mon != null ? mon.trim() : "");
                    bqd.setDiemA(diemA);
                    bqd.setDiemB(diemB);
                    bqd.setDiemC(diemC);
                    bqd.setDiemD(diemD);
                    bqd.setPhanVi(phanVi != null ? phanVi.trim() : "");

                    // Auto-generate maQuyDoi = phuongThuc_toHop_phanVi
                    String maQD = bqd.getPhuongThuc() + "_" + (!bqd.getToHop().isEmpty() ? bqd.getToHop() : bqd.getMon()) + "_" + bqd.getPhanVi();
                    bqd.setMaQuyDoi(maQD);

                    list.add(bqd);
                }
            }

            if (list.isEmpty()) return "Error: File không có dữ liệu hợp lệ!";

            if (replaceAll) {
                dao.deleteAll();
            }

            int inserted = dao.batchInsert(list);
            return "Success|Đã import " + inserted + " / " + list.size() + " bản ghi.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String cellStr(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private Double cellDbl(org.apache.poi.ss.usermodel.Row row, int col) {
        org.apache.poi.ss.usermodel.Cell cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            try { return Double.parseDouble(cell.getStringCellValue()); }
            catch (Exception ex) { return null; }
        }
    }
}
