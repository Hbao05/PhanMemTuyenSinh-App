package dao;

import entity.DiemCongXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class DiemCongDAO {

    public boolean insert(DiemCongXetTuyen dc) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(dc);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DiemCongXetTuyen dc) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(dc);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            DiemCongXetTuyen dc = session.get(DiemCongXetTuyen.class, id);
            if (dc != null) {
                session.remove(dc);
                tx.commit();
                return true;
            }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public DiemCongXetTuyen getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM DiemCongXetTuyen d LEFT JOIN FETCH d.thiSinh WHERE d.idDiemCong = :id",
                    DiemCongXetTuyen.class)
                    .setParameter("id", id).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean existsByDcKeys(String dcKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(d) FROM DiemCongXetTuyen d WHERE d.dcKeys = :k", Long.class)
                    .setParameter("k", dcKeys).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByDcKeysExcludeId(String dcKeys, int excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(d) FROM DiemCongXetTuyen d WHERE d.dcKeys = :k AND d.idDiemCong <> :id",
                    Long.class)
                    .setParameter("k", dcKeys).setParameter("id", excludeId).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<DiemCongXetTuyen> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<DiemCongXetTuyen> q = session.createQuery(
                    "FROM DiemCongXetTuyen d LEFT JOIN FETCH d.thiSinh ORDER BY d.cccd, d.maNganh",
                    DiemCongXetTuyen.class);
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public long countTotal() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("SELECT count(d) FROM DiemCongXetTuyen d", Long.class)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public List<DiemCongXetTuyen> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM DiemCongXetTuyen d LEFT JOIN FETCH d.thiSinh " +
                         "WHERE d.cccd LIKE :kw OR d.maNganh LIKE :kw OR d.maToHop LIKE :kw " +
                         "ORDER BY d.cccd, d.maNganh";
            Query<DiemCongXetTuyen> q = session.createQuery(hql, DiemCongXetTuyen.class);
            q.setParameter("kw", "%" + keyword + "%");
            q.setFirstResult(offset);
            q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public long countSearch(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(d) FROM DiemCongXetTuyen d " +
                         "WHERE d.cccd LIKE :kw OR d.maNganh LIKE :kw OR d.maToHop LIKE :kw";
            return session.createQuery(hql, Long.class)
                    .setParameter("kw", "%" + keyword + "%").uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
