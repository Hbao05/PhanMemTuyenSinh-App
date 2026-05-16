package dao;

import entity.ToHopMonThi;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class ToHopMonThiDAO {

    public boolean insert(ToHopMonThi toHop) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(toHop);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(ToHopMonThi toHop) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(toHop);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ToHopMonThi t = session.get(ToHopMonThi.class, id);
            if (t != null) {
                session.remove(t);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }

    public ToHopMonThi getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(ToHopMonThi.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ToHopMonThi> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<ToHopMonThi> q = session.createQuery("FROM ToHopMonThi t ORDER BY t.maToHop", ToHopMonThi.class);
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(t) FROM ToHopMonThi t", Long.class).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<ToHopMonThi> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ToHopMonThi t WHERE t.maToHop LIKE :kw OR t.tenToHop LIKE :kw ORDER BY t.maToHop";
            Query<ToHopMonThi> q = session.createQuery(hql, ToHopMonThi.class);
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
            String hql = "SELECT count(t) FROM ToHopMonThi t WHERE t.maToHop LIKE :kw OR t.tenToHop LIKE :kw";
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean existsByMaToHop(String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(t) FROM ToHopMonThi t WHERE t.maToHop = :ma";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("ma", maToHop).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<ToHopMonThi> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ToHopMonThi t ORDER BY t.maToHop", ToHopMonThi.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public boolean deleteAll() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM ToHopMonThi").executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        }
    }
}
