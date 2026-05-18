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
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
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
            Nganh n = session.get(Nganh.class, id);
            if (n != null) {
                // Kiểm tra xem ngành này có Tổ hợp hay Nguyện vọng nào không
                Long countToHop = session.createQuery("SELECT count(nth) FROM NganhToHop nth WHERE nth.maNganh = :ma", Long.class)
                        .setParameter("ma", n.getMaNganh())
                        .uniqueResult();
                Long countNV = session.createQuery("SELECT count(nv) FROM NguyenVongXetTuyen nv WHERE nv.maNganh = :ma", Long.class)
                        .setParameter("ma", n.getMaNganh())
                        .uniqueResult();
                        
                if ((countToHop != null && countToHop > 0) || (countNV != null && countNV > 0)) {
                    throw new RuntimeException("Không thể xóa ngành này vì đang có dữ liệu Tổ hợp hoặc Nguyện vọng tham chiếu!");
                }

                session.remove(n);
                tx.commit();
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
            throw e; // Ném ra ngoài để BUS xử lý thông báo
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            }
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

    public Nganh getByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Nganh n WHERE n.maNganh = :ma";
            return session.createQuery(hql, Nganh.class)
                    .setParameter("ma", maNganh).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
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

    // ── ĐẾM SỐ LƯỢNG NGUYỆN VỌNG ĐĂNG KÝ VÀO NGÀNH ──────
    public long countNguyenVongByMaNganh(String maNganh) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(nv) FROM NguyenVongXetTuyen nv WHERE nv.maNganh = :ma";
            Long count = session.createQuery(hql, Long.class)
                    .setParameter("ma", maNganh)
                    .uniqueResult();
            return count != null ? count : 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // ── CẬP NHẬT ĐIỂM TRÚNG TUYỂN + SỐ LƯỢNG THEO PHƯƠNG THỨC ──────────────
    public boolean updateDiemTrungTuyen(String maNganh, Double diemTrungTuyen,
                                        int slDgnl, int slVsat, int slThpt) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            session.createMutationQuery(
                    "UPDATE Nganh n SET n.diemTrungTuyen = :dtt, " +
                    "n.slDgnl = :dgnl, n.slVsat = :vsat, n.slThpt = :thpt WHERE n.maNganh = :ma")
                    .setParameter("dtt", diemTrungTuyen)
                    .setParameter("dgnl", slDgnl)
                    .setParameter("vsat", slVsat)
                    .setParameter("thpt", slThpt)
                    .setParameter("ma", maNganh)
                    .executeUpdate();
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx != null && tx.isActive()) try { tx.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        }
    }
}

