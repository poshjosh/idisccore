package com.idisc.core;

import java.util.logging.Logger;
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
import com.bc.jpa.context.JpaContext;
import com.idisc.pu.entities.Comment_;
import com.idisc.pu.entities.Commentreplynotice;
import com.idisc.core.util.TimeZones;
import com.bc.jpa.dao.Select;

public class CommentRepliesBuilder {
    
  private transient static final Logger LOG = Logger.getLogger(CommentRepliesBuilder.class.getName());
    
  public List<Map<String, Object>> build(
      Installation installation, boolean directRepliesOnly, int maxAgeDays, int maxNotifications) {
      
    JpaContext cf = IdiscApp.getInstance().getJpaContext();
    
    EntityManager em = cf.getEntityManager(Comment.class);
    
    List<Map<String, Object>> notices;
      
    try {
        
      List<Comment> comments = getComments(em, installation, maxAgeDays);
      
      Level level = comments == null || comments.isEmpty() ? Level.FINER : Level.FINE;
      if(LOG.isLoggable(level)) {
        LOG.log(level, "Installation: {0}, has {1} comments {2} days old or earlier", 
          new Object[]{installation, comments==null?null:comments.size(), maxAgeDays});
      }
      if (comments == null || comments.isEmpty()) {
          
        notices = Collections.EMPTY_LIST;
        
      }else{
         
        notices = new LinkedList();

        Map<Comment, List<Comment>> commentReplies = new HashMap<>();

        final Date NOW_DB_TIMEZONE = new TimeZones().getCurrentTimeInDatabaseTimeZone();

        EntityTransaction t = em.getTransaction();

        try {

          t.begin();

          for (Comment comment : comments) {
  // commentreplynotice            
            if (this.hasMoreNotifications(comment, maxNotifications) && !this.isReadByUser(comment)) {

              List<Comment> replies = getReplies(comment, directRepliesOnly);

              level = replies == null || replies.isEmpty() ? Level.FINER : Level.FINE;
              if(LOG.isLoggable(level)) {
                LOG.log(level, "Comment with ID: {0} has {1} replies", 
                  new Object[]{comment.getCommentid(), replies == null ? null : replies.size()});
              }
              if ((replies != null) && (!replies.isEmpty())) {

                commentReplies.put(comment, replies);
                  
                Commentreplynotice notification = new Commentreplynotice();
                notification.setCommentid(comment);
                notification.setDateusernotified(NOW_DB_TIMEZONE);
                notification.setInstallationid(installation);

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

        Set<Comment> keys = ((Map)commentReplies).keySet();

        for (Comment comment : keys) {

          Map<String, Object> notice = new HashMap(3, 1.0f);

          notice.put("feed", comment.getFeedid());

          notice.put("comment", comment);

          List<Comment> replies = (List<Comment>)commentReplies.get(comment);
          notice.put("replies", replies);

          notices.add(notice);
        }
      }
    } finally {
      em.close();
    }
    
    final Level level = notices.isEmpty() ? Level.FINER : Level.FINE;
    if(LOG.isLoggable(level)) {
        LOG.log(level, "Returning: {0} notices for screenname: {1} of {2}", 
        new Object[]{notices.size(), installation.getScreenname(), installation});
    }
    return notices;
  }
  
  public boolean hasMoreNotifications(Comment comment, int maxNotifications) {
    int notificationsCount = -1;
    final boolean output = maxNotifications < 1 || (notificationsCount = this.getNotificationCount(comment)) < maxNotifications;
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Has more notices: {0}, comment with id: {1}, max notices: {2}, notices count: {3}", new Object[]{ output,  comment.getCommentid(),  maxNotifications,  notificationsCount});
    }
    return output;
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
    boolean output;
    if(notifications == null || notifications.isEmpty()) {
      output = false;    
    }else{
      output = notifications.get(0).getDateuserread() != null;
    }
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Is read by user: {0}, user: {1}, comment with id: {2}", new Object[]{ output,  comment.getInstallationid().getInstallationid(),  comment.getCommentid()});
    }
    return output;
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
          if(LOG.isLoggable(Level.FINER)){
               LOG.log(Level.FINER, "Removing reply: {0}", reply);
          }
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
    
    Select<Comment> qb = jpaContext.getDaoForSelect(Comment.class);
    
    qb.descOrder(Comment_.commentid.getName());
    
    this.format(qb, installation, cal.getTime());
    
    TypedQuery<Comment> tq = qb.createQuery();
    
    List<Comment> comments = tq.getResultList();
    
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Found: {0} comments", 
 comments == null ? null : comments.size());
    }
    
    return comments;
  }
  
  public List<Integer> getFeedidsForUserComments(
          EntityManager em, Installation installation, int maxAgeDays) {
      
    JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();

    Select<Integer> qb = jpaContext.getDaoForSelect(Comment.class, Integer.class);
    
    qb.select(Comment_.feedid.getName());
    qb.descOrder(Comment_.feedid.getName());
    
    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    
    this.format(qb, installation, cal.getTime());
    
    TypedQuery<Integer> tq = qb.createQuery();
    
    List<Integer> feedids = tq.getResultList();
    
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Found: {0} comments", 
 feedids == null ? null : feedids.size());
    }
    
    return feedids;
  }
  
  private void format(Select<?> select, Installation installation, Date date) {
      
    select.where(Comment_.installationid.getName(), Select.EQ, installation)
    .and().where(Comment_.datecreated.getName(), Select.GTE, date);
  }
}
