import dao.NganhDAO;
import entity.Nganh;
import util.ExcelUtil;
import java.io.File;
import java.util.List;

public class TestDB {
    public static void main(String[] args) {
        File f = new File("src/main/resources/data_import/nganh_import.xlsx");
        List<Nganh> list = ExcelUtil.readNganhExcel(f);
        System.out.println("Read " + list.size() + " items.");
        if (!list.isEmpty()) {
            NganhDAO dao = new NganhDAO();
            List<Nganh> existing = dao.getAll();
            System.out.println("Existing Nganh count: " + (existing != null ? existing.size() : "null"));
            
            Nganh n = list.get(0);
            boolean exists = dao.existsByMaNganh(n.getMaNganh());
            System.out.println("Nganh " + n.getMaNganh() + " exists? " + exists);
            
            if (!exists) {
                System.out.println("Trying to insert...");
                boolean ok = dao.insert(n);
                System.out.println("Insert result: " + ok);
            } else {
                System.out.println("Already exists in DB!");
            }
        }
    }
}
