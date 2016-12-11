package com.idisc.core;

import com.bc.jpa.JpaContext;
import com.bc.util.XLogger;
import com.idisc.core.rss.RSSFeedTask;
import com.idisc.core.web.WebFeedTask;
import com.scrapper.config.JsonConfigFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

public class FeedUpdateTask  implements Runnable {
  
  public FeedUpdateTask(){  }
  
  @Override
  public void run() {
     
    try{
        
        this.downloadFeeds();

//        this.sleep();

//        this.archiveFeeds();

    }catch(RuntimeException e) {
     
      this.handleRuntimeException(e);
    }
  }
  
  protected void handleRuntimeException(RuntimeException e) {
    XLogger.getInstance().log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), this.getClass(), e);  
  }
  
  protected void sleep() {
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
      
      final boolean acceptDuplicateLinks = false;
      final int maxConcurrent = (int)this.getLongProperty(ConfigNames.MAXCONCURRENT, 3);
      final int maxFailsAllowed = (int)this.getLongProperty(ConfigNames.MAX_FAILS_ALLOWED, 9);
      
      final long webTimeout = this.getLongProperty(ConfigNames.WEB_TIMEOUT_PER_TASK_SECONDS, 600);
      final long webTimeoutEach = this.getLongProperty(ConfigNames.WEB_TIMEOUT_PER_SITE_SECONDS, 180);
      
      final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
      
      final JsonConfigFactory configFactory = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
      
      final WebFeedTask webFeedTask = new WebFeedTask(
              jpaContext, configFactory,
              webTimeout, TimeUnit.SECONDS, 
              webTimeoutEach, TimeUnit.SECONDS, 
              maxConcurrent, maxFailsAllowed, acceptDuplicateLinks);
      
      final Map<String, Integer> webFeedsUpdateCount = webFeedTask.call();
      
      final long rssTimeout = this.getLongProperty(ConfigNames.RSS_TIMEOUT_PER_TASK_SECONDS, 300);
      final long rssTimeoutEach = this.getLongProperty(ConfigNames.RSS_TIMEOUT_PER_SITE_SECONDS, 90);
      final RSSFeedTask rssFeedTask = new RSSFeedTask(
              jpaContext, rssTimeout, TimeUnit.SECONDS, 
              rssTimeoutEach, TimeUnit.SECONDS, maxConcurrent, acceptDuplicateLinks);
      
      final Map<String, Integer> rssFeedsUpdateCount = rssFeedTask.call();
      
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
      
      final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
      
      final int updateCount = new FeedArchiver(jpaContext).archiveFeedsBefore(maxAge, TimeUnit.DAYS, batchSize);
      
      XLogger.getInstance().log(Level.FINE, "Done archiving feeds", getClass());

      return updateCount;
      
    }catch (RuntimeException e) {
        
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception while archiving feeds", getClass(), e);
      
      return 0;
    }
  }

  private long getLongProperty(String key, long defaultValue) {
    Configuration config = IdiscApp.getInstance().getConfiguration();
    final long value = config.getLong(key, defaultValue);
    XLogger.getInstance().log(Level.FINE, "{0} = {1}", getClass(), key, value);
    return value;
  }
  
}
