package dao;

import entity.ThiSinh;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import util.HibernateUtil;

import java.util.List;
import java.util.Queue;

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
}
