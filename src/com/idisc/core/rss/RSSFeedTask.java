package com.idisc.core.rss;

import com.bc.jpa.JpaContext;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.FeedHandler;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.core.comparator.site.IncrementableValues;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class RSSFeedTask extends ConcurrentTaskList<Integer> {
    
  private final boolean acceptDuplicates;
  
  private final long timeoutEach;
  
  private final TimeUnit timeunitEach;
  
  private final JpaContext jpaContext;
  
  private final Properties feedProperties;
  
  private final List<String> taskNames;

  public RSSFeedTask(
      JpaContext jpaContext, 
      long timeout, TimeUnit timeUnit, 
      long timeoutEach, TimeUnit timeunitEach, 
      int maxConcurrent, boolean acceptDuplicates) {
    this(jpaContext, new RSSMgr().getFeedNamesProperties(),
            timeout, timeUnit, timeoutEach, timeunitEach,
            maxConcurrent, acceptDuplicates);
  }
  
  public RSSFeedTask(
      JpaContext jpaContext, Properties feedProperties,
      long timeout, TimeUnit timeUnit, 
      long timeoutEach, TimeUnit timeunitEach, 
      int maxConcurrent, boolean acceptDuplicates) {
    super(timeout, timeUnit, maxConcurrent);
    this.jpaContext = jpaContext;
    this.acceptDuplicates = acceptDuplicates;
    this.timeoutEach = timeoutEach;
    this.timeunitEach = timeunitEach;
    this.feedProperties = feedProperties;
    this.taskNames = Collections.unmodifiableList(new ArrayList(this.feedProperties.stringPropertyNames()));
  }
  
  @Override
  public List<String> getTaskNames() {
    return this.taskNames;
  }
  
  @Override
  public StoppableTask createNewTask(final String feedName) {
      
    XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);
    
    final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);
    
    StoppableTask task = new RSSFeedDownloadTask(
            feedName, this.feedProperties.getProperty(feedName), 
            this.timeoutEach, this.timeunitEach, 
            this.acceptDuplicates, feedHandler){
      @Override
      protected Integer doCall() {
        try{
            
          final Integer updateCount = super.doCall();
          
          RSSFeedTask.this.getResult().put(feedName, updateCount);
          
          return updateCount;
          
        }finally{
          try{
            ((IncrementableValues<String>)getTasknameSorter()).incrementAndGet(feedName, this.getAdded());
          }catch(ClassCastException ignored) { }
        }
      }
    };
    
    return task;
  }

  public final Properties getFeedProperties() {
    return feedProperties;
  }
}
