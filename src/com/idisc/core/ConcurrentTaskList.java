package com.idisc.core;

import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

public abstract class ConcurrentTaskList
  implements Serializable, Distributor<String>, TaskHasResult<Collection<Feed>>
{
  private boolean acceptDuplicates;
  private long timeoutMillis;
  private int maxConcurrent;
  private StoppableTask[] tasks;
  private Future[] futures;
  private Collection<Feed> result;
  private boolean randomize;
  private static int siteOffset;
  
  public ConcurrentTaskList() {}
  
  public ConcurrentTaskList(long timeout, TimeUnit timeUnit)
  {
    this.timeoutMillis = timeUnit.toMillis(timeout);
    IdiscApp app = IdiscApp.getInstance();
    Configuration config = app.getConfiguration();
    this.maxConcurrent = config.getInt("maxConcurrentSites", 3);
    this.result = Collections.synchronizedCollection(new ArrayList());
  }
  
  public abstract StoppableTask createNewTask(String paramString);
  
  public abstract List<String> getTaskNames();
  
  @Override
  public final void run()
  {
    try {
      doRun();
    } catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
  }
  
  @Override
  public List<String> distribute(List<String> values)
  {
    List<String> copy = new ArrayList(values);
    
    if (this.randomize)
    {
      Collections.shuffle(copy);
      
      return copy;
    }
    
    Collections.rotate(copy, siteOffset);
    
    siteOffset += this.maxConcurrent;

XLogger.getInstance().log(Level.FINER, " Input: {0}\nOutput: {1}", this.getClass(), values, copy);
    return copy;
  }
  
  protected void doRun()
  {
    ExecutorService es = Executors.newFixedThreadPool(this.maxConcurrent);
    
    List<String> siteNames = getTaskNames();
    
    if (this.maxConcurrent < siteNames.size())
    {
      distribute(siteNames);
    }
    
    XLogger.getInstance().log(Level.FINER, "Timeout: {0} minutes, Task count: {1}, max concurrent tasks: {2}", getClass(), Long.valueOf(this.timeoutMillis < 1000L ? 0L : TimeUnit.MILLISECONDS.toMinutes(this.timeoutMillis)), Integer.valueOf(siteNames.size()), Integer.valueOf(this.maxConcurrent));
    
    this.tasks = new StoppableTask[siteNames.size()];
    
    this.futures = new Future[siteNames.size()];
    
    try
    {
      for (int i = 0; i < siteNames.size(); i++)
      {
        StoppableTask task = createNewTask((String)siteNames.get(i));
        
        Future future = es.submit(task);
        
        this.tasks[i] = task;
        
        this.futures[i] = future;
      }
    } finally {
      try {
        beforeShutdown();
      } finally {
        Util.shutdownAndAwaitTermination(es, this.timeoutMillis, TimeUnit.MILLISECONDS);
      }
    }
  }
  
  protected void beforeShutdown() {}
  
  public StoppableTask[] getTasks() {
    return this.tasks;
  }
  
  public Future[] getFutures() {
    return this.futures;
  }
  
  public boolean isAcceptDuplicates() {
    return this.acceptDuplicates;
  }
  
  public void setAcceptDuplicates(boolean acceptDuplicates) {
    this.acceptDuplicates = acceptDuplicates;
  }
  
  public boolean isRandomize() {
    return this.randomize;
  }
  
  public void setRandomize(boolean randomize) {
    this.randomize = randomize;
  }
  
  public long getTimeoutMillis() {
    return this.timeoutMillis;
  }
  
  public void setTimeoutMillis(long timeoutMillis) {
    this.timeoutMillis = timeoutMillis;
  }
  
  public int getMaxConcurrent() {
    return this.maxConcurrent;
  }
  
  public void setMaxConcurrent(int maxConcurrent) {
    this.maxConcurrent = maxConcurrent;
  }
  
  @Override
  public Collection<Feed> getResult()
  {
    return this.result;
  }
  
  public void setResult(Collection<Feed> result) {
    this.result = result;
  }
  
  public static int getDistributionOffet() {
    return siteOffset;
  }
}
