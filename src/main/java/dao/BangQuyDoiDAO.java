package dao;

import entity.BangQuyDoi;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class BangQuyDoiDAO {

    // ── INSERT ────────────────────────────────────────────
    public boolean insert(BangQuyDoi bqd) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(bqd);
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

    // ── UPDATE ────────────────────────────────────────────
    public boolean update(BangQuyDoi bqd) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(bqd);
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

    // ── DELETE ────────────────────────────────────────────
    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            BangQuyDoi b = session.get(BangQuyDoi.class, id);
            if (b != null) {
                session.remove(b);
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

    // ── GET BY ID ─────────────────────────────────────────
    public BangQuyDoi getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(BangQuyDoi.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── KIỂM TRA MÃ QUY ĐỔI ─────────────────────────────
    public boolean existsByMaQuyDoi(String maQuyDoi) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(b) FROM BangQuyDoi b WHERE b.maQuyDoi = :ma", Long.class)
                    .setParameter("ma", maQuyDoi).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByMaQuyDoiExcludeId(String maQuyDoi, int excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                    "SELECT count(b) FROM BangQuyDoi b WHERE b.maQuyDoi = :ma AND b.idQd <> :id", Long.class)
                    .setParameter("ma", maQuyDoi).setParameter("id", excludeId).uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── PHÂN TRANG ────────────────────────────────────────
    public List<BangQuyDoi> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<BangQuyDoi> q = session.createQuery(
                    "FROM BangQuyDoi b ORDER BY b.phuongThuc, b.toHop, b.mon", BangQuyDoi.class);
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
            return session.createQuery("SELECT count(b) FROM BangQuyDoi b", Long.class)
                    .uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<BangQuyDoi> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM BangQuyDoi b WHERE b.maQuyDoi LIKE :kw " +
                         "OR b.phuongThuc LIKE :kw OR b.toHop LIKE :kw OR b.mon LIKE :kw " +
                         "OR b.phanVi LIKE :kw ORDER BY b.phuongThuc, b.toHop, b.mon";
            Query<BangQuyDoi> q = session.createQuery(hql, BangQuyDoi.class);
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
            String hql = "SELECT count(b) FROM BangQuyDoi b WHERE b.maQuyDoi LIKE :kw " +
                         "OR b.phuongThuc LIKE :kw OR b.toHop LIKE :kw OR b.mon LIKE :kw " +
                         "OR b.phanVi LIKE :kw";
            return session.createQuery(hql, Long.class)
                    .setParameter("kw", "%" + keyword + "%").uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ── BATCH INSERT (cho Import Excel) ──────────────────
    public int batchInsert(List<BangQuyDoi> list) {
        Transaction tx = null;
        int success = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                session.persist(list.get(i));
                if (i % 50 == 0) { session.flush(); session.clear(); }
                success++;
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
        }
        return success;
    }

    // ── XÓA TẤT CẢ ──────────────────────────────────────
    public int deleteAll() {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int count = session.createMutationQuery("DELETE FROM BangQuyDoi").executeUpdate();
            tx.commit();
            return count;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return -1;
        }
    }
}
