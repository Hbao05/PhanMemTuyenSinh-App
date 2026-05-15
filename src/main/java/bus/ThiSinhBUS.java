package bus;

import dao.ThiSinhDAO;
import entity.ThiSinh;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ThiSinhBUS {
    private final ThiSinhDAO thiSinhDAO;
    private static final int ROWS_PER_PAGE = 20;

    public ThiSinhBUS(){
        this.thiSinhDAO = new ThiSinhDAO();
    }
    public List<ThiSinh> getList(int currentPage){
        if( currentPage < 1) currentPage=1;
        int offset = (currentPage-1)*ROWS_PER_PAGE;
        return thiSinhDAO.getPaginatedList(offset,ROWS_PER_PAGE);
    }
    public ThiSinh getCandidate(int id) {
        return thiSinhDAO.getById(id);
    }
    public int calculateTotalPages(){
        long totalCandidates = thiSinhDAO.countTotalCandidates();
        int totalPages = (int) Math.ceil((double) totalCandidates / ROWS_PER_PAGE);
        return (totalPages == 0) ? 1 : totalPages;
    }
    public long getTotalCount() {
        return thiSinhDAO.countTotalCandidates();
    }
    public long getSearchCount(String keyword) {
        if (keyword == null) keyword = "";
        return thiSinhDAO.countSearchCandidates(keyword.trim());
    }
    public List<ThiSinh> search(int currentPage,String keywork){
        if (currentPage<1) currentPage =1;
        if (keywork == null) keywork = "";
        int offset = (currentPage-1)*ROWS_PER_PAGE;
        return thiSinhDAO.searchCandidates(offset,ROWS_PER_PAGE,keywork.trim());
    }
    public int calculateSearchTotalPages(String keyword) {
        if (keyword == null) keyword = "";
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
//        if (candidate.getCccd().length() < 9 || candidate.getCccd().length() > 12) {
//            return "Error: ID Card (CCCD) must be between 9 and 12 characters!";
//        }
        if (candidate.getTen() == null || candidate.getTen().trim().isEmpty()) {
            return "Error: Candidate's first name cannot be empty!";
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
        // Kiểm tra CCCD đã tồn tại chưa
        if (thiSinhDAO.checkCccdExists(candidate.getCccd().trim())) {
            return "Error: Số CCCD \"" + candidate.getCccd() + "\" đã tồn tại trong hệ thống!";
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

    public String importCandidates(List<ThiSinh> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Lỗi: Danh sách import trống hoặc file Excel không có dữ liệu!";
        }

        // 1. TẢI DỮ LIỆU CACHE: Kéo toàn bộ CCCD từ Database lên RAM (chỉ mất 1 câu truy vấn)
        Set<String> existingCccds = thiSinhDAO.getAllCccd();

        // 2. CHUẨN BỊ LÔ DỮ LIỆU SẠCH
        List<ThiSinh> validCandidates = new ArrayList<>();
        int duplicateCount = 0;

        for (ThiSinh candidate : importList) {
            String cccd = candidate.getCccd();

            // Nếu CCCD trống thì bỏ qua ngay lập tức
            if (cccd == null || cccd.trim().isEmpty()) {
                duplicateCount++;
                continue;
            }

            // Kiểm tra trùng lặp siêu tốc bằng HashSet.contains()
            if (existingCccds.contains(cccd)) {
                duplicateCount++; // Đã có trong DB -> Bỏ qua
            } else {
                validCandidates.add(candidate); // Hợp lệ -> Đưa vào danh sách chờ Import

                // QUAN TRỌNG: Phải thêm luôn CCCD này vào HashSet
                // Để phòng trường hợp trong chính file Excel có 2 dòng trùng CCCD với nhau!
                existingCccds.add(cccd);
            }
        }

        // 3. TIẾN HÀNH LƯU BATCH
        if (validCandidates.isEmpty()) {
            return "Import hoàn tất! \nKhông có dữ liệu mới (Tất cả " + duplicateCount + " dòng đều bị trùng CCCD hoặc lỗi).";
        }

        // Gửi lô dữ liệu sạch xuống DAO để Insert
        int successCount = thiSinhDAO.insertBatch(validCandidates);
        int failedToInsertCount = validCandidates.size() - successCount;

        // 4. TRẢ VỀ BÁO CÁO KẾT QUẢ
        return String.format("Import hoàn tất!\n- Thêm mới thành công: %d thí sinh.\n- Bỏ qua (Trùng CCCD/Lỗi): %d dòng.\n- Lỗi DB không thể lưu: %d dòng.",
                successCount, duplicateCount, failedToInsertCount);
    }

    public List<Object[]> countByDoiTuong() {
        return thiSinhDAO.countByDoiTuong();
    }

    public List<Object[]> countByKhuVuc() {
        return thiSinhDAO.countByKhuVuc();
    }
}
