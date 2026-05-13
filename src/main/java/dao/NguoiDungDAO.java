package dao;

import entity.NguoiDung;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class NguoiDungDAO {

    public boolean insert(NguoiDung nd) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(nd);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NguoiDung nd) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(nd);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            NguoiDung nd = session.get(NguoiDung.class, id);
            if (nd == null) return false;
            session.remove(nd);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    public NguoiDung getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(NguoiDung.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public NguoiDung findByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NguoiDung n WHERE n.username = :u", NguoiDung.class)
                    .setParameter("u", username)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existsByUsername(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(n) FROM NguoiDung n WHERE n.username = :u", Long.class)
                    .setParameter("u", username)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<NguoiDung> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NguoiDung n ORDER BY n.id", NguoiDung.class)
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(n) FROM NguoiDung n", Long.class)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<NguoiDung> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM NguoiDung n WHERE n.username LIKE :kw OR n.hoTen LIKE :kw ORDER BY n.id";
            return session.createQuery(hql, NguoiDung.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .setFirstResult(offset)
                    .setMaxResults(limit)
                    .list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countSearch(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(n) FROM NguoiDung n WHERE n.username LIKE :kw OR n.hoTen LIKE :kw",
                    Long.class)
                    .setParameter("kw", "%" + keyword + "%")
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
