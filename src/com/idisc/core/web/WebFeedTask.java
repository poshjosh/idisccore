package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.IdiscApp;
import com.scrapper.CapturerApp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

public class WebFeedTask extends ConcurrentTaskList {
    
  private static int siteOffset;
  
  public WebFeedTask(long timeout, TimeUnit timeUnit) {
    super(timeout, timeUnit);
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
  protected void beforeShutdown() {
    IdiscApp app = IdiscApp.getInstance();
    Configuration config = app.getConfiguration();
    final int maxFailsAllowed = config.getInt("maxFailsAllowedPerSite", 9);
    final long timeoutPerSiteSeconds = getTimePerSiteSeconds(config, getMaxConcurrent());
    final long timeoutMillis = getTimeoutMillis();
    if ((timeoutPerSiteSeconds > 0L) && (timeoutPerSiteSeconds < timeoutMillis)) {
      boolean scheduled = false;
      final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
      try {
        TaskTerminator terminator = new TaskTerminator(timeoutPerSiteSeconds, maxFailsAllowed);
        long interval = timeoutPerSiteSeconds / getTaskNames().size();
        if (interval < 1000L) {
          interval = 1000L;
        }
        ses.scheduleWithFixedDelay(terminator, interval, interval, TimeUnit.MILLISECONDS);
        scheduled = true;
      } finally { 
        if (scheduled) {
          Thread separateProcess = new Thread()
          {
            @Override
            public void run() {
              Util.shutdownAndAwaitTermination(ses, timeoutMillis, TimeUnit.MILLISECONDS);
            }
          };
          separateProcess.start();
        }
      }
    }
  }
  
  private long getTimePerSiteSeconds(Configuration config, int maxConcurrent) {
    long __timePerTaskSeconds = config.getLong("timeoutPerSiteSeconds", computeDefaultTimePerTask(maxConcurrent));
    long timePerTask = TimeUnit.SECONDS.toMillis(__timePerTaskSeconds);
    return timePerTask;
  }
  
  private long computeDefaultTimePerTask(int maxConcurrent) {
    List<String> taskNames = getTaskNames();
    int taskCount = taskNames.size();
    
    long timeoutMillis = getTimeoutMillis();
    long timePerTask; 
    if (taskCount > maxConcurrent) {
      long factor = taskCount / maxConcurrent;
      timePerTask = timeoutMillis / factor;
    } else {
      timePerTask = timeoutMillis;
    }
    return timePerTask;
  }
  

  @Override
  public NewsCrawler createNewTask(String site)
  {
    JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(site);
    
    NewsCrawler crawler = new NewsCrawler(config, getResult())
    {
      @Override
      public String getTaskName() {
        return "Extract Web Feeds from " + getSitename();
      }
      
      @Override
      public boolean isResume() {
        return !WebFeedTask.this.isAcceptDuplicates();
      }
      
    };

    String url = config.getString(new Object[] { "url", "start" });
    
    crawler.setStartUrl(url);
    
XLogger.getInstance().log(Level.FINER, "Created task {0} for {1}", 
        this.getClass(), crawler.getClass().getName(), site);

    return crawler;
  }
  
  @Override
  public List<String> getTaskNames()
  {
    CapturerApp cap = IdiscApp.getInstance().getCapturerApp();
    List<String> _tn = new ArrayList(new HashSet(cap.getConfigFactory().getSitenames()));
    _tn.remove(cap.getDefaultConfigname());
    return _tn;
  }
  
  private class TaskTerminator implements Runnable {
    private final int maxFailsAllowed;
    private final long timePerTask;
    
    private TaskTerminator(long timePerTask, int maxFailsAllowed) { 
      this.timePerTask = timePerTask;
      this.maxFailsAllowed = maxFailsAllowed;
    }
    
    @Override
    public void run() {
      StoppableTask[] tasks = WebFeedTask.this.getTasks();
      Future[] futures = WebFeedTask.this.getFutures();
      for (int i = 0; i < tasks.length; i++) {
        try {
          NewsCrawler task = (NewsCrawler)tasks[i];
          Future future = futures[i];
          process(task, future);
        } catch (Exception e) {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
        }
      }
    }
    
    private void process(NewsCrawler task, Future future) {
        
      try {
          
        final long timeSpent = System.currentTimeMillis() - task.getStartTime();
        
        final Set<String> failed = task.getFailed();
        
        final int failedCount = failed == null ? 0 : failed.size();
        
        XLogger.getInstance().log(Level.FINER, "Task: {0}, fails: {1}", getClass(), task.getTaskName(), failedCount);
        
        if (((task.isStarted()) && (!task.isCompleted()) && (timeSpent >= this.timePerTask)) || ((this.maxFailsAllowed > 0) && (failedCount > this.maxFailsAllowed))) {

          if ((!task.isStopRequested()) && (!future.isCancelled())) {
              
            XLogger.getInstance().log(Level.FINE, "Stopping task: {0}, Time spent: {1}, fails: {2}", 
                    getClass(), task.getTaskName(), timeSpent, failedCount);
            
            try {
              task.stop();
            } finally {
              future.cancel(true);
            }
          }
        }
      } catch (Exception e) {
        XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
      }
    }
  }
}
