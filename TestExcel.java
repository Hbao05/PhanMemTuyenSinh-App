import util.ExcelUtil;
import entity.ThiSinh;
import java.io.File;
import java.util.List;

public class TestExcel {
    public static void main(String[] args) {
        File f = new File("src/main/resources/data_import/thisinh_import.xlsx");
        System.out.println("File exists: " + f.exists());
        List<ThiSinh> list = ExcelUtil.readCandidateExcel(f);
        System.out.println("Size: " + list.size());
    }
}
