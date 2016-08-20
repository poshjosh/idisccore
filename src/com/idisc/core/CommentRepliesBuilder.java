package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Installation;
import java.util.ArrayList;
import java.util.Calendar;
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
import com.bc.jpa.JpaContext;
import com.idisc.pu.entities.Comment_;
import com.idisc.pu.entities.Commentreplynotice;
import com.bc.jpa.dao.BuilderForSelect;

public class CommentRepliesBuilder {
    
  public List<Map<String, Object>> build(
      Installation installation, boolean directRepliesOnly, int maxAgeDays, int maxNotifications) {
      
    JpaContext cf = IdiscApp.getInstance().getJpaContext();
    
    EntityManager em = cf.getEntityManager(Comment.class);
    
    try {
        
      List<Comment> comments = getComments(em, installation, maxAgeDays);
      
XLogger.getInstance().log(Level.FINER, "Installation: {0}, has {1} comments {2} days old or earlier", 
        this.getClass(), installation, comments==null?null:comments.size(), maxAgeDays);

      if ((comments == null) || (comments.isEmpty())) {
        return Collections.EMPTY_LIST;
      }
      
      Map<Comment, List<Comment>> commentReplies = new HashMap<>();
      
      final Date NOW = new Date();
      
      EntityTransaction t = em.getTransaction();
      
      try {
          
        t.begin();
        
        for (Comment comment : comments) {
// commentreplynotice            
          if ((maxNotifications < 1 || this.getNotificationCount(comment) < maxNotifications) && 
                  !this.isReadByUser(comment)) {

            List<Comment> replies = getReplies(comment, directRepliesOnly);
            
            XLogger.getInstance().log(Level.FINER, "Comment with ID: {0} has {1} replies", 
            this.getClass(), comment.getCommentid(), replies == null ? null : replies.size());
            
            if ((replies != null) && (!replies.isEmpty())) {

              Commentreplynotice notification = new Commentreplynotice();
              notification.setCommentid(comment);
              notification.setDateusernotified(NOW);

              em.persist(notification);
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
  
  public int getNotificationCount(Comment comment) {
    List<Commentreplynotice> notifications = comment.getCommentreplynoticeList();
    if(notifications == null || notifications.isEmpty()) {
      return 0;
    }else{
      return notifications.size();
    }
  }

  public boolean isReadByUser(Comment comment) {
    List<Commentreplynotice> notifications = comment.getCommentreplynoticeList();
    if(notifications == null || notifications.isEmpty()) {
      return false;    
    }else{
      return notifications.get(0).getDateuserread() != null;
    }
  }
  
  public boolean isUserAlreadyNotified(Comment comment) {
    List<Commentreplynotice> notifications = comment.getCommentreplynoticeList();
    if(notifications == null || notifications.isEmpty()) {
      return false;    
    }else{
      return notifications.get(0).getDateusernotified() != null;
    }
  }
  
  public List<Comment> getReplies(Comment comment, boolean directReplies) {
      
    List<Comment> replies = directReplies ? comment.getCommentList() : comment.getFeedid().getCommentList();
    
    replies = replies == null ? Collections.EMPTY_LIST : new ArrayList(replies);
    
    if (directReplies) {

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
      
    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    
    JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
    
    BuilderForSelect<Comment> qb = jpaContext.getBuilderForSelect(Comment.class);
    
    qb.descOrder(Comment_.commentid.getName());
    
    this.format(qb, installation, cal.getTime());
    
    TypedQuery<Comment> tq = qb.createQuery();
    
    List<Comment> comments = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", 
            this.getClass(), comments == null ? null : comments.size());
    
    return comments;
  }
  
  public List<Integer> getFeedidsForUserComments(
          EntityManager em, Installation installation, int maxAgeDays) {
      
    JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();

    BuilderForSelect<Integer> qb = jpaContext.getBuilderForSelect(Comment.class, Integer.class);
    
    qb.select(Comment_.feedid.getName());
    qb.descOrder(Comment_.feedid.getName());
    
    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    
    this.format(qb, installation, cal.getTime());
    
    TypedQuery<Integer> tq = qb.createQuery();
    
    List<Integer> feedids = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", 
            this.getClass(), feedids == null ? null : feedids.size());
    
    return feedids;
  }
  
  private void format(BuilderForSelect<?> select, Installation installation, Date date) {
      
    select.where(Comment_.installationid.getName(), BuilderForSelect.EQ, installation)
    .and().where(Comment_.datecreated.getName(), BuilderForSelect.GTE, date);
  }
}
