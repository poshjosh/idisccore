package com.idisc.core;

import com.bc.util.Util;
import com.bc.util.XLogger;
import com.idisc.core.rss.RSSFeedTask;
import com.idisc.core.web.WebFeedTask;
import com.idisc.pu.entities.Feed;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

public class FeedUpdateTask  implements Runnable {
    
  public long getFeedLoadTimeoutSeconds(String key) {
    Configuration config = IdiscApp.getInstance().getConfiguration();
    long flt = config.getLong(key, 180L);
    XLogger.getInstance().log(Level.FINE, "Feed load timeout: {0} seconds", getClass(), flt);
    return flt;
  }
  
  @Override
  public void run() {
      
    this.downloadFeeds();
    
    this.archiveFeeds();
    
    this.updateFeedCache();
  }

  public boolean downloadFeeds(){
    try {
      
      Map<String, TaskHasResult<Collection<Feed>>> tasks = new HashMap(3, 1.0F);
      
      final long webTimeout = this.getFeedLoadTimeoutSeconds(AppProperties.WEB_TIMEOUT_PER_TASK_SECONDS);
      final long rssTimeout = this.getFeedLoadTimeoutSeconds(AppProperties.RSS_TIMEOUT_PER_TASK_SECONDS);
      
      tasks.put("Web Feeds", new WebFeedTask(webTimeout, TimeUnit.SECONDS));
      tasks.put("RSS Feeds", new RSSFeedTask(rssTimeout, TimeUnit.SECONDS));
      
      ExecutorService es = Executors.newFixedThreadPool(tasks.size());
      try {
        for (TaskHasResult task : tasks.values()) {
          es.submit(task);
        }
      } finally {
        Util.shutdownAndAwaitTermination(es, Math.max(webTimeout, rssTimeout), TimeUnit.SECONDS);
      }
      
      FeedResultUpdater updater = new FeedResultUpdater();
      
      for (String name : tasks.keySet()) {
          
        TaskHasResult<Collection<Feed>> task = (TaskHasResult)tasks.get(name);
        
        updater.process(name, (Collection)task.getResult());
      }
      
      return true;
      
    }catch (Exception e) {
        
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
    }
    return false;
  }
  
  public int archiveFeeds() {
    try {
        
      Configuration config = IdiscApp.getInstance().getConfiguration();
      
      long maxAge = config.getLong("maxFeedAgeDays");
      
      int batchSize = config.getInt("archiveBatchSize");
      
      return new FeedArchiver().archiveFeeds(maxAge, TimeUnit.DAYS, batchSize);
      
    }catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
    }
    return 0;
  }

  public boolean updateFeedCache() {
    try {
      new FeedCache().updateCache();
      return true;
    } catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e); 
    }
    return false;
  }
}
