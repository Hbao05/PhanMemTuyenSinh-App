import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class CheckDB {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/xettuyen2026?serverTimezone=UTC";
        String user = "root";
        String password = "root";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            ResultSet rs = stmt.executeQuery("SELECT count(*) FROM xt_thisinhxettuyen25");
            if (rs.next()) {
                System.out.println("JDBC_COUNT: " + rs.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
