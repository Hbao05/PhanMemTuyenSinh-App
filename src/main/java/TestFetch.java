import org.hibernate.Session;
import util.HibernateUtil;

public class TestFetch {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Executing FROM ThiSinh t");
            java.util.List<?> list = session.createQuery("FROM ThiSinh t", entity.ThiSinh.class).setMaxResults(10).list();
            System.out.println("FETCHED_COUNT: " + list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
