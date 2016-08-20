package com.idisc.core;

import com.bc.jpa.EntityController;
import com.bc.jpa.JpaContext;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Archivedfeed;
import com.idisc.pu.entities.Feed;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.exceptions.DatabaseException;
import com.bc.jpa.dao.BuilderForSelect;

public class FeedArchiver
{
  private EntityController<Feed, Integer> _fc;
  private EntityController<Archivedfeed, Integer> _afc;
  
  public int archiveFeeds(long maxAge, TimeUnit timeUnit, int batchSize)
  {
    Date before = new Date(convertAgeToTime(maxAge, timeUnit));
    
    return archiveFeeds(before, batchSize);
  }

  public int archiveFeeds(Date before, int batchSize)
  {
    EntityController<Feed, Integer> ec = getFeedController();
    
    XLogger.getInstance().log(Level.FINE, "Executing query to archive feeds before: {0}", getClass(), before);
    
    int offset = 0;
    for (;;)
    {
      List feedids = getFeedIdsBefore(before, offset, batchSize);
      
      XLogger.getInstance().log(Level.FINE, "Offset: {0}, batchSize: {1}, Ids: {2}", getClass(), Integer.valueOf(offset), feedids == null ? null : Integer.valueOf(feedids.size()), feedids);
      
      if ((feedids == null) || (feedids.isEmpty())) {
        break;
      }
      
//@edited changed = to +=      
      offset += feedids.size();
      
      EntityManager em = ec.getEntityManager();
      try
      {
        for (Object feedid : feedids) {
          try {
            int updateCount = executeUpdate(em, feedid);
          } catch (Exception e) { 
            if (XLogger.getInstance().isLoggable(Level.FINE, getClass())) {
              XLogger.getInstance().log(Level.FINE, e.toString(), getClass());
            } else {
              XLogger.getInstance().log(Level.FINER, "Error archiving feed with feedid: " + feedid, getClass(), e);
            }
          }
        }
      } finally {
        em.close();
      }
    }

    return offset;
  }

  public int executeUpdate(EntityManager em, Object feedid)
    throws Exception
  {
      
    XLogger logger = XLogger.getInstance();
    String insertSelectQuery = "INSERT INTO `archivedfeed` (archivedfeedid, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid)  SELECT null, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid FROM `feed` WHERE `feed`.`feedid` = '" + feedid + "'";
    String deleteQuery = "DELETE FROM `feed` WHERE `feed`.`feedid` = '" + feedid + "'";
    String selectArchivedfeed = "SELECT * FROM `archivedfeed` WHERE `archivedfeed`.`feedid` = '" + feedid + "' limit 0,1";
    int updateCount = -1;
    try {
      EntityTransaction t = em.getTransaction();
      try {
          
        t.begin();
        
        Feed feed = em.find(Feed.class, feedid);
logger.log(Level.FINER, "Found feed: {0}", this.getClass(), feed);
        if(feed == null) { // We can't archive a feed that doesn't exist
            updateCount = 0;
        }else{
            
          Query qsaf = em.createNativeQuery(selectArchivedfeed, Archivedfeed.class);
          qsaf.setFirstResult(0);
          qsaf.setMaxResults(1);
          List found = qsaf.getResultList();
          Object archivedfeed = found == null || found.isEmpty() ? null : found.get(0);

          int insertCount = -1;
          if(archivedfeed == null) { 
              
            Query q = em.createNativeQuery(insertSelectQuery);
          
            insertCount = q.executeUpdate();
logger.log(Level.FINER, "Archived feed: {0}", this.getClass(), feed);
          }else {
logger.log(Level.FINER, "Already archived {0} as {1}", this.getClass(), feed, archivedfeed);
          } 
          
          boolean alreadyArchived = archivedfeed != null;
          boolean archiveSucceeded = insertCount > 0;
            
          if (alreadyArchived ||  archiveSucceeded) {
              
            Query q = em.createNativeQuery(deleteQuery);
            
            int deleteCount = q.executeUpdate();
logger.log(Level.FINER, "Update count = {0} for operation 'DELETE {1} Feed'", 
this.getClass(), deleteCount, (alreadyArchived?"already archived":"archived"));
            if (deleteCount > 0) {
              t.commit();
              updateCount = deleteCount;
            } else {
logger.log(Level.FINER, "Rolling back delete of Feed: {1}", this.getClass(), feed);
              t.rollback();
              updateCount = 0;
            }
          }else {
logger.log(Level.FINER, "Rolling back insert of Archivedfeed with feedid: {1}", this.getClass(), feed);
            t.rollback();
            updateCount = 0;
          }
        }
      } finally {
        if (t.isActive()) {
logger.log(Level.FINER, "Rolling back active transaction", this.getClass());
          t.rollback();
          updateCount = 0;
        }
      }
    } finally {
//      XLogger.getInstance().log(Level.FINER, "Insert Query: {0}\nDelete Query: {1}\nUpdate count: {2}", getClass(), insertSelectQuery, deleteQuery, Integer.valueOf(updateCount));
    }

    return updateCount;
  }

  private void handleException(Throwable e)
  {
    boolean log = true;
    if (((e instanceof DatabaseException)) || (((e = e.getCause()) instanceof DatabaseException)))
    {

      DatabaseException dbe = (DatabaseException)e;
      
      if (((dbe.getInternalException() instanceof MySQLIntegrityConstraintViolationException)) && (dbe.getInternalException().getMessage().contains("Duplicate")))
      {
        log = false;
      }
    }
    if (log)
    {
      XLogger.getInstance().log(Level.WARNING, e.toString(), getClass());
    }
  }
  
  public int archiveFeeds_0(long maxAge, TimeUnit timeUnit, int limit)
  {
    Date before = new Date(convertAgeToTime(maxAge, timeUnit));
    
    return archiveFeeds_0(before, limit);
  }
  
  public int archiveFeeds_0(Date before, int batchSize)
  {
    EntityController<Feed, Integer> ec = getFeedController();
    
    XLogger.getInstance().log(Level.FINE, "Executing query to archive feeds before: {0}", getClass(), before);
    
    int offset = 0;
    for (;;)
    {
      EntityManager em = ec.getEntityManager();
      
      List<Feed> feeds = getFeedsBefore(em, before, offset, batchSize);
      
      XLogger.getInstance().log(Level.FINE, "Offset: {0}, batchSize: {1}", getClass(), Integer.valueOf(offset), feeds == null ? null : Integer.valueOf(feeds.size()));

      if ((feeds == null) || (feeds.isEmpty())) {
        break;
      }
      
//@edited changed = to +=      
      offset += feeds.size();
      
      try
      {
        Archivedfeed archivedfeed = new Archivedfeed();
        
        for (Feed feed : feeds)
        {
          boolean updated = archiveFeed(em, feed, archivedfeed);
          
          em.detach(archivedfeed);
        }
      } finally { 
        em.close();
      }
    }
    
    return offset;
  }

  public boolean archiveFeed(EntityManager em, Feed feed, Archivedfeed archivedfeed)
  {
    boolean output = false;
    try
    {
      updateArchivedfeed(archivedfeed, feed);
      
      EntityTransaction t = em.getTransaction();
      
      try
      {
        t.begin();
        
        em.persist(archivedfeed);
        
        em.remove(feed);
        
        t.commit();
        
        output = true;
      }
      finally {
        if (t.isActive()) {
          t.rollback();
          output = false;
        }
      }
    }
    catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, e.toString(), getClass());
    } finally {
      XLogger.getInstance().log(Level.FINER, "Updated: {0}, Delete feed: {1}, Insert archvivedfeed: {2}", getClass(), Boolean.valueOf(output), feed, archivedfeed);
    }
    
    return output;
  }
  
  public List<Integer> getFeedIdsBefore(Date before, int offset, int limit) {
      
    final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
    
    try(BuilderForSelect<Integer> qb = jpaContext.getBuilderForSelect(Feed.class, Integer.class)) {
        
        List<Integer> feedids = qb.from(Feed.class)
        .select("feedid")        
        .where("feeddate", BuilderForSelect.LT, before)
        .createQuery().getResultList();
        
        return feedids;
    }
  }
  
  public List<Feed> getFeedsBefore(Date before, int offset, int limit)
  {
    EntityController<Feed, Integer> ec = getFeedController();
    
    EntityManager em = ec.getEntityManager();
    
    try
    {
      return getFeedsBefore(em, before, offset, limit);
    }
    finally
    {
      em.close();
    }
  }
  
  private List<Feed> getFeedsBefore(EntityManager em, Date before, int offset, int limit)
  {
    CriteriaBuilder cb = em.getCriteriaBuilder();
    CriteriaQuery<Feed> query = cb.createQuery(Feed.class);
    Root<Feed> feed = query.from(Feed.class);
    
    XLogger.getInstance().log(Level.FINER, "Select feeds before: {0}", getClass(), before);
    query.where(cb.lessThan(feed.<Date>get("feeddate"), before));
    
    TypedQuery<Feed> typedQuery = em.createQuery(query);
    typedQuery.setFirstResult(offset);
    typedQuery.setMaxResults(limit);

    typedQuery.setHint("eclipselink.batch", "f.commentList");
    typedQuery.setHint("eclipselink.batch", "f.feedhitList");
    typedQuery.setHint("eclipselink.batch", "f.bookmarkfeedList");
    typedQuery.setHint("eclipselink.batch", "f.favoritefeedList");
    typedQuery.setHint("eclipselink.batch.type", BatchFetchType.IN);
    
    List<Feed> feeds = typedQuery.getResultList();
    
    XLogger.getInstance().log(Level.FINER, "Expected: {0}, retreived {1} feeds from database", getClass(), Integer.valueOf(limit), feeds == null ? null : Integer.valueOf(feeds.size()));
    
    return feeds;
  }
  
  public static final long convertAgeToTime(long maxAge, TimeUnit timeUnit)
  {
    long maxAgeInMillis = timeUnit.toMillis(maxAge);
    
    return System.currentTimeMillis() - maxAgeInMillis;
  }
  
  public void updateArchivedfeed(Archivedfeed feedarchive, Feed feed) {
    feedarchive.setAuthor(feed.getAuthor());
    feedarchive.setCategories(feed.getCategories());
    feedarchive.setContent(feed.getContent());
    feedarchive.setDatecreated(feed.getDatecreated());
    feedarchive.setDescription(feed.getDescription());
    feedarchive.setExtradetails(feed.getExtradetails());
    feedarchive.setArchivedfeedid(null);
    feedarchive.setFeeddate(feed.getFeeddate());
    feedarchive.setFeedid(feed.getFeedid());
    feedarchive.setImageurl(feed.getImageurl());
    feedarchive.setKeywords(feed.getKeywords());
    feedarchive.setRawid(feed.getRawid());
    feedarchive.setSiteid(feed.getSiteid());
    feedarchive.setTimemodified(feed.getTimemodified());
    feedarchive.setTitle(feed.getTitle());
    feedarchive.setUrl(feed.getUrl());
  }
  
  private EntityController<Feed, Integer> getFeedController()
  {
    if (this._fc == null) {
      this._fc = IdiscApp.getInstance().getJpaContext().getEntityController(Feed.class, Integer.class);
    }
    return this._fc;
  }
  
  private EntityController<Archivedfeed, Integer> getArchivedfeedController()
  {
    if (this._afc == null) {
      this._afc = IdiscApp.getInstance().getJpaContext().getEntityController(Archivedfeed.class, Integer.class);
    }
    return this._afc;
  }
}
