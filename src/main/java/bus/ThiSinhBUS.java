package bus;

import dao.ThiSinhDAO;
import entity.ThiSinh;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ThiSinhBUS {
    private final ThiSinhDAO thiSinhDAO;
    private static final int ROWS_PER_PAGE = 20;

    /** Kết quả xử lý một (hoặc gộp nhiều) lô import thí sinh. */
    public static final class ImportCandidateResult {
        private int successCount;
        private int duplicateCount;
        private int failedToInsertCount;

        public ImportCandidateResult() {
        }

        public ImportCandidateResult(int successCount, int duplicateCount, int failedToInsertCount) {
            this.successCount = successCount;
            this.duplicateCount = duplicateCount;
            this.failedToInsertCount = failedToInsertCount;
        }

        public void merge(ImportCandidateResult other) {
            if (other == null) {
                return;
            }
            this.successCount += other.successCount;
            this.duplicateCount += other.duplicateCount;
            this.failedToInsertCount += other.failedToInsertCount;
        }

        public boolean isEmptyTotals() {
            return successCount == 0 && duplicateCount == 0 && failedToInsertCount == 0;
        }

        public String formatMessage() {
            if (successCount == 0 && duplicateCount > 0 && failedToInsertCount == 0) {
                return "Import hoàn tất! \nKhông có dữ liệu mới (Tất cả " + duplicateCount
                        + " dòng đều bị trùng CCCD hoặc lỗi).";
            }
            return String.format(
                    "Import hoàn tất!\n- Thêm mới thành công: %d thí sinh.\n- Bỏ qua (Trùng CCCD/Lỗi): %d dòng.\n- Lỗi DB không thể lưu: %d dòng.",
                    successCount, duplicateCount, failedToInsertCount);
        }
    }

    public ThiSinhBUS() {
        this.thiSinhDAO = new ThiSinhDAO();
    }

    public List<ThiSinh> getList(int currentPage) {
        if (currentPage < 1)
            currentPage = 1;
        int offset = (currentPage - 1) * ROWS_PER_PAGE;
        return thiSinhDAO.getPaginatedList(offset, ROWS_PER_PAGE);
    }

    public ThiSinh getCandidate(int id) {
        return thiSinhDAO.getById(id);
    }

    public int calculateTotalPages() {
        long totalCandidates = thiSinhDAO.countTotalCandidates();
        int totalPages = (int) Math.ceil((double) totalCandidates / ROWS_PER_PAGE);
        return (totalPages == 0) ? 1 : totalPages;
    }

    public long getTotalCount() {
        return thiSinhDAO.countTotalCandidates();
    }

    public long getSearchCount(String keyword) {
        if (keyword == null)
            keyword = "";
        return thiSinhDAO.countSearchCandidates(keyword.trim());
    }

    public List<ThiSinh> search(int currentPage, String keywork) {
        if (currentPage < 1)
            currentPage = 1;
        if (keywork == null)
            keywork = "";
        int offset = (currentPage - 1) * ROWS_PER_PAGE;
        return thiSinhDAO.searchCandidates(offset, ROWS_PER_PAGE, keywork.trim());
    }

    public int calculateSearchTotalPages(String keyword) {
        if (keyword == null)
            keyword = "";
        long totalCandidates = thiSinhDAO.countSearchCandidates(keyword.trim());
        int totalPages = (int) Math.ceil((double) totalCandidates / ROWS_PER_PAGE);
        return (totalPages == 0) ? 1 : totalPages;
    }

    public String updateCandidateInfo(ThiSinh candidate) {
        if (candidate.getIdThiSinh() <= 0) {
            return "Error: Cannot identify the candidate to update!";
        }
        if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
            return "Error: ID Card (CCCD) cannot be empty!";
        }
        // if (candidate.getCccd().length() < 9 || candidate.getCccd().length() > 12) {
        // return "Error: ID Card (CCCD) must be between 9 and 12 characters!";
        // }
        if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
            return "Error: Tên thí sinh không được để trống!";
        }

        // Kiểm tra định dạng ngày sinh dd/MM/yyyy
        if (candidate.getNgaySinh() != null && !candidate.getNgaySinh().trim().isEmpty()) {
            if (!candidate.getNgaySinh().trim().matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                return "Error: Ngày sinh sai định dạng (Yêu cầu: dd/MM/yyyy)!";
            }
        }

        // Kiểm tra CCCD trùng lặp (khác ID hiện tại)
        ThiSinh existingByCccd = thiSinhDAO.getByCccd(candidate.getCccd().trim());
        if (existingByCccd != null && existingByCccd.getIdThiSinh() != candidate.getIdThiSinh()) {
            return "Error: Số CCCD \"" + candidate.getCccd() + "\" đã tồn tại cho thí sinh khác!";
        }

        // Kiểm tra SBD trùng lặp (khác ID hiện tại)
        if (candidate.getSoBaoDanh() != null && !candidate.getSoBaoDanh().trim().isEmpty()) {
            ThiSinh existingBySbd = thiSinhDAO.getBySbd(candidate.getSoBaoDanh().trim());
            if (existingBySbd != null && existingBySbd.getIdThiSinh() != candidate.getIdThiSinh()) {
                return "Error: Số Báo Danh \"" + candidate.getSoBaoDanh() + "\" đã tồn tại cho thí sinh khác!";
            }
        }

        boolean isSuccess = thiSinhDAO.update(candidate);
        if (isSuccess) {
            return "Success: Candidate information updated successfully!";
        } else {
            return "Error: Failed to update database.";
        }
    }

    public String addCandidate(ThiSinh candidate) {
        // Validation nghiệp vụ
        if (candidate.getCccd() == null || candidate.getCccd().trim().isEmpty()) {
            return "Error: Số CCCD không được để trống!";
        }
        if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
            return "Error: Tên thí sinh không được để trống!";
        }
        // Kiểm tra định dạng ngày sinh dd/MM/yyyy
        if (candidate.getNgaySinh() != null && !candidate.getNgaySinh().trim().isEmpty()) {
            if (!candidate.getNgaySinh().trim().matches("^\\d{2}/\\d{2}/\\d{4}$")) {
                return "Error: Ngày sinh sai định dạng (Yêu cầu: dd/MM/yyyy)!";
            }
        }
        // Kiểm tra CCCD đã tồn tại chưa
        if (thiSinhDAO.checkCccdExists(candidate.getCccd().trim())) {
            return "Error: Số CCCD \"" + candidate.getCccd() + "\" đã tồn tại trong hệ thống!";
        }
        // Kiểm tra SBD đã tồn tại chưa
        if (candidate.getSoBaoDanh() != null && !candidate.getSoBaoDanh().trim().isEmpty()) {
            if (thiSinhDAO.checkSbdExists(candidate.getSoBaoDanh().trim())) {
                return "Error: Số Báo Danh \"" + candidate.getSoBaoDanh() + "\" đã tồn tại trong hệ thống!";
            }
        }
        boolean isSuccess = thiSinhDAO.insert(candidate);
        return isSuccess
                ? "Success: Thêm thí sinh thành công!"
                : "Error: Không thể lưu vào cơ sở dữ liệu. Vui lòng thử lại!";
    }

    public String deleteCandidate(int id) {
        if (id <= 0) {
            return "Error: Không xác định được thí sinh cần xóa!";
        }
        boolean isSuccess = thiSinhDAO.delete(id);
        return isSuccess
                ? "Success"
                : "Error: Không thể xóa. Thí sinh có thể đã bị liên kết dữ liệu.";
    }

    public ThiSinh getByCccd(String cccd) {
        return thiSinhDAO.getByCccd(cccd);
    }

    /**
     * Snapshot CCCD trong DB — dùng làm cache cho import Excel nhiều lô (tránh
     * query lặp).
     */
    public Set<String> newImportCccdCache() {
        return thiSinhDAO.getAllCccd();
    }

    public Set<String> newImportSbdCache() {
        return thiSinhDAO.getAllSbd();
    }

    /**
     * Xử lý một lô thí sinh đọc từ Excel. Danh sách rỗng → kết quả toàn 0 (dùng khi
     * import theo lô).
     *
     * @param existingCccdCache snapshot CCCD đã có (DB + các dòng đã duyệt); bị cập
     *                          nhật khi có bản ghi mới hợp lệ trong lô.
     */
    public ImportCandidateResult importCandidatesBatch(List<ThiSinh> importList, Set<String> existingCccdCache,
            Set<String> existingSbdCache) {
        if (importList == null || importList.isEmpty()) {
            return new ImportCandidateResult();
        }
        Objects.requireNonNull(existingCccdCache, "existingCccdCache");
        Objects.requireNonNull(existingSbdCache, "existingSbdCache");

        List<ThiSinh> validCandidates = new ArrayList<>();
        int duplicateCount = 0;

        for (ThiSinh candidate : importList) {
            String cccd = candidate.getCccd();
            String sbd = candidate.getSoBaoDanh();

            if (cccd == null || cccd.trim().isEmpty()) {
                duplicateCount++;
                continue;
            }

            boolean hasSbd = sbd != null && !sbd.trim().isEmpty();

            if (existingCccdCache.contains(cccd) || (hasSbd && existingSbdCache.contains(sbd))) {
                duplicateCount++;
            } else {
                validCandidates.add(candidate);
                existingCccdCache.add(cccd);
                if (hasSbd) {
                    existingSbdCache.add(sbd);
                }
            }
        }

        if (validCandidates.isEmpty()) {
            return new ImportCandidateResult(0, duplicateCount, 0);
        }

        int successCount = thiSinhDAO.insertBatch(validCandidates);
        int failedToInsertCount = validCandidates.size() - successCount;
        return new ImportCandidateResult(successCount, duplicateCount, failedToInsertCount);
    }

    public String importCandidates(List<ThiSinh> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
        }
        return importCandidatesBatch(importList, thiSinhDAO.getAllCccd(), thiSinhDAO.getAllSbd()).formatMessage();
    }

    public List<Object[]> countByDoiTuong() {
        return thiSinhDAO.countByDoiTuong();
    }

    public List<Object[]> countByKhuVuc() {
        return thiSinhDAO.countByKhuVuc();
    }
}
