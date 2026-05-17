package dao;

import entity.ThiSinh;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class ThiSinhDAO {
    public boolean insert(ThiSinh ts){
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            tx = session.beginTransaction();
            session.persist(ts);
            tx.commit();
            return true;
        }
        catch (Exception e){
            if(tx!= null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
    public boolean update(ThiSinh ts){
        Transaction tx = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            tx = session.beginTransaction();
            session.merge(ts);
            tx.commit();
            return true;
        } catch (Exception e){
            if(tx!=null) tx.rollback();
            e.printStackTrace();
            return false;
        }
    }
    public ThiSinh getById(int id){
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Hàm get của Hibernate sẽ tìm đúng đối tượng theo ID, cực nhanh
            return session.get(ThiSinh.class, id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<ThiSinh> getPaginatedList(int start,int line){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String hql = "FROM ThiSinh t";
            Query<ThiSinh> query = session.createQuery(hql,ThiSinh.class);
            query.setFirstResult(start);
            query.setMaxResults(line);
            return query.list();
        } catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public long countTotalCandidates(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String hql = "SELECT count(t) FROM ThiSinh t";
            return session.createQuery(hql, Long.class).uniqueResult();
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }

    public List<ThiSinh> searchCandidates(int start, int line, String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM ThiSinh t WHERE t.cccd LIKE :kw OR t.ho LIKE :kw OR t.ten LIKE :kw OR CONCAT(t.ho, ' ', t.ten) LIKE :kw";
            Query<ThiSinh> query = session.createQuery(hql, ThiSinh.class);
            query.setParameter("kw", "%" + keyword + "%");
            query.setFirstResult(start);
            query.setMaxResults(line);
            return query.list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public long countSearchCandidates(String keyword) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(t) FROM ThiSinh t WHERE t.cccd LIKE :kw OR t.ho LIKE :kw OR t.ten LIKE :kw OR CONCAT(t.ho, ' ', t.ten) LIKE :kw";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("kw", "%" + keyword + "%");
            return query.uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    public boolean checkCccdExists(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(t) FROM ThiSinh t WHERE t.cccd = :cccd";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("cccd", cccd);
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public Set<String> getAllCccd(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String hql = "SELECT t.cccd FROM ThiSinh t";
            List<String> list = session.createQuery(hql,String.class).list();
            return new HashSet<>(list);
        }catch (Exception e){
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    public boolean checkSbdExists(String sbd) {
        if (sbd == null || sbd.trim().isEmpty()) return false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT count(t) FROM ThiSinh t WHERE t.soBaoDanh = :sbd";
            Query<Long> query = session.createQuery(hql, Long.class);
            query.setParameter("sbd", sbd.trim());
            return query.uniqueResult() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> getAllSbd(){
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            String hql = "SELECT t.soBaoDanh FROM ThiSinh t WHERE t.soBaoDanh IS NOT NULL AND t.soBaoDanh != ''";
            List<String> list = session.createQuery(hql,String.class).list();
            return new HashSet<>(list);
        }catch (Exception e){
            e.printStackTrace();
            return new HashSet<>();
        }
    }

    public ThiSinh getBySbd(String sbd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh t WHERE t.soBaoDanh = :sbd", ThiSinh.class)
                    .setParameter("sbd", sbd).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ThiSinh getByCccd(String cccd) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh t WHERE t.cccd = :cccd", ThiSinh.class)
                    .setParameter("cccd", cccd).uniqueResult();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean delete(int id) {
        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();
            ThiSinh ts = session.get(ThiSinh.class, id);
            if (ts != null) {
                session.remove(ts);
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

    public int insertBatch(List<ThiSinh> candidates) {
        Transaction tx = null;
        int successCount = 0;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            for (int i = 0; i < candidates.size(); i++) {
                session.persist(candidates.get(i));
                successCount++;

                // Kỹ thuật Batching cốt lõi của Hibernate: Cứ 50 người thì xả bộ nhớ
                if (i > 0 && i % 50 == 0) {
                    session.flush(); // Đẩy lệnh INSERT xuống MySQL
                    session.clear(); // Xóa rác trong bộ nhớ Session của Java
                }
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            return 0; // Trả về 0 nếu toàn bộ lô bị lỗi
        }
        return successCount;
    }

    public List<Object[]> countByDoiTuong() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.doiTuong, COUNT(t) FROM ThiSinh t GROUP BY t.doiTuong";
            return session.createQuery(hql, Object[].class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Object[]> countByKhuVuc() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "SELECT t.khuVuc, COUNT(t) FROM ThiSinh t GROUP BY t.khuVuc";
            return session.createQuery(hql, Object[].class).list();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ── LẤY TẤT CẢ (cho engine xét tuyển) ───────────────
    public List<ThiSinh> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM ThiSinh t", ThiSinh.class).list();
        } catch (Exception e) { e.printStackTrace(); return List.of(); }
    }
}

