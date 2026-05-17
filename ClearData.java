import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class ClearData {
    public static void main(String[] args) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            // Xóa d? li?u các b?ng liên quan n?u c?n
            session.createMutationQuery("DELETE FROM NguyenVongXetTuyen").executeUpdate();
            session.createMutationQuery("DELETE FROM DiemThiXetTuyen").executeUpdate();
            session.createMutationQuery("DELETE FROM DiemCongXetTuyen").executeUpdate();
            
            int deletedCount = session.createMutationQuery("DELETE FROM ThiSinh").executeUpdate();
            tx.commit();
            System.out.println("DA_XOA_THANH_CONG_SO_LUONG: " + deletedCount);
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
        }
    }
}
