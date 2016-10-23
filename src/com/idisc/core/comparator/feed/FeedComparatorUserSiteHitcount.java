package com.idisc.core.comparator.feed;

import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * <b>Implements {@link java.lang.AutoCloseable} hence must be closed after use</b>
 * @author poshjosh
 */
public class FeedComparatorUserSiteHitcount extends BaseFeedComparator implements AutoCloseable {
    
  private boolean exceptionLogged;
  
  private Map<Pattern, Integer> elite;
  
  private final EntityManager entityManager;
  
  private final Installation installation;
  
  private Query userSiteHitcountQuery;
  
  public FeedComparatorUserSiteHitcount() {
    this(null);
  } 
  
  public FeedComparatorUserSiteHitcount(Installation installation) { 
    this(installation, null, false);
  }  
  
  public FeedComparatorUserSiteHitcount(Installation installation, Map<Pattern, Integer> elite, boolean reverseOrder) { 
    super(reverseOrder);
    this.installation = installation;
    this.elite = elite;
    this.entityManager = IdiscApp.getInstance().getJpaContext().getEntityManager(Feedhit.class);
  }  
  
  public boolean isOpen() {
    return this.entityManager != null && this.entityManager.isOpen();
  }

  @Override
  public void close() {
    if(this.isOpen()) {
      entityManager.close();
    }
  }
  
  public long getAddValuePerHit() {
      return 900_000; // 15 minutes in millis, i.e 15 * 60 * 1000;
  }
  
  public long getAddedValueMax() {
      return 2_880; // 2 days in minutes, i.e 48 * 60
  }
  
  @Override
  public long getScore(Feed feed) {
      
    long score = feed.getFeeddate() == null ? 0L : feed.getFeeddate().getTime();
    
    score += getAddedValueTime(feed);
    
    String imageUrl = feed.getImageurl();

    if(imageUrl != null) {

      score = score * 2;
    }
    
    return score;
  }
  
  protected long getAddedValueTime(Feed feed) {

    long output = this.getAddedValueForFeedhits(feed);
    
    Site site = feed.getSiteid();
    try
    {
      output += getAddedValueFor(installation, site);
    } catch (RuntimeException e) {
      if (!this.exceptionLogged) {
        this.exceptionLogged = true;
        XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
      }
    }
    
    String title = feed.getTitle();
    if ((output > 0L) && (title != null) && 
      (this.elite != null) && (!this.elite.isEmpty())) {
      Set<Map.Entry<Pattern, Integer>> entries = this.elite.entrySet();
      for (Map.Entry<Pattern, Integer> entry : entries) {
        Pattern pattern = (Pattern)entry.getKey();
        Integer addedValueFactor = entry.getValue();
        
        if (pattern.matcher(title).find())
        {
          output *= addedValueFactor;
        }
      }
    }

    if (output > getAddedValueMax()) {
      output = getAddedValueMax();
    }
    
    return output;
  }
  
  private long getAddedValueForFeedhits(Feed feed) {
      
long tb4 = System.currentTimeMillis();
long mb4 = Runtime.getRuntime().freeMemory();

// This consumes a lot of memory      
//    Long feedhits = this.count(Feedhit.class, "feedid", feed, false);
    final long feedhits = this.countFeedHits(feed);

    long output; 
    if (feedhits > 0L) {
      long addedValue = feedhits * getAddValuePerHit();
      output = addedValue;
    } else {
      output = 0L;
    }
this.logTimeAndMemoryConsumed("getAddedValueForFeedhits", tb4, mb4);
    return output;
  }
  
  private final Map<Integer, Long> usersite_hitcounts = new HashMap<>();
  
  protected long getAddedValueFor(Installation installation, Site site) { 

    long output = 0L;
    
    if (site != null) {
        
      Integer siteid = site.getSiteid();
      
      if (!this.usersite_hitcounts.containsKey(siteid)) {

        Long appsitehits = this.countFeedhits(installation==null?null:installation.getInstallationid(), siteid);
        
        this.usersite_hitcounts.put(siteid, appsitehits);
      }
      
      Long user_sitehits = this.usersite_hitcounts.get(siteid);
      
      if (user_sitehits != null) {
          
        long userTotalHits = installation == null ? 0L : this.getUserTotalHits(installation);
        long addedVal;
        if ((user_sitehits == 0L) || (userTotalHits == 0L)) {
          addedVal = 0L;
        } else {
          addedVal = user_sitehits / userTotalHits * this.getNos() * (getAddValuePerHit() * 4L);
        }
        
        output = addedVal;
      }
    }
    return output;
  }

  private long _uth = -1L;
  private long getUserTotalHits(Installation installation) {
    if(_uth == -1L) {
      this._uth = this.count(Feedhit.class, "installationid", installation, false);
// This consumes a lot of memory    
//      this._uth = installation.getFeedhitList() == null ? 0L : installation.getFeedhitList().size();
    }
    return _uth;
  }
  
  private long _nos = -1L;
  private long getNos() {
    if(this._nos == -1L) {
      this._nos = this.count(Site.class);
    }
    return this._nos;
  }
  
  private Long countFeedhits(Integer installationid, Integer siteid) { 
    if(siteid == null) {
        throw new NullPointerException();
    }
long tb4 = System.currentTimeMillis();
long mb4 = Runtime.getRuntime().freeMemory();
    if(userSiteHitcountQuery == null) {
      String queryString;
      if(installationid != null) {
        queryString = "SELECT COUNT(t3.feedhitid) FROM site t0, installation t1, feed t2, feedhit t3 WHERE t0.siteid = ?1 AND t1.installationid = ?2 AND t2.siteid = ?3 AND t1.installationid = t3.installationid AND t2.feedid = t3.feedid";
      }else{
        queryString = "SELECT COUNT(t2.feedhitid) FROM site t0, feed t1, feedhit t2 WHERE t0.siteid = ?1 AND t1.siteid = ?2 AND t1.feedid = t2.feedid";  
      }
      userSiteHitcountQuery = entityManager.createNativeQuery(queryString);
    }
    if(installationid != null) {
      userSiteHitcountQuery.setParameter(1, siteid);
      userSiteHitcountQuery.setParameter(2, installationid);
      userSiteHitcountQuery.setParameter(3, siteid);
    }else{
      userSiteHitcountQuery.setParameter(1, siteid);
      userSiteHitcountQuery.setParameter(2, siteid);
    }
    Long output = (Long)userSiteHitcountQuery.getSingleResult();
this.logTimeAndMemoryConsumed("countFeedHits", tb4, mb4);
    return output;
  }
  
  private Long count(Class entityClass) {
      return count(entityClass, null, null, false);
  }
  
  private Long count(Class entityClass, String key, Object value, boolean distinct) {
//long tb4 = System.currentTimeMillis();
//long mb4 = Runtime.getRuntime().freeMemory();
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> cq = cb.createQuery(Long.class);
    Root<Long> root = cq.from(entityClass); 
    Expression<Long> countExpression;
    if(distinct) {
        countExpression = cb.countDistinct(root);
    }else{
        countExpression = cb.count(root);
    }        
    cq.select(countExpression);
    if(key != null) {
      Predicate keyEqualsValue = cb.equal(root.get(key), value);
      cq.where(keyEqualsValue);
    }
    Query q = entityManager.createQuery(cq);
    Long count = (Long)q.getSingleResult();
//this.logTimeAndMemoryConsumed("Count: "+entityClass.getSimpleName(), tb4, mb4);
    return count;
  }
  
  public final Map<Pattern, Integer> getElite() {
    return this.elite;
  }
  
  private void logTimeAndMemoryConsumed(String key, long tb4, long mb4) {
    if(true) {
      return;
    }
System.out.println(key+". consumed time: "+
(System.currentTimeMillis()-tb4)+", memory: "+(mb4-Runtime.getRuntime().freeMemory()));        
  }
}
