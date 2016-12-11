package com.idisc.core;

import com.bc.task.AbstractStoppableTask;
import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.bc.util.concurrent.NamedThreadFactory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import com.idisc.core.comparator.Sorter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.commons.configuration.Configuration;

public abstract class ConcurrentTaskList<E>
  extends AbstractStoppableTask<Map<String, E>>      
  implements Serializable, Sorter<String> {
   
  private static Sorter<String> tasknameSorter;  
    
  private final long timeout;
  private final TimeUnit timeoutUnit;
  private final int maxConcurrent;
  private final Map<String, E> result;

  private StoppableTask[] tasks;
  private Future[] futures;
  
  public ConcurrentTaskList(long timeout, TimeUnit timeUnit, int maxConcurrent) {
    this.timeout = timeout;
    this.timeoutUnit = timeUnit;
    this.maxConcurrent = maxConcurrent;
    this.result = Collections.synchronizedMap(new HashMap());
  }
  
  public abstract StoppableTask createNewTask(String paramString);
  
  public abstract List<String> getTaskNames();

  @Override
  public String getTaskName() {
    return this.getClass().getName();
  }

  @Override
  protected Map<String, E> doCall() {
      
    ExecutorService es = Executors.newFixedThreadPool(this.maxConcurrent,
            new NamedThreadFactory(this.getClass().getName()+"_ThreadPool"));
    
    List<String> siteNames = getTaskNames();
    
    if (this.maxConcurrent < siteNames.size()) {
        
      siteNames = sort(siteNames);
    }
    
XLogger.getInstance().log(Level.FINER, "Timeout: {0} {1}, Task count: {2}, max concurrent tasks: {3}", 
        getClass(), timeout, timeoutUnit, siteNames.size(), this.maxConcurrent);
    
    this.tasks = new StoppableTask[siteNames.size()];
    
    this.futures = new Future[siteNames.size()];
    
    try {
        
      for (int i = 0; i < siteNames.size(); i++) {
          
        StoppableTask task = createNewTask((String)siteNames.get(i));
        
        Future future = es.submit((Callable)task);
        
        this.tasks[i] = task;
        
        this.futures[i] = future;
      }
    } finally {
      Util.shutdownAndAwaitTermination(es, this.timeout, timeoutUnit);  
    }
    
    return this.result;
  }
  
  @Override
  public List<String> sort(List<String> values) {
    
    final List<String> output;
    
    final Sorter<String> sorter = this.getTasknameSorter();
    
    if(sorter != null) {
        
      output = sorter.sort(values);
      
XLogger.getInstance().log(Level.FINE, "{0}\n Input: {1}\nOutput: {2}", 
        this.getClass(), sorter, values, output);

    }else{
        
      output = values;  
    }
    
    return output;
  }
  
  public Sorter<String> getTasknameSorter() {
    if(tasknameSorter == null) {
      tasknameSorter = this.createTasknameSorter(null);
    }
    return tasknameSorter;
  }
  
  public Sorter<String> createTasknameSorter(Sorter<String> defaultInstance) {
      Sorter<String> output;
      final Configuration config = IdiscApp.getInstance().getConfiguration();  
      final String className = config.getString(ConfigNames.COMPARATOR_SITE_CLASSNAME);
      if(className != null) {
        try{
          output = (Sorter<String>)Class.forName(className).getConstructor().newInstance();
        }catch(ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | 
        IllegalAccessException | IllegalArgumentException | InvocationTargetException | ClassCastException e) {
          output = defaultInstance;
          XLogger.getInstance().log(Level.WARNING, 
          "Exception calling: (Sorter<String>)Class.forName('{0}').getConstructor().newInstance().\n{1}", 
          this.getClass(), className, e.toString());
        }
      }else{
        output = defaultInstance;
      }
      if(output == defaultInstance) {
        final String type = config.getString(ConfigNames.COMPARATOR_SITE_TYPE);
        if(type != null) {
          switch(type) {
            default:    
          }
        }
      }
      return output;
  }
  
  public StoppableTask[] getTasks() {
    return this.tasks;
  }
  
  public Future[] getFutures() {
    return this.futures;
  }
  
  public int getMaxConcurrent() {
    return this.maxConcurrent;
  }
  
  public long getTimeout() {
    return timeout;
  }

  public TimeUnit getTimeoutUnit() {
    return timeoutUnit;
  }

  protected final Map<String, E> getResult() {
    return result;
  }
}
