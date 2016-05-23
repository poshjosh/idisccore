package com.idisc.core;

import com.bc.util.IntegerArray;
import com.bc.util.XLogger;
import com.idisc.core.jpa.FeedSearch;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class FeedCache 
{
  private static List<Feed> cachedFeeds;
  
  private static Map<Integer, CharSequence> cachedFeedsJson;
  
  private static long lastTime;
  
  public FeedCache() { }
  
  public boolean isRearrangeOutput() {
    return true;
  }
  
  public long getFeedCycleIntervalMillis() {
    return TimeUnit.MINUTES.toMillis(15);
  }
  
  public int getCacheLimit() {
    return 500;
  }
  
  public boolean isNextUpdateDue() {
    return (lastTime == 0L) || (getTimeElapsedSinceLastUpdate() > getFeedCycleIntervalMillis());
  }
  
  public long getTimeElapsedSinceLastUpdate() {
    return lastTime <= 0L ? 0L : System.currentTimeMillis() - lastTime;
  }
  
  public boolean updateCache()
  {
    int cacheLimit = getCacheLimit();
    
    List<Feed> feeds = new FeedSearch().select(null, null, 0, cacheLimit * 2);
    
    if ((feeds == null) || (feeds.isEmpty())) {
      return false;
    }
    
    try
    {
      Util.printFirstDateLastDateAndFeedIds("BEFORE SORT", feeds, Level.FINER);
      
      if (!this.isRearrangeOutput())
      {
        cachedFeeds = feeds.size() <= cacheLimit ? feeds : feeds.subList(0, cacheLimit);
      }
      else
      {
        cachedFeeds = ensureEquality(feeds, cacheLimit);
      }
    } catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "Error apply distribution logic to feeds", getClass(), e);
      cachedFeeds = feeds.size() <= cacheLimit ? feeds : feeds.subList(0, cacheLimit);
    }finally{
      if(cachedFeedsJson == null) {
        cachedFeedsJson = new HashMap<>(cachedFeeds.size(), 1.0f);
      }else{
        cachedFeedsJson.clear();
      }  
    }

    XLogger.getInstance().log(Level.FINE, "Updated cache with {0} feeds", getClass(), cachedFeeds == null ? null : Integer.valueOf(cachedFeeds.size()));
    Util.printFirstDateLastDateAndFeedIds("AFTER UPDATING CACHE", feeds, Level.FINER);
    
    lastTime = System.currentTimeMillis();
    
    return true;
  }
  
  protected List<Feed> ensureEquality(List<Feed> feeds, int outputSize)
  {
    Util.printFirstDateLastDateAndFeedIds("BEFORE REARRANGE", feeds, Level.FINER);
    
    FeedFrequency ff = new FeedFrequency(feeds);
    int numOfSites = ff.getSiteCount();

    final int multiple = 2;
    
    if ((outputSize > numOfSites) && (feeds.size() > numOfSites * multiple))
    {
      int ave = outputSize / numOfSites;
      if (ave < 1) {
        ave = 1;
      }
      
      int max = ave * 2;
      
      IntegerArray siteIds = new IntegerArray(numOfSites);
      IntegerArray siteCounts = new IntegerArray(numOfSites);
      
      Iterator<Feed> iter = feeds.iterator();
      
      List<Feed> appendAtEnd = null;
      
      int index = 0;
      
      while (iter.hasNext())
      {
        index++;
        
        Feed feed = (Feed)iter.next();
        
        Site site = feed.getSiteid();
        
        if (site == null) {
          XLogger.getInstance().log(Level.WARNING, "No site found for Feed:: ID: {0}, title: {1}", getClass(), feed.getFeedid(), feed.getTitle());
        }
        else
        {

          int siteid = site.getSiteid().intValue();
          
          int siteIndex = siteIds.indexOf(siteid);
          
          int siteCount;
          
          if (siteIndex == -1) {
            siteCount = 0;
            siteIds.add(siteid);
            siteCounts.add(++siteCount);
          } else {
            siteCount = siteCounts.get(siteIndex);
            assert (siteCount > 0) : ("Expected count > 0 found: " + siteCount);
            siteCounts.set(siteIndex, ++siteCount);
          }
          
          if (siteCount >= max)
          {
            XLogger.getInstance().log(Level.FINER, "Index: {0}. Site: {1}, has appeared {2} times", getClass(), Integer.valueOf(index), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), Integer.valueOf(siteCount));
            
            iter.remove();
            
            if (appendAtEnd == null) {
              appendAtEnd = new ArrayList(ave * 2);
            }
            
            XLogger.getInstance().log(Level.FINEST, "Relocating Feed. ID: {0}, Date: {1}", getClass(), feed.getFeedid(), feed.getFeeddate());
            
            appendAtEnd.add(feed);
          }
        }
      }
      if ((appendAtEnd != null) && (!appendAtEnd.isEmpty()))
      {
        feeds.addAll(appendAtEnd);
      }
      
      Util.printFirstDateLastDateAndFeedIds("AFTER REARRANGE", feeds, Level.FINER);
    }
    
    return feeds.size() <= outputSize ? feeds : feeds.subList(0, outputSize);
  }

  public static boolean isCachedFeedsAvailable()
  {
    return (cachedFeeds != null) && (!cachedFeeds.isEmpty());
  }
  
  public static List<Feed> getCachedFeeds() {
    return cachedFeeds;
  }

  public static Map<Integer, CharSequence> getCachedFeedsJson() {
    return cachedFeedsJson;
  }
}
