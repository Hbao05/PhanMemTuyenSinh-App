package dao;

import entity.DiemCongXetTuyen;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
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

    public int batchInsert(List<DiemCongXetTuyen> list) {
        if (list == null || list.isEmpty()) return 0;
        final int[] successCount = {0};
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.doWork(new Work() {
                @Override
                public void execute(Connection connection) throws SQLException {
                    String sql = "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, phuongthuc, diemCC, diemUtxt, diemTong, dc_keys) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                                 "ON DUPLICATE KEY UPDATE diemCC=VALUES(diemCC), diemUtxt=VALUES(diemUtxt), diemTong=VALUES(diemTong)";
                    try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                        for (DiemCongXetTuyen dc : list) {
                            pstmt.setString(1, dc.getCccd());
                            pstmt.setString(2, dc.getMaNganh());
                            pstmt.setString(3, dc.getMaToHop());
                            pstmt.setString(4, dc.getPhuongThuc());
                            if (dc.getDiemCc() != null) pstmt.setDouble(5, dc.getDiemCc());
                            else pstmt.setNull(5, java.sql.Types.DOUBLE);
                            if (dc.getDiemUtXt() != null) pstmt.setDouble(6, dc.getDiemUtXt());
                            else pstmt.setNull(6, java.sql.Types.DOUBLE);
                            if (dc.getDiemTong() != null) pstmt.setDouble(7, dc.getDiemTong());
                            else pstmt.setNull(7, java.sql.Types.DOUBLE);
                            pstmt.setString(8, dc.getDcKeys());
                            pstmt.addBatch();
                        }
                        int[] counts = pstmt.executeBatch();
                        for (int c : counts) {
                            if (c >= 0 || c == Statement.SUCCESS_NO_INFO) {
                                successCount[0]++;
                            }
                        }
                    }
                }
            });
            tx.commit();
            return successCount[0];
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            System.err.println("Lỗi batch insert Điểm Cộng. Chuyển sang insert từng dòng (fallback)...");
            return fallbackSingleInsert(list);
        }
    }

    private int fallbackSingleInsert(List<DiemCongXetTuyen> list) {
        int successCount = 0;
        for (DiemCongXetTuyen dc : list) {
            Transaction tx = null;
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                tx = session.beginTransaction();
                session.doWork(new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        String sql = "INSERT INTO xt_diemcongxetuyen (ts_cccd, manganh, matohop, phuongthuc, diemCC, diemUtxt, diemTong, dc_keys) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                                     "ON DUPLICATE KEY UPDATE diemCC=VALUES(diemCC), diemUtxt=VALUES(diemUtxt), diemTong=VALUES(diemTong)";
                        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                            pstmt.setString(1, dc.getCccd());
                            pstmt.setString(2, dc.getMaNganh());
                            pstmt.setString(3, dc.getMaToHop());
                            pstmt.setString(4, dc.getPhuongThuc());
                            if (dc.getDiemCc() != null) pstmt.setDouble(5, dc.getDiemCc());
                            else pstmt.setNull(5, java.sql.Types.DOUBLE);
                            if (dc.getDiemUtXt() != null) pstmt.setDouble(6, dc.getDiemUtXt());
                            else pstmt.setNull(6, java.sql.Types.DOUBLE);
                            if (dc.getDiemTong() != null) pstmt.setDouble(7, dc.getDiemTong());
                            else pstmt.setNull(7, java.sql.Types.DOUBLE);
                            pstmt.setString(8, dc.getDcKeys());
                            pstmt.executeUpdate();
                        }
                    }
                });
                tx.commit();
                successCount++;
            } catch (Exception e) {
                if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) {}
                System.err.println("Bỏ qua dòng lỗi (CCCD: " + dc.getCccd() + ", Ngành: " + dc.getMaNganh() + ")");
            }
        }
        return successCount;
    }
}
