package com.idisc.core.rss;

import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class RSSFeedTask extends ConcurrentTaskList {
    
  private static int siteOffset;
  
  private Properties feedProperties;
  
  public RSSFeedTask(long timeout, TimeUnit timeUnit) {
    super(timeout, timeUnit);
    this.feedProperties = new RSSMgr().getFeedNamesProperties();
  }
  
  @Override
  public List<String> distribute(List<String> values) {
      
    List<String> copy = new ArrayList(values);
    
    if (this.isRandomize()) {
        
      Collections.shuffle(copy);
      
      return copy;
    }
    
    Collections.rotate(copy, siteOffset);
    
XLogger.getInstance().log(Level.FINE, "Number of values: {0}, offset: {1}\n Input: {2}\nOutput: {3}", 
        this.getClass(), values.size(), siteOffset, values, copy);
    
    siteOffset += this.getMaxConcurrent();

    return copy;
  }
  
  @Override
  public List<String> getTaskNames() {
    Set<String> feedNames = this.feedProperties.stringPropertyNames();
    return new ArrayList(feedNames);
  }
  
  @Override
  public StoppableTask createNewTask(String feedName)
  {
    XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);
    StoppableTask task = new RSSFeedDownloadTask(
            feedName, this.feedProperties.getProperty(feedName), 
            this.isAcceptDuplicates(), this.getResult());
    return task;
  }

  public Properties getFeedProperties() {
    return feedProperties;
  }

  public void setFeedProperties(Properties feedProperties) {
    this.feedProperties = feedProperties;
  }
}
