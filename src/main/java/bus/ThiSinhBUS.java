package bus;

import dao.ThiSinhDAO;
import entity.ThiSinh;

import java.util.List;

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
    public int calculateTotalPages(){
        long totalCandidates = thiSinhDAO.countTotalCandidates();
        int totalPages = (int) Math.ceil((double) totalCandidates / ROWS_PER_PAGE);
        return (totalPages == 0) ? 1 : totalPages;
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
        if (candidate.getCccd().length() < 9 || candidate.getCccd().length() > 12) {
            return "Error: ID Card (CCCD) must be between 9 and 12 characters!";
        }
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

    public String importCandidates(List<ThiSinh> importList) {
        if (importList == null || importList.isEmpty()) {
            return "Error: Import list is empty or invalid data!";
        }

        int successCount = 0;
        int failureCount = 0;

        for (ThiSinh candidate : importList) {
            String cccd = candidate.getCccd();
            if (cccd != null && !cccd.trim().isEmpty() && !thiSinhDAO.checkCccdExists(cccd)) {
                if (thiSinhDAO.insert(candidate)) {
                    successCount++;
                } else {
                    failureCount++;
                }
            } else {
                failureCount++;
            }
        }

        return "Import completed! Success: " + successCount + " rows. Failed/Skipped: " + failureCount + " rows.";
    }
}
