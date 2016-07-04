package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.core.rss.RSSFeedTask;
import com.idisc.core.web.WebFeedTask;
import com.idisc.pu.entities.Feed;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

public class FeedUpdateTask  implements Runnable {
  
  public FeedUpdateTask(){ }
  
  public FeedCache createFeedCache() {
    return new FeedCache();
  }
  
  @Override
  public void run() {
     
    try{
        
        this.downloadFeeds();

        this.sleep();

        this.archiveFeeds();

        this.sleep();

        this.updateFeedCache();
        
    }catch(RuntimeException e) {
     
        XLogger.getInstance().log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), this.getClass(), e);
    }
  }
  
  private void sleep() {
    final long interval = this.getIntervalMillis();
    if(interval > 0) {
      Runtime.getRuntime().gc();
      this.sleep(interval);
    }        
  }
  
  private void sleep(long sleepTime) {
    if(sleepTime > 0) {
      final Thread curr = Thread.currentThread();
      try { 
        Thread.sleep(sleepTime); 
      } catch (InterruptedException e) { 
        XLogger.getInstance().log(Level.WARNING, "Interrupted, thread: " + curr.getName() + "#id: " + curr.getId(), getClass(), e);
        curr.interrupt();
      }
    }
  }
  
  public long getIntervalMillis() {
    return 0;
  }

  public boolean downloadFeeds(){
    try {
      
      XLogger.getInstance().log(Level.FINE, "Downloading feeds", getClass());
      
      final FeedResultUpdater updater = new FeedResultUpdater();
      
      final boolean acceptDuplicateLinks = false;
      final int maxConcurrent = (int)this.getLongProperty(ConfigNames.MAXCONCURRENT, 3);
      final int maxFailsAllowed = (int)this.getLongProperty(ConfigNames.MAX_FAILS_ALLOWED, 9);
      
      final long webTimeout = this.getLongProperty(ConfigNames.WEB_TIMEOUT_PER_TASK_SECONDS, 600);
      final long webTimeoutEach = this.getLongProperty(ConfigNames.WEB_TIMEOUT_PER_SITE_SECONDS, 180);
      final WebFeedTask webFeedTask = new WebFeedTask(
              webTimeout, TimeUnit.SECONDS, 
              webTimeoutEach, TimeUnit.SECONDS, 
              maxConcurrent, maxFailsAllowed, acceptDuplicateLinks);
      
      Collection<Feed> webFeeds = webFeedTask.call();
      updater.process("Web Feeds", webFeeds);
      
      final long rssTimeout = this.getLongProperty(ConfigNames.RSS_TIMEOUT_PER_TASK_SECONDS, 300);
      final long rssTimeoutEach = this.getLongProperty(ConfigNames.RSS_TIMEOUT_PER_SITE_SECONDS, 90);
      final RSSFeedTask rssFeedTask = new RSSFeedTask(
              rssTimeout, TimeUnit.SECONDS, 
              rssTimeoutEach, TimeUnit.SECONDS, maxConcurrent, acceptDuplicateLinks);
      
      final Collection<Feed> rssFeeds = rssFeedTask.call();
      updater.process("RSS Feeds", rssFeeds);
      
      XLogger.getInstance().log(Level.FINE, "Done downloading feeds", getClass());

      return true;
      
    }catch (Exception e) {
        
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception while downloading feeds", getClass(), e);
      
      return false;
    }
  }
  
  public int archiveFeeds() {
    try {
      XLogger.getInstance().log(Level.FINE, "Archiving feeds", getClass());
        
      Configuration config = IdiscApp.getInstance().getConfiguration();
      
      long maxAge = config.getLong("maxFeedAgeDays");
      
      int batchSize = config.getInt("archiveBatchSize");
      
      final int updateCount = new FeedArchiver().archiveFeeds(maxAge, TimeUnit.DAYS, batchSize);
      
      XLogger.getInstance().log(Level.FINE, "Done archiving feeds", getClass());

      return updateCount;
      
    }catch (RuntimeException e) {
        
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception while archiving feeds", getClass(), e);
      
      return 0;
    }
  }

  public boolean updateFeedCache() {
    try {
      XLogger.getInstance().log(Level.FINE, "Updating feed cache", getClass());  
      this.createFeedCache().updateCache();
      XLogger.getInstance().log(Level.FINE, "Done updating feed cache", getClass());
      return true;
    } catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception while updating feed cache", getClass(), e); 
      return false;
    }
  }

  private long getLongProperty(String key, long defaultValue) {
    Configuration config = IdiscApp.getInstance().getConfiguration();
    final long value = config.getLong(key, defaultValue);
    XLogger.getInstance().log(Level.FINE, "{0} = {1}", getClass(), key, value);
    return value;
  }
  
}
