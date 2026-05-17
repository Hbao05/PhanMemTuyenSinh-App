package dao;

import entity.NguyenVongXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;

public class NguyenVongDAO {

    public boolean insert(NguyenVongXetTuyen nv) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.persist(nv);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(NguyenVongXetTuyen nv) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.merge(nv);
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
            NguyenVongXetTuyen nv = session.get(NguyenVongXetTuyen.class, id);
            if (nv != null) { session.remove(nv); tx.commit(); return true; }
            return false;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    public NguyenVongXetTuyen getById(int id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NguyenVongXetTuyen nv LEFT JOIN FETCH nv.thiSinh LEFT JOIN FETCH nv.nganh WHERE nv.idNv = :id",
                    NguyenVongXetTuyen.class)
                    .setParameter("id", id).uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return null; }
    }

    public boolean existsByNvKeys(String nvKeys) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long cnt = session.createQuery(
                    "SELECT count(n) FROM NguyenVongXetTuyen n WHERE n.nvKeys = :k", Long.class)
                    .setParameter("k", nvKeys).uniqueResult();
            return cnt != null && cnt > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean existsByNvKeysExcludeId(String nvKeys, int excludeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long cnt = session.createQuery(
                    "SELECT count(n) FROM NguyenVongXetTuyen n WHERE n.nvKeys = :k AND n.idNv <> :id",
                    Long.class)
                    .setParameter("k", nvKeys).setParameter("id", excludeId).uniqueResult();
            return cnt != null && cnt > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── PHÂN TRANG với filter ─────────────────────────────
    public List<NguyenVongXetTuyen> getPaginatedList(int offset, int limit,
                                                      String filterNganh, String filterKetQua) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder(
                "FROM NguyenVongXetTuyen nv LEFT JOIN FETCH nv.thiSinh LEFT JOIN FETCH nv.nganh WHERE 1=1");
            if (filterNganh != null && !filterNganh.isEmpty()) hql.append(" AND nv.maNganh = :mn");
            if (filterKetQua != null && !filterKetQua.isEmpty()) hql.append(" AND nv.ketQua = :kq");
            hql.append(" ORDER BY nv.cccd, nv.thuTuNguyenVong");
            Query<NguyenVongXetTuyen> q = session.createQuery(hql.toString(), NguyenVongXetTuyen.class);
            if (filterNganh != null && !filterNganh.isEmpty()) q.setParameter("mn", filterNganh);
            if (filterKetQua != null && !filterKetQua.isEmpty()) q.setParameter("kq", filterKetQua);
            q.setFirstResult(offset); q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    public long countList(String filterNganh, String filterKetQua) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            StringBuilder hql = new StringBuilder(
                "SELECT count(nv) FROM NguyenVongXetTuyen nv WHERE 1=1");
            if (filterNganh != null && !filterNganh.isEmpty()) hql.append(" AND nv.maNganh = :mn");
            if (filterKetQua != null && !filterKetQua.isEmpty()) hql.append(" AND nv.ketQua = :kq");
            Query<Long> q = session.createQuery(hql.toString(), Long.class);
            if (filterNganh != null && !filterNganh.isEmpty()) q.setParameter("mn", filterNganh);
            if (filterKetQua != null && !filterKetQua.isEmpty()) q.setParameter("kq", filterKetQua);
            return q.uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── TÌM KIẾM ─────────────────────────────────────────
    public List<NguyenVongXetTuyen> search(int offset, int limit, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM NguyenVongXetTuyen nv LEFT JOIN FETCH nv.thiSinh LEFT JOIN FETCH nv.nganh " +
                         "WHERE nv.cccd LIKE :kw OR nv.maNganh LIKE :kw ORDER BY nv.cccd, nv.thuTuNguyenVong";
            Query<NguyenVongXetTuyen> q = session.createQuery(hql, NguyenVongXetTuyen.class);
            q.setParameter("kw", "%" + keyword + "%");
            q.setFirstResult(offset); q.setMaxResults(limit);
            return q.list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    public long countSearch(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT count(nv) FROM NguyenVongXetTuyen nv WHERE nv.cccd LIKE :kw OR nv.maNganh LIKE :kw",
                    Long.class)
                    .setParameter("kw", "%" + keyword + "%").uniqueResult();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── LẤY TẤT CẢ (cho engine xét tuyển) ───────────────
    public List<NguyenVongXetTuyen> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NguyenVongXetTuyen nv ORDER BY nv.cccd, nv.thuTuNguyenVong",
                    NguyenVongXetTuyen.class).list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    // ── LẤY THEO CCCD (xem kết quả của một thí sinh) ────
    public List<NguyenVongXetTuyen> getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "FROM NguyenVongXetTuyen nv LEFT JOIN FETCH nv.nganh " +
                    "WHERE nv.cccd = :cccd ORDER BY nv.thuTuNguyenVong",
                    NguyenVongXetTuyen.class)
                    .setParameter("cccd", cccd).list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }

    // ── BATCH UPDATE sau khi engine chạy ─────────────────
    public boolean batchUpdate(List<NguyenVongXetTuyen> list) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                session.merge(list.get(i));
                if (i % 50 == 0) { session.flush(); session.clear(); }
            }
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }

    // ── BATCH INSERT (Cho Import Excel) ──────────────────
    public int batchInsert(List<NguyenVongXetTuyen> list) {
        Transaction tx = null;
        int successCount = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                session.persist(list.get(i));
                successCount++;
                if (i > 0 && i % 50 == 0) { session.flush(); session.clear(); }
            }
            tx.commit();
            return successCount;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            System.err.println("Lỗi batch insert. Chuyển sang insert từng dòng (fallback)...");
            return fallbackSingleInsert(list);
        }
    }

    private int fallbackSingleInsert(List<NguyenVongXetTuyen> list) {
        int successCount = 0;
        for (NguyenVongXetTuyen nv : list) {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                // Khi batchInsert fail và rollback, các entity đã được persist trước lỗi
                // vẫn bị giữ lại ID đã generate. Ta cần reset ID về 0 để Hibernate hiểu đây là entity mới.
                nv.setIdNv(0);
                session.persist(nv);
                tx.commit();
                successCount++;
            } catch (Exception e) {
                if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) {}
                String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
                System.err.println("Bỏ qua dòng lỗi (CCCD: " + nv.getCccd() + ", Ngành: " + nv.getMaNganh() + ") - Lý do: " + reason);
            }
        }
        return successCount;
    }
}
