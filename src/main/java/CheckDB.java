import dao.NganhDAO;
import dao.ToHopMonThiDAO;
import entity.Nganh;
import entity.ToHopMonThi;
import java.util.List;

public class CheckDB {
    public static void main(String[] args) {
        try {
            NganhDAO nDao = new NganhDAO();
            List<Nganh> nList = nDao.getAll();
            System.out.println("Nganh in DB: " + (nList != null ? nList.size() : "null"));
            if (nList != null && !nList.isEmpty()) {
                System.out.println("First Nganh: " + nList.get(0).getMaNganh() + " - " + nList.get(0).getTenNganh());
            }

            ToHopMonThiDAO tDao = new ToHopMonThiDAO();
            List<ToHopMonThi> tList = tDao.getAll();
            System.out.println("ToHopMonThi in DB: " + (tList != null ? tList.size() : "null"));
            if (tList != null && !tList.isEmpty()) {
                System.out.println("First ToHop: " + tList.get(0).getMaToHop());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
