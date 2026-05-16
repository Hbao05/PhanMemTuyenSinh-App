import util.HibernateUtil;
import org.hibernate.Session;

public class CreateTables {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("Hibernate bootstrapped and tables should be created/updated.");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
