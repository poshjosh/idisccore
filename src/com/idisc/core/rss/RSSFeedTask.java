package com.idisc.core.rss;

import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RSSFeedTask
  extends ConcurrentTaskList
{
  private Properties feedProperties;
  
  public RSSFeedTask(long timeout, TimeUnit timeUnit)
  {
    super(timeout, timeUnit);
    this.feedProperties = new RSSMgr().getFeedNamesProperties();
  }
  
  @Override
  public List<String> getTaskNames()
  {
    Set<String> feedNames = this.feedProperties.stringPropertyNames();
    return new ArrayList(feedNames);
  }
  
  @Override
  public StoppableTask createNewTask(String feedName)
  {
    XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);
    StoppableTask task = new DownloadSyndFeedTask(
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
