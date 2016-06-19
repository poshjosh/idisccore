package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.JPQL;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Installation;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;

public class CommentNotificationsBuilder {
    
  public List<Map<String, Object>> build(
      Installation installation, boolean directRepliesOnly, int maxAgeDays, boolean repeat) {
      
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    
    EntityManager em = cf.getEntityManager(Comment.class);
    
    try {
        
      List<Comment> comments = getComments(em, installation, maxAgeDays);
      
XLogger.getInstance().log(Level.FINER, "Installation: {0}, has {1} comments {2} days old or earlier", 
        this.getClass(), comments==null?null:comments.size(), maxAgeDays);

      if ((comments == null) || (comments.isEmpty())) {
        return Collections.EMPTY_LIST;
      }
      
      Map<Comment, List<Comment>> commentReplies = new HashMap<>();
      
      final Date NOW = new Date();
      
      EntityTransaction t = em.getTransaction();
      
      try {
          
        t.begin();
        
        for (Comment comment : comments) {
            
          if (repeat || !isUserAlreadyNotified(comment)) {

            List<Comment> replies = getReplies(comment, directRepliesOnly);
            
            XLogger.getInstance().log(Level.FINER, "Comment with ID: {0} has {1} replies", 
            this.getClass(), comment.getCommentid(), replies == null ? null : replies.size());
            
            if ((replies != null) && (!replies.isEmpty())) {

              commentReplies.put(comment, replies);
              
              comment.setDateusernotified(NOW);
              
              em.merge(comment);
            }
          }
        }
        
        t.commit();
        
      }finally {
        if (t.isActive()) {
          t.rollback();
        }
      }
      
      List<Map<String, Object>> notices = new LinkedList();
      
      Set<Comment> keys = ((Map)commentReplies).keySet();
      
      for (Comment comment : keys) {
          
        Map<String, Object> notice = new HashMap(3, 1.0f);
        
        notice.put("feed", comment.getFeedid());
        
        notice.put("comment", comment);
        
        List<Comment> replies = (List<Comment>)commentReplies.get(comment);
        notice.put("replies", replies);
        
        notices.add(notice);
      }
      
      XLogger.getInstance().log(Level.FINE, "Returning: {0} notices", this.getClass(), notices.size());
      
      return notices;
      
    } finally {
      em.close();
    }
  }
  
  public boolean isUserAlreadyNotified(Comment comment) {
    return comment.getDateusernotified() != null;
  }
  
  public List<Comment> getReplies(Comment comment, boolean directReplies) {
      
    List<Comment> replies = directReplies ? comment.getCommentList() : comment.getFeedid().getCommentList();
    
    replies = new ArrayList(replies);
    
    if (!directReplies) {

      Iterator<Comment> iter = replies.iterator();
      
      while (iter.hasNext()) {
          
        Comment reply = (Comment)iter.next();
        
        if ((reply.getDatecreated().before(comment.getDatecreated())) || (comment.equals(reply))) {
          XLogger.getInstance().log(Level.FINER, "Removing reply: {0}", this.getClass(), reply);
          iter.remove();
        }
      }
    }
    
    return replies;
  }
  
  public List<Comment> getComments(EntityManager em, Installation installation, int maxAgeDays) {
      
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    
    JPQL jpql = cf.getJpql(Comment.class);
    
    Map<String, Object> where = new HashMap(3, 1.0F);
    
    where.put("installationid", installation);
    
    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    where.put("datecreated", cal.getTime());
    
    XLogger.getInstance().log(Level.FINER, "Query parameters: {0}", this.getClass(), where);
    
    StringBuilder query = new StringBuilder();
    
    String tableAlias = jpql.getTableAlias();
    
    jpql.appendSelectQuery(null, query);
    
    query.append(" WHERE ");
    
    jpql.setComparisonOperator("=");
    jpql.appendWherePair("installationid", tableAlias, query);
    
    query.append(" AND ");
    jpql.setComparisonOperator(" >= ");
    jpql.appendWherePair("datecreated", tableAlias, query);
    
    XLogger.getInstance().log(Level.FINER, "Query: {0}", this.getClass(), query);
    
    TypedQuery<Comment> tq = em.createQuery(query.toString(), Comment.class);
    
    jpql.updateQuery(em, tq, where, true);
    
    List<Comment> comments = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", 
            this.getClass(), comments == null ? null : comments.size());
    
    return comments;
  }
  
  public List<Integer> getFeedidsForUserComments(
          EntityManager em, Installation installation, int maxAgeDays) {
      
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    
    JPQL jpql = cf.getJpql(Comment.class);
    
    Map<String, Object> where = new HashMap(3, 1.0F);
    
    where.put("installationid", installation);
    
    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    where.put("datecreated", cal.getTime());
    
    XLogger.getInstance().log(Level.FINER, "Query parameters: {0}", this.getClass(), where);
    
    StringBuilder query = new StringBuilder();
    
    String tableAlias = jpql.getTableAlias();
    
    Collection columnToSelect = Collections.singleton("feedid");
    jpql.appendSelectQuery(columnToSelect, query);
    
    query.append(" WHERE ");
    
    jpql.setComparisonOperator("=");
    jpql.appendWherePair("installationid", tableAlias, query);
    
    query.append(" AND ");
    jpql.setComparisonOperator(" >= ");
    jpql.appendWherePair("datecreated", tableAlias, query);
    
    XLogger.getInstance().log(Level.FINER, "Query: {0}", this.getClass(), query);
    
    TypedQuery<Integer> tq = em.createQuery(query.toString(), Integer.class);
    
    jpql.updateQuery(em, tq, where, true);
    
    List<Integer> feedids = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", 
            this.getClass(), feedids == null ? null : feedids.size());
    
    return feedids;
  }
}
