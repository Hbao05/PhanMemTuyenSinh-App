package dao;

import entity.Nganh;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class NganhDAO {

    // ── INSERT ────────────────────────────────────────────
    public boolean insert(Nganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(nganh);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    // ── UPDATE ────────────────────────────────────────────
    public boolean update(Nganh nganh) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(nganh);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    // ── DELETE ────────────────────────────────────────────
    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            Nganh n = session.get(Nganh.class, id);
            if (n != null) {
                session.remove(n);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }

    // ── GET BY ID ─────────────────────────────────────────
    public Nganh getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Nganh.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<Nganh> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Nganh> q = session.createQuery("FROM Nganh n ORDER BY n.maNganh", Nganh.class);
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── ĐẾM TỔNG ─────────────────────────────────────────
    public long countTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(n) FROM Nganh n", Long.class).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<Nganh> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Nganh n WHERE n.maNganh LIKE :kw OR n.tenNganh LIKE :kw ORDER BY n.maNganh";
            Query<Nganh> q = session.createQuery(hql, Nganh.class);
            q.setParameter("kw", "%" + keyword + "%");
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countSearch(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(n) FROM Nganh n WHERE n.maNganh LIKE :kw OR n.tenNganh LIKE :kw";
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ── KIỂM TRA MÃ NGÀNH ĐÃ TỒN TẠI ────────────────────
    public boolean existsByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(n) FROM Nganh n WHERE n.maNganh = :ma";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("ma", maNganh).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── LẤY TẤT CẢ (cho ComboBox) ────────────────────────
    public List<Nganh> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Nganh n ORDER BY n.maNganh", Nganh.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
