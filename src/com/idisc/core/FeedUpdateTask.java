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

public class FeedUpdateTask
  implements Runnable
{
  public static Level LOG_LEVEL = Level.FINER;
  private long _flt;
  
  public long getFeedLoadTimeoutSeconds() {
    if (this._flt < 1L) {
      Configuration config = IdiscApp.getInstance().getConfiguration();
      this._flt = config.getLong("timeoutPerTaskSeconds", 300L);
      XLogger.getInstance().log(Level.FINE, "Feed load timeout: {0} seconds", getClass(), Long.valueOf(this._flt));
    }
    return this._flt;
  }
  

  @Override
  public void run()
  {
    downloadFeeds();
    
    archiveFeeds();
  }

  public boolean downloadFeeds()
  {
    try
    {
      long feedLoadTimeoutSeconds = getFeedLoadTimeoutSeconds();
      
      Map<String, TaskHasResult<Collection<Feed>>> tasks = new HashMap(3, 1.0F);
      
      tasks.put("Web Feeds", new WebFeedTask(feedLoadTimeoutSeconds, TimeUnit.SECONDS));
      tasks.put("RSS Feeds", new RSSFeedTask(feedLoadTimeoutSeconds, TimeUnit.SECONDS));
      
      ExecutorService es = Executors.newFixedThreadPool(tasks.size());
      try
      {
        for (TaskHasResult task : tasks.values()) {
          es.submit(task);
        }
      }
      finally {
        Util.shutdownAndAwaitTermination(es, feedLoadTimeoutSeconds, TimeUnit.SECONDS);
      }
      
      FeedResultUpdater updater = new FeedResultUpdater();
      
      for (String name : tasks.keySet())
      {
        TaskHasResult<Collection<Feed>> task = (TaskHasResult)tasks.get(name);
        
        updater.process(name, (Collection)task.getResult());
      }
      
      return true;
    }
    catch (Exception e)
    {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
    }
    return false;
  }
  
  public int archiveFeeds()
  {
    try
    {
      Configuration config = IdiscApp.getInstance().getConfiguration();
      
      long maxAge = config.getLong("maxFeedAgeDays");
      
      int batchSize = config.getInt("archiveBatchSize");
      
      return new FeedArchiver().archiveFeeds(maxAge, TimeUnit.DAYS, batchSize);
    }
    catch (RuntimeException e)
    {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
    }
    return 0;
  }
}
