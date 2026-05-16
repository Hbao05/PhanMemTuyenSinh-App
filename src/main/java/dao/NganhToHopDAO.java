package dao;

import entity.NganhToHop;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class NganhToHopDAO {

    public boolean insert(NganhToHop item) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(item);
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

    public boolean update(NganhToHop item) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(item);
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
            NganhToHop item = session.get(NganhToHop.class, id);
            if (item != null) {
                session.remove(item);
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

    public NganhToHop getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(NganhToHop.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<NganhToHop> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<NganhToHop> q = session.createQuery(
                "FROM NganhToHop n ORDER BY n.maNganh, n.maToHop", NganhToHop.class);
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
            return session.createQuery("SELECT count(n) FROM NganhToHop n", Long.class).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<NganhToHop> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM NganhToHop n WHERE n.maNganh LIKE :kw OR n.maToHop LIKE :kw OR n.tbKeys LIKE :kw ORDER BY n.maNganh, n.maToHop";
            Query<NganhToHop> q = session.createQuery(hql, NganhToHop.class);
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
            String hql = "SELECT count(n) FROM NganhToHop n WHERE n.maNganh LIKE :kw OR n.maToHop LIKE :kw OR n.tbKeys LIKE :kw";
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("kw", "%" + keyword + "%");
            return q.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean existsByTbKeys(String tbKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(n) FROM NganhToHop n WHERE n.tbKeys = :key";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("key", tbKeys).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<NganhToHop> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM NganhToHop n ORDER BY n.maNganh, n.maToHop", NganhToHop.class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<NganhToHop> getByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NganhToHop n WHERE n.maNganh = :mn ORDER BY n.maToHop", NganhToHop.class)
                    .setParameter("mn", maNganh).list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public NganhToHop getByKeys(String maNganh, String maToHop) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NganhToHop n WHERE n.maNganh = :mn AND n.maToHop = :th", NganhToHop.class)
                    .setParameter("mn", maNganh).setParameter("th", maToHop).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean deleteAll() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery("DELETE FROM NganhToHop").executeUpdate();
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
