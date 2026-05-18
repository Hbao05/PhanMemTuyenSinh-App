import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DropConstraint {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/xettuyen2026?serverTimezone=UTC";
        String user = "root";
        String password = "root";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            stmt.executeUpdate("ALTER TABLE xt_thisinhxettuyen25 DROP FOREIGN KEY FKjtv6kk0jx1d3n53yousyn3os3");
            System.out.println("DROPPED_CONSTRAINT_SUCCESSFULLY");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
