import util.HibernateUtil;
import org.hibernate.Session;
import java.sql.Connection;
import java.sql.Statement;
import org.hibernate.jdbc.Work;

public class AlterSchema {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws java.sql.SQLException {
                    try (Statement stmt = connection.createStatement()) {
                        System.out.println("Altering xt_tohop_monthi...");
                        stmt.execute("ALTER TABLE xt_tohop_monthi MODIFY mon1 VARCHAR(100)");
                        stmt.execute("ALTER TABLE xt_tohop_monthi MODIFY mon2 VARCHAR(100)");
                        stmt.execute("ALTER TABLE xt_tohop_monthi MODIFY mon3 VARCHAR(100)");
                        
                        System.out.println("Altering xt_nganh...");
                        stmt.execute("ALTER TABLE xt_nganh MODIFY n_tohopgoc VARCHAR(100)");
                        
                        System.out.println("Successfully updated database schema!");
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
