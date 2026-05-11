import util.HibernateUtil;
import org.hibernate.Session;
import java.sql.Connection;
import java.sql.Statement;
import org.hibernate.jdbc.Work;

public class ClearDB {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws java.sql.SQLException {
                    try (Statement stmt = connection.createStatement()) {
                        System.out.println("Clearing xt_tohop_monthi...");
                        stmt.execute("DELETE FROM xt_tohop_monthi");
                        System.out.println("Successfully cleared!");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
