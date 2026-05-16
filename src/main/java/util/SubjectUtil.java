package util;

public class SubjectUtil {

    public static final String[] SUBJECT_CODES = {
        "TO", "VA", "LI", "HO", "SI", "SU", "DI", "N1", "KTPL", "CNCN", "CNNN", 
        "NK1", "NK2", "NK3", "NK4", "NK5", "NK6"
    };

    /**
     * Chuyển đổi mã môn thi thành tên môn thi đầy đủ
     * @param code Mã môn thi (ví dụ: TO, LI, HO)
     * @return Tên môn thi tương ứng (ví dụ: Toán, Vật lí, Hóa học)
     */
    public static String getSubjectName(String code) {
        if (code == null || code.trim().isEmpty()) {
            return "";
        }
        switch (code.toUpperCase().trim()) {
            case "TO":
                return "Toán";
            case "VA":
                return "Ngữ văn";
            case "LI":
                return "Vật lí";
            case "HO":
                return "Hóa học";
            case "SI":
                return "Sinh học";
            case "SU":
                return "Lịch sử";
            case "DI":
                return "Địa lí";
            case "N1":
            case "TI":
                return "Tiếng Anh";
            case "KTPL":
                return "Giáo dục KTPL";
            case "CNCN":
                return "Công nghệ (Công nghiệp)";
            case "CNNN":
                return "Công nghệ (Nông nghiệp)";
            case "NK1":
                return "Năng khiếu 1";
            case "NK2":
                return "Năng khiếu 2";
            case "NK3":
                return "Năng khiếu 3";
            case "NK4":
                return "Năng khiếu 4";
            case "NK5":
                return "Năng khiếu 5";
            case "NK6":
                return "Năng khiếu 6";
            default:
                return code;
        }
    }
}
