package dao;

import entity.DiemThiXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiemThiDAO {

    public boolean insert(DiemThiXetTuyen dt) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(dt);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(DiemThiXetTuyen dt) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(dt);
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
            DiemThiXetTuyen dt = session.get(DiemThiXetTuyen.class, id);
            if (dt != null) { session.remove(dt); tx.commit(); return true; }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public DiemThiXetTuyen getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(DiemThiXetTuyen.class, id);
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public boolean existsByCccdAndPhuongThuc(String cccd, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT count(d) FROM DiemThiXetTuyen d " +
                                    "WHERE d.cccd = :cccd AND d.phuongThuc = :pt", Long.class)
                    .setParameter("cccd", cccd)
                    .setParameter("pt", phuongThuc)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean existsByCccdAndPhuongThucExcludeId(String cccd, String phuongThuc, int excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long count = session.createQuery(
                            "SELECT count(d) FROM DiemThiXetTuyen d " +
                                    "WHERE d.cccd = :cccd AND d.phuongThuc = :pt AND d.idDiemThi <> :id",
                            Long.class)
                    .setParameter("cccd", cccd)
                    .setParameter("pt", phuongThuc)
                    .setParameter("id", excludeId)
                    .uniqueResult();
            return count != null && count > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── PHÂN TRANG TẤT CẢ ────────────────────────────────
    public List<DiemThiXetTuyen> getPaginatedList(int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // SỬA TẠI ĐÂY: Thay đổi trọng số THPT lên trước, và đổi idDiemThi thành ASC (Tăng dần)
            String hql = "FROM DiemThiXetTuyen d ORDER BY CASE WHEN d.phuongThuc = '4' THEN 1 ELSE 2 END ASC, d.idDiemThi ASC";
            Query<DiemThiXetTuyen> q = session.createQuery(hql, DiemThiXetTuyen.class);
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
            return session.createQuery("SELECT count(d) FROM DiemThiXetTuyen d", Long.class).uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── PHÂN TRANG THEO PHƯƠNG THỨC ──────────────────────
    public List<DiemThiXetTuyen> getPaginatedListByPhuongThuc(String pt, int offset, int limit) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<DiemThiXetTuyen> q = session.createQuery(
                    "FROM DiemThiXetTuyen d WHERE d.phuongThuc = :pt ORDER BY d.cccd",
                    DiemThiXetTuyen.class);
            q.setParameter("pt", pt); q.setFirstResult(offset); q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    public long countByPhuongThuc(String pt) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(d) FROM DiemThiXetTuyen d WHERE d.phuongThuc = :pt", Long.class)
                    .setParameter("pt", pt).uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<DiemThiXetTuyen> search(int offset, int limit, String keyword, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String ptClause = (phuongThuc != null && !phuongThuc.isEmpty())
                    ? " AND d.phuongThuc = :pt" : "";
            String hql = "FROM DiemThiXetTuyen d WHERE (d.cccd LIKE :kw OR d.soBaoDanh LIKE :kw)" +
                         ptClause + " ORDER BY d.cccd";
            Query<DiemThiXetTuyen> q = session.createQuery(hql, DiemThiXetTuyen.class);
            q.setParameter("kw", "%" + keyword + "%");
            if (!ptClause.isEmpty()) q.setParameter("pt", phuongThuc);
            q.setFirstResult(offset); q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    public long countSearch(String keyword, String phuongThuc) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String ptClause = (phuongThuc != null && !phuongThuc.isEmpty())
                    ? " AND d.phuongThuc = :pt" : "";
            String hql = "SELECT count(d) FROM DiemThiXetTuyen d WHERE (d.cccd LIKE :kw OR d.soBaoDanh LIKE :kw)" + ptClause;
            Query<Long> q = session.createQuery(hql, Long.class);
            q.setParameter("kw", "%" + keyword + "%");
            if (!ptClause.isEmpty()) q.setParameter("pt", phuongThuc);
            return q.uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── LẤY TẤT CẢ THEO PT (cho thống kê) ───────────────
    public List<DiemThiXetTuyen> getAllByPhuongThuc(String pt) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM DiemThiXetTuyen d WHERE d.phuongThuc = :pt", DiemThiXetTuyen.class)
                    .setParameter("pt", pt).list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    // ── LẤY TẤT CẢ (cho engine xét tuyển) ───────────────
    public List<DiemThiXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM DiemThiXetTuyen d ORDER BY d.cccd", DiemThiXetTuyen.class).list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    /**
     * Lấy tất cả (cccd + phuongThuc) đang có trong DB về một lần duy nhất.
     * Dùng để check trùng phía Java, tránh N+1 query.
     */
    public Set<String> getAllExistingKeys() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Object[]> rows = session.createQuery(
                            "SELECT d.cccd, d.phuongThuc FROM DiemThiXetTuyen d", Object[].class)
                    .list();
            Set<String> keys = new HashSet<>();
            for (Object[] r : rows) keys.add(r[0] + "_" + r[1]);
            return keys;
        } catch (Exception e) { e.printStackTrace(); return new HashSet<>(); }
    }

    /**
     * Insert nhiều bản ghi trong một session + một transaction duy nhất.
     * Flush + clear định kỳ để tránh OutOfMemory với file lớn.
     */
    public int[] insertBatch(List<DiemThiXetTuyen> list) {
        int success = 0, error = 0;
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            int count = 0;
            for (DiemThiXetTuyen dt : list) {
                session.persist(dt);
                count++;
                if (count % 50 == 0) {   // flush mỗi 50 dòng
                    session.flush();
                    session.clear();
                }
            }
            tx.commit();
            success = list.size();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            error = list.size();
            success = 0;
        }
        return new int[]{success, error};
    }
}
