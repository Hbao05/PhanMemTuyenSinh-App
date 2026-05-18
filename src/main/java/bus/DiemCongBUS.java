package bus;

import dao.DiemCongDAO;
import dao.NganhDAO;
import dao.ThiSinhDAO;
import entity.DiemCongXetTuyen;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.github.pjfanning.xlsx.StreamingReader;

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
        dc.setDcKeys(dc.getCccd() + "_" + dc.getMaNganh() + "_" + toHop);
    }

    // ── IMPORT EXCEL ─────────────────────────────────────
    public interface ProgressCallback {
        void onProgress(int processed, int success, String message);
    }

    public void importFromExcel(File file, ProgressCallback callback) {
        int batchSize = 5000;
        List<DiemCongXetTuyen> batch = new ArrayList<>(batchSize);
        int totalProcessed = 0;
        int totalSuccess = 0;
        
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)
                     .bufferSize(4096)
                     .open(is)) {
            boolean cancelled = false;
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    if (Thread.currentThread().isInterrupted()) {

                        cancelled = true;

                        if (!batch.isEmpty()) {
                            int success = dao.batchInsert(batch);
                            totalSuccess += success;
                            batch.clear();
                        }

                        break;
                    }
                    if (row.getRowNum() == 0) continue; // Skip header

                    try {
                        String cccd = getCellValue(row.getCell(0));
                        if (cccd == null || cccd.isBlank()) continue;
                        
                        DiemCongXetTuyen dc = new DiemCongXetTuyen();
                        dc.setCccd(cccd.trim());
                        dc.setMaNganh(getCellValue(row.getCell(1)));
                        dc.setMaToHop(getCellValue(row.getCell(2)));
                        
                        String diemCCStr = getCellValue(row.getCell(3));
                        if (diemCCStr != null && !diemCCStr.isBlank()) {
                            dc.setDiemCc(Double.parseDouble(diemCCStr));
                        }
                        
                        dc.setPhuongThuc(getCellValue(row.getCell(4)));
                        
                        String diemUtStr = getCellValue(row.getCell(5));
                        if (diemUtStr != null && !diemUtStr.isBlank()) {
                            dc.setDiemUtXt(Double.parseDouble(diemUtStr));
                        }
                        
                        String diemTongStr = getCellValue(row.getCell(6));
                        if (diemTongStr != null && !diemTongStr.isBlank()) {
                            dc.setDiemTong(Double.parseDouble(diemTongStr));
                        }
                        
                        autoFill(dc);
                        
                        batch.add(dc);
                        totalProcessed++;
                        
                        if (batch.size() >= batchSize) {
                            int success = dao.batchInsert(batch);
                            totalSuccess += success;
                            batch.clear();
                            if (callback != null) {
                                callback.onProgress(totalProcessed, totalSuccess, "Đang xử lý...");
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi dòng " + row.getRowNum() + ": " + ex.getMessage());
                    }
                }
                if (cancelled) {
                    break;
                }
            }

            if (!cancelled && !batch.isEmpty()) {

                int success = dao.batchInsert(batch);

                totalSuccess += success;

                if (callback != null) {
                    callback.onProgress(
                            totalProcessed,
                            totalSuccess,
                            "Đang xử lý..."
                    );
                }
            }

            if (callback != null) {
                callback.onProgress(totalProcessed, totalSuccess, "Hoàn thành!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onProgress(totalProcessed, totalSuccess, "Lỗi: " + e.getMessage());
            }
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC: 
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    return String.format("%d", (long) val);
                } else {
                    return String.valueOf(val);
                }
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }
}
