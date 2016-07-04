package com.idisc.core;

import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.bc.util.concurrent.NamedThreadFactory;
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

public abstract class ConcurrentTaskList
  implements Serializable, Distributor<String>, TaskHasResult<Collection<Feed>> {
    
  private final long timeout;
  private final TimeUnit timeoutUnit;
  private final int maxConcurrent;
  private StoppableTask[] tasks;
  private Future[] futures;
  private final Collection<Feed> result;
  
  public ConcurrentTaskList(long timeout, TimeUnit timeUnit, int maxConcurrent) {
    this.timeout = timeout;
    this.timeoutUnit = timeUnit;
    this.maxConcurrent = maxConcurrent;
    this.result = Collections.synchronizedCollection(new ArrayList());
  }
  
  public abstract StoppableTask createNewTask(String paramString);
  
  public abstract List<String> getTaskNames();

  @Override
  public final Collection<Feed> call() {
    this.run();
    return this.getResult();
  }
  
  @Override
  public final void run() {
    try {
      doRun();
    } catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), getClass(), e);
    }
  }
  
  protected void doRun() {
      
    ExecutorService es = Executors.newFixedThreadPool(this.maxConcurrent,
            new NamedThreadFactory(this.getClass().getName()+"_ThreadPool"));
    
    List<String> siteNames = getTaskNames();
    
    if (this.maxConcurrent < siteNames.size()) {
        
      siteNames = distribute(siteNames);
    }
    
XLogger.getInstance().log(Level.FINER, "Timeout: {0} {1}, Task count: {2}, max concurrent tasks: {3}", 
        getClass(), timeout, timeoutUnit, siteNames.size(), this.maxConcurrent);
    
    this.tasks = new StoppableTask[siteNames.size()];
    
    this.futures = new Future[siteNames.size()];
    
    try {
        
      for (int i = 0; i < siteNames.size(); i++) {
          
        StoppableTask task = createNewTask((String)siteNames.get(i));
        
        Future future = es.submit(task);
        
        this.tasks[i] = task;
        
        this.futures[i] = future;
      }
    } finally {
      try {
        beforeShutdown();
      } finally {
        Util.shutdownAndAwaitTermination(es, this.timeout, timeoutUnit);
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
  
  public int getMaxConcurrent() {
    return this.maxConcurrent;
  }
  
  @Override
  public Collection<Feed> getResult() {
    return this.result;
  }

  public long getTimeout() {
    return timeout;
  }

  public TimeUnit getTimeoutUnit() {
    return timeoutUnit;
  }
}
