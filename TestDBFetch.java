import org.hibernate.Session;
import util.HibernateUtil;
import java.util.List;

public class TestDBFetch {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<?> result = session.createNativeQuery("SELECT khu_vuc, doi_tuong FROM xt_thisinhxettuyen25 LIMIT 10").list();
            for (Object row : result) {
                Object[] arr = (Object[]) row;
                System.out.println("KhuVuc: " + arr[0] + ", DoiTuong: " + arr[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
