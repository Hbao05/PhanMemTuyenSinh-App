import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class HardDelete {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/xettuyen2026?serverTimezone=UTC";
        String user = "root";
        String password = "root";
        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement()) {
             
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            int del = stmt.executeUpdate("DELETE FROM xt_thisinhxettuyen25");
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
            
            System.out.println("HARD_DELETED: " + del);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
