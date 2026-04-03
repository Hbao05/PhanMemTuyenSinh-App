import entity.ToHopMonThi;
import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("Đang kết nối cơ sở dữ liệu...");

        // Lấy 1 phiên làm việc (Session) từ "Người quản đốc" util.HibernateUtil
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = null;

            try {
                tx = session.beginTransaction(); // Bắt đầu giao dịch

                // 1. Tạo một đối tượng Tổ hợp môn mới
                ToHopMonThi toHop = new ToHopMonThi();
                toHop.setMaToHop("A00");
                toHop.setMon1("Toán");
                toHop.setMon2("Vật lý");
                toHop.setMon3("Hóa học");
                toHop.setTenToHop("Toán, Lý, Hóa");

                // 2. Lưu xuống Database (Dùng persist cho Hibernate 6)
                session.persist(toHop);

                tx.commit(); // 3. Chốt giao dịch, ghi thẳng vào ổ cứng
                System.out.println("Tuyệt vời! Đã lưu thành công tổ hợp môn: " + toHop.getMaToHop());

            } catch (Exception e) {
                if (tx != null) tx.rollback(); // Nếu có lỗi thì hủy bỏ mọi thao tác
                System.err.println("Có lỗi xảy ra khi lưu dữ liệu!");
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Tắt nhà máy Hibernate khi tắt ứng dụng
            HibernateUtil.shutdown();
        }
    }
}