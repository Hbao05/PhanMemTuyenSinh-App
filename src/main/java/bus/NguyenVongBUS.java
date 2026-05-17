package bus;

import dao.*;
import entity.*;

import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import com.github.pjfanning.xlsx.StreamingReader;

public class NguyenVongBUS {

    private static final int ROWS_PER_PAGE = 20;

    private final NguyenVongDAO   dao           = new NguyenVongDAO();
    private final ThiSinhDAO      thiSinhDAO    = new ThiSinhDAO();
    private final NganhDAO        nganhDAO      = new NganhDAO();
    private final NganhToHopDAO   nganhToHopDAO = new NganhToHopDAO();
    private final DiemThiDAO      diemThiDAO    = new DiemThiDAO();
    private final DiemCongDAO     diemCongDAO   = new DiemCongDAO();
    private final BangQuyDoiBUS   bqdBUS        = new BangQuyDoiBUS();

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<NguyenVongXetTuyen> getList(int page, String filterNganh, String filterKetQua) {
        int offset = (page - 1) * ROWS_PER_PAGE;
        return dao.getPaginatedList(offset, ROWS_PER_PAGE, filterNganh, filterKetQua);
    }

    public int calculateTotalPages(String filterNganh, String filterKetQua) {
        long total = dao.countList(filterNganh, filterKetQua);
        return (int) Math.max(1, Math.ceil((double) total / ROWS_PER_PAGE));
    }

    public long getTotalCount(String filterNganh, String filterKetQua) {
        return dao.countList(filterNganh, filterKetQua);
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<NguyenVongXetTuyen> search(int page, String keyword) {
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
    public NguyenVongXetTuyen getById(int id) { return dao.getById(id); }

    // ── GET BY CCCD (xem kết quả) ────────────────────────
    public List<NguyenVongXetTuyen> getByCccd(String cccd) { return dao.getByCccd(cccd); }

    // ── THÊM ─────────────────────────────────────────────
    public String addNguyenVong(NguyenVongXetTuyen nv) {
        String err = validate(nv, -1);
        if (err != null) return "Error: " + err;
        autoFill(nv);
        if (dao.existsByNvKeys(nv.getNvKeys())) return "Error: Nguyen vong nay (CCCD + Nganh) da ton tai!";
        return dao.insert(nv) ? "Success" : "Error: Khong the them vao co so du lieu!";
    }

    // ── SỬA ──────────────────────────────────────────────
    public String updateNguyenVong(NguyenVongXetTuyen nv) {
        String err = validate(nv, nv.getIdNv());
        if (err != null) return "Error: " + err;
        autoFill(nv);
        if (dao.existsByNvKeysExcludeId(nv.getNvKeys(), nv.getIdNv()))
            return "Error: Nguyen vong nay (CCCD + Nganh) da ton tai!";
        return dao.update(nv) ? "Success" : "Error: Khong the cap nhat co so du lieu!";
    }

    // ── XÓA ──────────────────────────────────────────────
    public String deleteNguyenVong(int id) {
        return dao.delete(id) ? "Success" : "Error: Khong the xoa ban ghi nay!";
    }

    // ── VALIDATION ────────────────────────────────────────
    private String validate(NguyenVongXetTuyen nv, int excludeId) {
        String cccd = nv.getCccd() != null ? nv.getCccd().trim() : "";
        if (cccd.isEmpty()) return "CCCD khong duoc de trong!";
        if (!thiSinhDAO.checkCccdExists(cccd)) return "CCCD \"" + cccd + "\" khong ton tai!";

        String maNganh = nv.getMaNganh() != null ? nv.getMaNganh().trim() : "";
        if (maNganh.isEmpty()) return "Ma nganh khong duoc de trong!";
        if (!nganhDAO.existsByMaNganh(maNganh)) return "Ma nganh \"" + maNganh + "\" khong ton tai!";

        if (nv.getThuTuNguyenVong() <= 0) return "Thu tu nguyen vong phai lon hon 0!";
        if (nv.getPhuongThuc() == null || nv.getPhuongThuc().isBlank())
            return "Phuong thuc khong duoc de trong!";

        return null;
    }

    private void autoFill(NguyenVongXetTuyen nv) {
        nv.setCccd(nv.getCccd().trim());
        nv.setMaNganh(nv.getMaNganh().trim());
        nv.setNvKeys(nv.getCccd() + "_" + nv.getMaNganh() + "_" + nv.getPhuongThuc());
    }

    // ── CHẠY XÉT TUYỂN ───────────────────────────────────
    /**
     * Tải toàn bộ dữ liệu cần thiết vào HashMap, chạy engine, lưu kết quả về DB.
     * Dữ liệu nhỏ (BangQuyDoi, NganhToHop, Nganh, ThiSinh) → load 1 lần vào HashMap.
     * Dữ liệu lớn (NguyenVong, DiemCong) → load all nhưng index bằng HashMap.
     */
    public String runXetTuyen(XetTuyenEngine.ProgressCallback callback) {
        try {
            if (callback != null) callback.update(0, 0, "Đang tải dữ liệu...");

            // ── Pre-load tất cả dữ liệu vào HashMap ──
            List<NguyenVongXetTuyen> allNV = dao.getAll();
            if (allNV.isEmpty()) return "Error: Khong co nguyen vong nao de xet!";

            if (callback != null) callback.update(0, allNV.size(), "Đang tải điểm thi...");

            // DiemThi: key=cccd → List<DiemThiXetTuyen> (1 TS có nhiều PT)
            Map<String, List<DiemThiXetTuyen>> diemThiMap = new HashMap<>();
            for (DiemThiXetTuyen dt : diemThiDAO.getAll()) {
                diemThiMap.computeIfAbsent(dt.getCccd(), k -> new ArrayList<>()).add(dt);
            }

            if (callback != null) callback.update(0, allNV.size(), "Đang tải tổ hợp ngành...");

            // NganhToHop: key=maNganh → List<NganhToHop>
            Map<String, List<NganhToHop>> nganhToHopMap = new HashMap<>();
            List<NganhToHop> allNTH = nganhToHopDAO.getAll();
            if (allNTH != null) {
                for (NganhToHop nth : allNTH) {
                    nganhToHopMap.computeIfAbsent(nth.getMaNganh(), k -> new ArrayList<>()).add(nth);
                }
            }

            if (callback != null) callback.update(0, allNV.size(), "Đang tải điểm cộng...");

            // DiemCong: key=cccd_manganh_tohop (không có phuongThuc)
            Map<String, DiemCongXetTuyen> diemCongMap = new HashMap<>();
            for (DiemCongXetTuyen dc : diemCongDAO.getAll()) {
                String key = nvStr(dc.getCccd()) + "_" + nvStr(dc.getMaNganh()) + "_" + nvStr(dc.getMaToHop());
                diemCongMap.put(key, dc);
            }

            if (callback != null) callback.update(0, allNV.size(), "Đang tải thí sinh...");

            // ThiSinh: key=cccd
            Map<String, ThiSinh> thiSinhMap = new HashMap<>();
            for (ThiSinh ts : thiSinhDAO.getAll()) {
                if (ts.getCccd() != null) thiSinhMap.put(ts.getCccd(), ts);
            }

            // Nganh: key=maNganh
            Map<String, Nganh> nganhMap = new HashMap<>();
            List<Nganh> allNganh = nganhDAO.getAll();
            if (allNganh != null) {
                for (Nganh n : allNganh) nganhMap.put(n.getMaNganh(), n);
            }

            // BangQuyDoi: key=phuongThuc_toHop → List<BangQuyDoi>
            Map<String, List<BangQuyDoi>> bqdMap = new HashMap<>();
            List<BangQuyDoi> allBQD = bqdBUS.getAll();
            if (allBQD != null) {
                for (BangQuyDoi bqd : allBQD) {
                    String key = nvStr(bqd.getPhuongThuc()) + "_" + nvStr(bqd.getToHop());
                    bqdMap.computeIfAbsent(key, k -> new ArrayList<>()).add(bqd);
                }
            }

            if (callback != null) callback.update(0, allNV.size(), "Bắt đầu tính điểm...");

            // ── Chạy engine ──
            List<NguyenVongXetTuyen> result = XetTuyenEngine.runAll(
                    allNV, diemThiMap, nganhToHopMap, diemCongMap,
                    thiSinhMap, nganhMap, bqdMap, callback);

            // ── Batch update kết quả vào DB ──
            if (callback != null) callback.update(result.size(), result.size(), "Đang lưu kết quả...");

            // Chia batch 5000 để update
            int batchSize = 5000;
            for (int i = 0; i < result.size(); i += batchSize) {
                List<NguyenVongXetTuyen> batch = result.subList(i, Math.min(i + batchSize, result.size()));
                boolean ok = dao.batchUpdate(batch);
                if (!ok) return "Error: Loi khi luu ket qua xuong DB (batch " + (i / batchSize + 1) + ")!";
            }

            // ── Cập nhật điểm trúng tuyển + số lượng theo phương thức vào ngành ──
            Map<String, List<NguyenVongXetTuyen>> byNganh = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_TRUNG_TUYEN.equals(nv.getKetQua()))
                    .collect(java.util.stream.Collectors.groupingBy(NguyenVongXetTuyen::getMaNganh));

            for (Map.Entry<String, List<NguyenVongXetTuyen>> entry : byNganh.entrySet()) {
                List<NguyenVongXetTuyen> trungTuyenList = entry.getValue();
                double minDXT = trungTuyenList.stream()
                        .mapToDouble(NguyenVongXetTuyen::getDiemXetTuyen)
                        .min().orElse(0);
                int slDgnl = (int) trungTuyenList.stream()
                        .filter(nv -> "DGNL".equals(nv.getPhuongThuc()) || "2".equals(nv.getPhuongThuc())).count();
                int slVsat = (int) trungTuyenList.stream()
                        .filter(nv -> "VSAT".equals(nv.getPhuongThuc()) || "3".equals(nv.getPhuongThuc())).count();
                int slThpt = (int) trungTuyenList.stream()
                        .filter(nv -> "THPT".equals(nv.getPhuongThuc()) || "4".equals(nv.getPhuongThuc())).count();
                nganhDAO.updateDiemTrungTuyen(entry.getKey(), minDXT,
                        trungTuyenList.size(), slDgnl, slVsat, slThpt);
            }

            // ── Thống kê ──
            long trungTuyen = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_TRUNG_TUYEN.equals(nv.getKetQua())).count();
            long truot = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_TRUOT_NGANH.equals(nv.getKetQua())
                               || XetTuyenEngine.KQ_DUOI_SAN.equals(nv.getKetQua())).count();
            long khongXet = result.stream()
                    .filter(nv -> XetTuyenEngine.KQ_KHONG_XET.equals(nv.getKetQua())).count();

            return "Success|Tong NV: " + result.size()
                 + " | Trung tuyen: " + trungTuyen
                 + " | Truot: " + truot
                 + " | Khong xet: " + khongXet;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    private String nvStr(String s) { return s != null ? s : ""; }

    // ── IMPORT EXCEL ─────────────────────────────────────
    public interface ProgressCallback {
        void onProgress(int processed, int success, String message);
    }

    public void importFromExcel(File file, ProgressCallback callback) {
        int batchSize = 5000;
        List<NguyenVongXetTuyen> batch = new ArrayList<>(batchSize);
        int totalProcessed = 0;
        int totalSuccess = 0;
        
        try (InputStream is = new FileInputStream(file);
             Workbook workbook = StreamingReader.builder()
                     .rowCacheSize(100)
                     .bufferSize(4096)
                     .open(is)) {
            
            for (Sheet sheet : workbook) {
                for (Row row : sheet) {
                    if (row.getRowNum() == 0) continue; // Skip header

                    try {
                        String cccd = getCellValue(row.getCell(0));
                        if (cccd == null || cccd.isBlank()) continue;
                        
                        NguyenVongXetTuyen nv = new NguyenVongXetTuyen();
                        nv.setCccd(cccd.trim());
                        nv.setMaNganh(getCellValue(row.getCell(1)));
                        
                        String ttnvStr = getCellValue(row.getCell(2));
                        if (ttnvStr != null && !ttnvStr.isBlank()) {
                            nv.setThuTuNguyenVong((int) Double.parseDouble(ttnvStr));
                        }
                        
                        String diemThStr = getCellValue(row.getCell(3));
                        if (diemThStr != null && !diemThStr.isBlank()) {
                            nv.setDiemThxt(Double.parseDouble(diemThStr));
                        }
                        
                        String diemUtStr = getCellValue(row.getCell(4));
                        if (diemUtStr != null && !diemUtStr.isBlank()) {
                            nv.setDiemUtqd(Double.parseDouble(diemUtStr));
                        }
                        
                        String diemCongStr = getCellValue(row.getCell(5));
                        if (diemCongStr != null && !diemCongStr.isBlank()) {
                            nv.setDiemCong(Double.parseDouble(diemCongStr));
                        }
                        
                        String diemXtStr = getCellValue(row.getCell(6));
                        if (diemXtStr != null && !diemXtStr.isBlank()) {
                            nv.setDiemXetTuyen(Double.parseDouble(diemXtStr));
                        }
                        
                        nv.setKetQua(getCellValue(row.getCell(7)));
                        nv.setPhuongThuc(getCellValue(row.getCell(9)));
                        nv.setToHopMon(getCellValue(row.getCell(10)));
                        
                        autoFill(nv); // Tạo keys: cccd|maNganh
                        
                        batch.add(nv);
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
            }
            
            if (!batch.isEmpty()) {
                int success = dao.batchInsert(batch);
                totalSuccess += success;
                if (callback != null) {
                    callback.onProgress(totalProcessed, totalSuccess, "Đang xử lý...");
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
