package com.idisc.core;

import com.bc.util.IntegerArray;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.util.List;
import java.util.logging.Level;














public class FeedFrequency
{
  private final IntegerArray siteIds;
  private final IntegerArray siteFrequencies;
  
  public FeedFrequency()
  {
    this.siteIds = new IntegerArray();
    this.siteFrequencies = new IntegerArray();
  }
  
  public FeedFrequency(List<Feed> feeds) {
    this();
    setFeeds(feeds);
  }
  
  public void setFeeds(List<Feed> feeds) {
    if (feeds == null) {
      throw new NullPointerException();
    }
    if (!feeds.isEmpty()) {
      this.siteIds.clear();
      this.siteFrequencies.clear();
      for (Feed feed : feeds) {
        updateSiteFrequency(feed);
      }
    }
  }
  
  public int getSiteCount() {
    return this.siteFrequencies == null ? 0 : this.siteFrequencies.size();
  }
  
  public int getSiteFrequency(Feed feed) {
    Site site = feed.getSiteid();
    if ((site == null) || (this.siteIds == null) || (this.siteFrequencies == null)) {
      return -1;
    }
    int pos = this.siteIds.indexOf(site.getSiteid().intValue());
    return this.siteFrequencies.get(pos);
  }
  
  private int updateSiteFrequency(Feed feed)
  {
    Site site = feed.getSiteid();
    
    if (site == null) {
      XLogger.getInstance().log(Level.WARNING, "No site found for Feed:: ID: {0}, title: {1}", getClass(), feed.getFeedid(), feed.getTitle());
      

      return -1;
    }
    
    int siteid = site.getSiteid().intValue();
    
    int index = this.siteIds.indexOf(siteid);
    
    int siteFreq;
    
    if (index == -1) {
      siteFreq = 0;
      this.siteIds.add(siteid);
      this.siteFrequencies.add(++siteFreq);
    } else {
      siteFreq = this.siteFrequencies.get(index);
      this.siteFrequencies.set(index, ++siteFreq);
    }
    
    return siteFreq;
  }
}
