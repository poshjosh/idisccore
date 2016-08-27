package com.idisc.core;

import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.io.Serializable;

public class SiteFrequency implements Serializable {
    
  private final IntDict dict;
  
  public SiteFrequency(int bufferSize) { 
    this.dict = new IntDict(bufferSize);
  }
  
  public int getSiteCount() {
    return dict.size();
  }
  
  public int getSiteFrequency(Feed feed, int outputIfNone) {
      
    return this.getSiteFrequency(feed.getSiteid(), outputIfNone);
  }
  
  public int getSiteFrequency(Site site, int outputIfNone) {
      
      return dict.get(site.getSiteid(), outputIfNone);
  }
  
  public int increment(Feed feed){
      
    return this.increment(feed.getSiteid());
  }

  public int increment(Site site){
   
    return this.update(site, 1, true);
  }
  
  private int update(Site site, int amount, boolean ifTrueAddOtherwiseRemove){
      
    Integer siteid = site.getSiteid();
    
    int sitefreq = dict.get(siteid, -1);
    
    if(sitefreq == -1) {
      sitefreq = 0;
      sitefreq = ifTrueAddOtherwiseRemove ? sitefreq + amount : sitefreq - amount;
      dict.add(siteid, sitefreq);
    }else{
      sitefreq = ifTrueAddOtherwiseRemove ? sitefreq + amount : sitefreq - amount;  
      dict.put(siteid, sitefreq, -1);  
    }
    
    return sitefreq;
  }
}
