package com.idisc.core.rss;

import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.comparator.site.IncrementableValues;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RSSFeedTask extends ConcurrentTaskList<Feed> {
    
  private final boolean acceptDuplicates;
  
  private final long timeoutEach;
  
  private final TimeUnit timeunitEach;
  
  private Properties feedProperties;
  
  public RSSFeedTask(
      long timeout, TimeUnit timeUnit, 
      long timeoutEach, TimeUnit timeunitEach, 
      int maxConcurrent, boolean acceptDuplicates) {
    super(timeout, timeUnit, maxConcurrent);
    this.acceptDuplicates = acceptDuplicates;
    this.timeoutEach = timeoutEach;
    this.timeunitEach = timeunitEach;
    this.feedProperties = new RSSMgr().getFeedNamesProperties();
  }
  
  @Override
  public List<String> getTaskNames() {
    Set<String> feedNames = this.feedProperties.stringPropertyNames();
    return new ArrayList(feedNames);
  }
  
  @Override
  public StoppableTask createNewTask(final String feedName) {
      
    XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);
    
    StoppableTask task = new RSSFeedDownloadTask(
            feedName, this.feedProperties.getProperty(feedName), 
            this.timeoutEach, this.timeunitEach, 
            this.acceptDuplicates, this.getResult()){
      @Override
      protected Object doCall() {
        try{
          return super.doCall();
        }finally{
          try{
            ((IncrementableValues<String>)getTasknameSorter()).incrementAndGet(feedName, this.getAdded());
          }catch(ClassCastException ignored) { }
        }
      }
    };
    
    return task;
  }

  public Properties getFeedProperties() {
    return feedProperties;
  }

  public void setFeedProperties(Properties feedProperties) {
    this.feedProperties = feedProperties;
  }
}
