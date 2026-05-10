import util.HibernateUtil;
import org.hibernate.Session;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import org.hibernate.jdbc.ReturningWork;

public class CheckSchema {
    public static void main(String[] args) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.doReturningWork(new ReturningWork<Void>() {
                @Override
                public Void execute(Connection connection) throws java.sql.SQLException {
                    DatabaseMetaData meta = connection.getMetaData();
                    ResultSet rs = meta.getColumns(null, null, "xt_tohop_monthi", null);
                    System.out.println("Columns in xt_tohop_monthi:");
                    while (rs.next()) {
                        System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME") + "(" + rs.getInt("COLUMN_SIZE") + ")");
                    }
                    
                    System.out.println("---");
                    rs = meta.getColumns(null, null, "xt_nganh", null);
                    System.out.println("Columns in xt_nganh:");
                    while (rs.next()) {
                        System.out.println(rs.getString("COLUMN_NAME") + " - " + rs.getString("TYPE_NAME") + "(" + rs.getInt("COLUMN_SIZE") + ")");
                    }
                    return null;
                }
            });
        }
    }
}
