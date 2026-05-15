import org.hibernate.Session;
import util.HibernateUtil;

public class TestCount {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery("SELECT count(t) FROM ThiSinh t", Long.class).uniqueResult();
            System.out.println("CURRENT_COUNT_IN_DB: " + count);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
