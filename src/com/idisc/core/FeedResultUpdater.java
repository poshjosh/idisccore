package com.idisc.core;

import com.idisc.core.util.Util;
import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

public class FeedResultUpdater {
    
  public int process(Map<String, Collection<Feed>> allResults) {
      
    XLogger.getInstance().log(Level.FINER, "Saving feeds.", getClass());
    
    int created = 0;
    
    for (String taskName : allResults.keySet())  {
      Collection<Feed> taskResults = (Collection)allResults.get(taskName);
      
      if ((taskResults != null) && (!taskResults.isEmpty())) {

        created += process(taskName, taskResults);
      }
    }
    XLogger.getInstance().log(Level.FINE, "Saved {0} feeds", getClass(), created);
    return created;
  }
  
  public int process(String taskName, Collection<Feed> taskResults) {
      
    XLogger.getInstance().log(Level.FINE, "Task: {0}, has {1} results.", getClass(), taskName, taskResults == null ? null : Integer.valueOf(taskResults.size()));
    

    if ((taskResults == null) || (taskResults.isEmpty())) {
      return 0;
    }
    
    int created = 0;
    
    EntityManager em = getFeedController().getEntityManager();
    
    // Use a copy to prevent concurrent modification exception
    //
    Iterator<Feed> iter = new ArrayList(taskResults).iterator();
    
    Feed reusedFeedParams = new Feed();
    
    Date dateCreated = new Date();
    
    try {
        
      while (iter.hasNext()) {

        Feed toCreate = (Feed)iter.next();
        
        if (toCreate != null) {

          boolean updated = update(em, reusedFeedParams, toCreate, dateCreated);
          
          if (updated) {
            created++;
          }
        }
      }
      XLogger.getInstance().log(Level.FINE, "Created {0} records for task: {1}.", getClass(), Integer.valueOf(created), taskName);

    }catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected error updating feeds of type: " + taskName, getClass(), e);
    } finally {
      em.close();
    }
    
    return created;
  }
  
  private boolean update(EntityManager em, Feed reusedFeedParams, Feed toCreate, Date dateCreated) {
    boolean output = false;
    
    EntityTransaction t = null;
    
    try {
        
      if (!Util.isInDatabase(getFeedController(), reusedFeedParams, toCreate)) {
          
        t = em.getTransaction();
        
        t.begin();
        
        dateCreated.setTime(System.currentTimeMillis());
        toCreate.setDatecreated(dateCreated);
        
        em.persist(toCreate);
        
        t.commit();
        
        output = true;
      }
    } catch (Exception e) {
      XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
    } finally {
      if ((t != null) && (t.isActive())) {
        t.rollback();
        output = false;
      }
    }
    return output;
  }
  
  private String toString(Feed feed) {
    StringBuilder builder = new StringBuilder();
    builder.append("Id: ").append(feed.getFeedid());
    builder.append(", site: ").append(feed.getSiteid().getSite());
    builder.append(", author: ").append(feed.getAuthor());
    builder.append(", date: ").append(feed.getFeeddate());
    builder.append(", link: ").append(feed.getUrl());
    builder.append(", imageUrl: ").append(feed.getImageurl());
    return builder.toString();
  }
  
  private EntityController<Feed, Integer> _fc;
  private EntityController<Feed, Integer> getFeedController() {
    if (this._fc == null) {
      this._fc = IdiscApp.getInstance().getControllerFactory().getEntityController(Feed.class, Integer.class);
    }
    return this._fc;
  }
}
