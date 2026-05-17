import org.hibernate.Session;
import org.hibernate.Transaction;
import util.HibernateUtil;

public class ClearData {
    public static void main(String[] args) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            try { session.createMutationQuery("DELETE FROM NguyenVongXetTuyen").executeUpdate(); } catch (Exception e) {}
            try { session.createMutationQuery("DELETE FROM DiemThiXetTuyen").executeUpdate(); } catch (Exception e) {}
            try { session.createMutationQuery("DELETE FROM DiemCongXetTuyen").executeUpdate(); } catch (Exception e) {}
            
            int deletedCount = session.createMutationQuery("DELETE FROM ThiSinh").executeUpdate();
            tx.commit();
            System.out.println("DA_XOA_THANH_CONG_SO_LUONG: " + deletedCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
