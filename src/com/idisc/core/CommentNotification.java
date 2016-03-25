package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.JPQL;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
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




public class CommentNotification
{
  public static List<Map<String, Object>> getNotifications(Installation installation, EntityJsonFormat jsonFormat, boolean directReplies, int maxAgeDays)
  {
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    EntityController<Comment, Integer> ec = cf.getEntityController(Comment.class, Integer.class);
    EntityManager em = ec.getEntityManager();
    
    try
    {
      List<Comment> comments = getComments(em, installation, maxAgeDays);
      
      if ((comments == null) || (comments.isEmpty())) {
        return null;
      }
      


      Object accepted = new HashMap();
      



      Date dateUserNotified = new Date();
      
      EntityTransaction t = em.getTransaction();
      
      try
      {
        t.begin();
        
        for (Comment comment : comments)
        {
          if (!alreadyNotified(comment))
          {


            List<Comment> replies = getReplies(comment, directReplies);
            
            XLogger.getInstance().log(Level.FINER, "Comment with ID: {0} has {1} replies", CommentNotification.class, comment.getCommentid(), replies == null ? null : Integer.valueOf(replies.size()));
            

            if ((replies != null) && (!replies.isEmpty()))
            {


              ((Map)accepted).put(comment, replies);
              

              comment.setDateusernotified(dateUserNotified);
              
              em.merge(comment);
            }
          }
        }
      }
      finally {
        if (!t.isActive()) {}
      }
      


      List<Map<String, Object>> notices = new LinkedList();
      
      Set<Comment> keys = ((Map)accepted).keySet();
      
      for (Comment comment : keys)
      {
        List<Comment> replies = (List)((Map)accepted).get(comment);
        
        Map<String, Object> notice = new HashMap(3, 1.0F);
        
        notice.put("feed", jsonFormat.toMap(comment.getFeedid()));
        notice.put("comment", jsonFormat.toMap(comment));
        
        List<Map> convertedReplies = new LinkedList();
        notice.put("replies", convertedReplies);
        for (Comment reply : replies) {
          convertedReplies.add(jsonFormat.toMap(reply));
        }
        
        notices.add(notice);
      }
      
      XLogger.getInstance().log(Level.FINE, "Returning: {0} notices", CommentNotification.class, Integer.valueOf(notices.size()));
      
      return notices;
    }
    finally
    {
      em.close();
    }
  }
  
  public static boolean alreadyNotified(Comment comment) {
    return comment.getDateusernotified() != null;
  }
  
  public static List<Comment> getReplies(Comment comment, boolean directReplies)
  {
    List<Comment> replies = directReplies ? comment.getCommentList() : comment.getFeedid().getCommentList();
    


    replies = new ArrayList(replies);
    
    if (!directReplies)
    {


      Iterator<Comment> iter = replies.iterator();
      
      while (iter.hasNext())
      {
        Comment reply = (Comment)iter.next();
        
        if ((reply.getDatecreated().before(comment.getDatecreated())) || (comment.equals(reply)))
        {

          XLogger.getInstance().log(Level.FINER, "Removing reply: {0}", CommentNotification.class, reply);
          iter.remove();
        }
      }
    }
    
    return replies;
  }
  



  public static List<Comment> getComments(EntityManager em, Installation installation, int maxAgeDays)
  {
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    
    JPQL jpql = cf.getJpql(Comment.class);
    
    Map<String, Object> where = new HashMap(3, 1.0F);
    
    where.put("installationid", installation);
    

    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    where.put("datecreated", cal.getTime());
    
    XLogger.getInstance().log(Level.FINER, "Query parameters: {0}", CommentNotification.class, where);
    
    StringBuilder query = new StringBuilder();
    
    String tableAlias = jpql.getTableAlias();
    
    jpql.appendSelectQuery(null, query);
    
    query.append(" WHERE ");
    
    String EQUALS = "=";
    String AND = " AND ";
    
    jpql.setComparisonOperator("=");
    jpql.appendWherePair("installationid", tableAlias, query);
    




    query.append(" AND ");
    jpql.setComparisonOperator(" >= ");
    jpql.appendWherePair("datecreated", tableAlias, query);
    
    XLogger.getInstance().log(Level.FINER, "Query: {0}", CommentNotification.class, query);
    
    TypedQuery<Comment> tq = em.createQuery(query.toString(), Comment.class);
    
    jpql.updateQuery(em, tq, where, true);
    
    List<Comment> comments = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", CommentNotification.class, comments == null ? null : Integer.valueOf(comments.size()));
    

    return comments;
  }
  



  public static List<Integer> getFeedidsForUserComments(EntityManager em, Installation installation, int maxAgeDays)
  {
    ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
    
    JPQL jpql = cf.getJpql(Comment.class);
    
    Map<String, Object> where = new HashMap(3, 1.0F);
    
    where.put("installationid", installation);
    

    Calendar cal = Calendar.getInstance();
    cal.add(6, -maxAgeDays);
    where.put("datecreated", cal.getTime());
    
    XLogger.getInstance().log(Level.FINER, "Query parameters: {0}", CommentNotification.class, where);
    
    StringBuilder query = new StringBuilder();
    
    String tableAlias = jpql.getTableAlias();
    
    Collection columnToSelect = Collections.singleton("feedid");
    jpql.appendSelectQuery(columnToSelect, query);
    
    query.append(" WHERE ");
    
    String EQUALS = "=";
    String AND = " AND ";
    
    jpql.setComparisonOperator("=");
    jpql.appendWherePair("installationid", tableAlias, query);
    




    query.append(" AND ");
    jpql.setComparisonOperator(" >= ");
    jpql.appendWherePair("datecreated", tableAlias, query);
    
    XLogger.getInstance().log(Level.FINER, "Query: {0}", CommentNotification.class, query);
    
    TypedQuery<Integer> tq = em.createQuery(query.toString(), Integer.class);
    
    jpql.updateQuery(em, tq, where, true);
    
    List<Integer> feedids = tq.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Found: {0} comments", CommentNotification.class, feedids == null ? null : Integer.valueOf(feedids.size()));
    

    return feedids;
  }
}
