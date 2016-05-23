package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.util.Comparator;
import java.util.Date;
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
public class FeedComparator implements Comparator<Feed>, AutoCloseable
{
    
  private boolean invertSort = true;
  private Map<Pattern, Integer> elite;
  private boolean exceptionLogged;
  
  private final EntityManager entityManager;
  
  private final Installation installation;
  
  private Query userSiteHitcountQuery;
  
  public FeedComparator() {
      this(null);
  } 
  
  public FeedComparator(Installation installation) { 
    this.entityManager = IdiscApp.getInstance().getControllerFactory().getEntityManager(Feedhit.class);
    this.installation = installation;
  }  

  @Override
  public void close() {
    if(entityManager != null && entityManager.isOpen()) {
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
  public int compare(Feed o1, Feed o2)
  {
    long t1 = computeTimeForSorting(o1);
    
    long t2 = computeTimeForSorting(o2);
    
    return this.compareLongs(t1, t2);
  }
  
  private long computeTimeForSorting(Feed feed)
  {
    long time = feed.getFeeddate() == null ? 0L : feed.getFeeddate().getTime();
    time += getAddedValueTime(feed);
    return time;
  }

  protected long getAddedValueTime(Feed feed)
  {

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
//long tb4 = System.currentTimeMillis();
//long mb4 = Runtime.getRuntime().freeMemory();
// This consumes a lot of memory      
//    Long feedhits = this.count(Feedhit.class, "feedid", feed, false);
      
    Long feedhits = feed.getFeedhitList() == null ? 0L : feed.getFeedhitList().size();

    long output; 
    if (feedhits > 0L) {
      long addedValue = feedhits * getAddValuePerHit();
      output = addedValue;
    } else {
      output = 0L;
    }
//this.logTimeAndMemoryConsumed("getAddedValueForFeedhits", tb4, mb4);
    return output;
  }
  
  private long user_totalhits = -1L;
  private long numberOfSites = -1L;
  
  private final Map<Integer, Long> usersite_hitcounts = new HashMap<>();
  
  protected long getAddedValueFor(Installation installation, Site site) { 

    long output = 0L;
    
    if ((installation != null) && (site != null))
    {
        
      Integer siteid = site.getSiteid();
      
      if (!this.usersite_hitcounts.containsKey(siteid))
      {

        Long appsitehits = this.countFeedhits(installation.getInstallationid(), siteid);
        
        this.usersite_hitcounts.put(siteid, appsitehits);
      }
      
      Long user_sitehits = this.usersite_hitcounts.get(siteid);
      
      if (user_sitehits != null)
      {
        if (this.user_totalhits == -1L) {
          initRequirements(installation);
        }
        long addedVal;
        if ((user_sitehits == 0L) || (this.user_totalhits == 0L)) {
          addedVal = 0L;
        } else {
          addedVal = user_sitehits / this.user_totalhits * this.numberOfSites * (getAddValuePerHit() * 4L);
        }
        
        output = addedVal;
      }
    }
    return output;
  }

  private void initRequirements(Installation installation)
  {
//long tb4 = System.currentTimeMillis();
//long mb4 = Runtime.getRuntime().freeMemory();
    if(installation == null) {
      throw new NullPointerException();
    }

    this.user_totalhits = this.count(Feedhit.class, "installationid", installation, false);
// This consumes a lot of memory    
//    this.user_totalhits = installation.getFeedhitList() == null ? 0L : installation.getFeedhitList().size();
    
    this.numberOfSites = this.count(Site.class);
//this.logTimeAndMemoryConsumed("initRequirements", tb4, mb4);
  }
  
  protected int compareInts(int x, int y) { 
    int n = this.invertSort ? Integer.compare(y, x) : Integer.compare(x, y);
    return n; 
  }
  
  protected int compareLongs(long x, long y) { 
    int n = this.invertSort ? Long.compare(y, x) : Long.compare(x, y);
    return n; 
  }

  protected int compareDates(Date date_a, Date date_b) { 
    if ((date_a == null) && (date_b == null))
      return 0;
    if (date_a == null)
      return this.invertSort ? 1 : -1;
    if (date_b == null) {
      return this.invertSort ? -1 : 1;
    }
    return this.invertSort ? date_b.compareTo(date_a) : date_a.compareTo(date_b);
  }
  
  private Long countFeedhits(Integer installationid, Integer siteid) { 
//long tb4 = System.currentTimeMillis();
//long mb4 = Runtime.getRuntime().freeMemory();
    if(userSiteHitcountQuery == null) {
      String queryString = "SELECT COUNT(t3.feedhitid) FROM site t0, installation t1, feed t2, feedhit t3 WHERE t0.siteid = ?1 AND t1.installationid = ?2 AND t2.siteid = ?3 AND t1.installationid = t3.installationid AND t2.feedid = t3.feedid";
      userSiteHitcountQuery = entityManager.createNativeQuery(queryString);
    }
    userSiteHitcountQuery.setParameter(1, siteid);
    userSiteHitcountQuery.setParameter(2, installationid);
    userSiteHitcountQuery.setParameter(3, siteid);
    Long output = (Long)userSiteHitcountQuery.getSingleResult();
//this.logTimeAndMemoryConsumed("countFeedHits", tb4, mb4);
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
  
  public boolean isInvertSort() {
    return this.invertSort;
  }
  
  public void setInvertSort(boolean invertSort) {
    this.invertSort = invertSort;
  }
  
  public Map<Pattern, Integer> getElite() {
    return this.elite;
  }
  
  public void setElite(Map<Pattern, Integer> elite) {
    this.elite = elite;
  }

//  private void logTimeAndMemoryConsumed(String key, long tb4, long mb4) {
//System.out.println(key+". consumed time: "+
//(System.currentTimeMillis()-tb4)+", memory: "+(mb4-Runtime.getRuntime().freeMemory()));        
//  }
}
