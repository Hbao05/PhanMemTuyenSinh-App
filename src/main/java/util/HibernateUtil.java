package util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Tự động đọc file hibernate.cfg.xml và xây dựng nhà máy
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Khởi tạo SessionFactory thất bại: " + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    // Hàm này sẽ được gọi ở mọi nơi khi cần lấy kết nối Database
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        getSessionFactory().close();
    }
}
