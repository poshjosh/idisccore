package com.idisc.core;

import com.idisc.core.util.Util;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import com.bc.jpa.JpaContext;
import com.idisc.pu.entities.Feed_;
import java.util.Collection;

public class FeedService {
    
  private final JpaContext jpaContext;
  
  private final int limit;
  
  private final boolean spreadOutput;

  public FeedService(JpaContext jpaContext, int limit, boolean spreadOutput) { 
    this.jpaContext = jpaContext;
    this.limit = limit;
    this.spreadOutput = spreadOutput;
  }
  
  public List<Feed> getFeeds() {
    return this.getFeeds(this.limit);
  }
  
  public List<Feed> getFeeds(int max) {
      
    final List<Feed> output;
    
XLogger.getInstance().entering(this.getClass(), "#getFeeds(int)", "");
      
    List<Feed> loadedFeeds = this.selectFeeds();
    
    if (loadedFeeds == null || loadedFeeds.isEmpty()) {
        
      output = Collections.EMPTY_LIST;
      
    }else if(loadedFeeds.size() <= max) {
     
      output = Collections.unmodifiableList(loadedFeeds);  
      
    }else{
       
      this.printFirstDateLastDateAndFeedIds(Level.FINER, "BEFORE", loadedFeeds);
      
      if (this.spreadOutput) {
        output = Collections.unmodifiableList(spreadOutput(loadedFeeds, max));  
      }else{
        output = Collections.unmodifiableList(truncate(loadedFeeds, max));  
      }
      
      XLogger.getInstance().log(Level.FINE, "Loaded {0} feeds", getClass(), sizeOf(output));
    
      this.printFirstDateLastDateAndFeedIds(Level.FINER, "AFTER", output);
    }
    
    return output;
  }
  
  protected List<Feed> selectFeeds() {
    List<Feed> loadedFeeds = this.jpaContext.getBuilderForSelect(Feed.class)
            .descOrder(Feed.class, Feed_.feedid.getName())
            .getResultsAndClose(0, this.limit);
            
    return loadedFeeds;
  }
  
  public List<Feed> spreadOutput(List<Feed> feeds, int outputSize) {
    return new SpreadBySite().spread(feeds, outputSize);      
  }
  
  protected List<Feed> truncate(List<Feed> feeds, int limit) {
    return feeds.size() <= limit ? feeds : feeds.subList(0, limit);  
  }
  
  protected int sizeOf(Collection<Feed> feeds) {
    return feeds == null ? 0 : feeds.size();
  }
  
  protected void printFirstDateLastDateAndFeedIds(Level level, String key, List<Feed> feeds) {
    if (XLogger.getInstance().isLoggable(level, this.getClass()) && feeds != null && !feeds.isEmpty()) {
      Feed first = (Feed)feeds.get(0);
      Feed last = (Feed)feeds.get(feeds.size() - 1);
      XLogger.getInstance().log(level, "{0}. First feed, date: {1}. Last feed, date: {2}\n{3}", 
              this.getClass(), key, first.getFeeddate(), last.getFeeddate(), Util.toString(feeds));
    }
  }

  public final JpaContext getJpaContext() {
    return jpaContext;
  }

  public final int getLimit() {
    return limit;
  }

  public final boolean isSpreadOutput() {
    return spreadOutput;
  }
}
