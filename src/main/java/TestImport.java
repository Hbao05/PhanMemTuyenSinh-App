import bus.ThiSinhBUS;
import entity.ThiSinh;
import util.ExcelUtil;
import java.io.File;
import java.util.List;

public class TestImport {
    public static void main(String[] args) {
        try {
            File f = new File("src/main/resources/data_import/thisinh_import.xlsx");
            List<ThiSinh> list = ExcelUtil.readCandidateExcel(f);
            System.out.println("Read " + list.size() + " candidates. Importing...");
            ThiSinhBUS bus = new ThiSinhBUS();
            String result = bus.importCandidates(list);
            System.out.println("Result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
