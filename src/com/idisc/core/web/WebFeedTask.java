package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.IdiscApp;
import com.idisc.core.comparator.site.IncrementableValues;
import com.idisc.pu.entities.Feed;
import com.scrapper.CapturerApp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class WebFeedTask extends ConcurrentTaskList<Feed> {
    
  private final boolean acceptDuplicateUrls;
  
  private final long timeoutEach;
  
  private final TimeUnit timeunitEach;
  
  private final int maxFailsAllowed;
  
  public WebFeedTask(
      long timeout, TimeUnit timeUnit, 
      long timeoutEach, TimeUnit timeunitEach, 
      int maxConcurrent, int maxFailsAllowed, boolean acceptDuplicateUrls) {
      
    super(timeout, timeUnit, maxConcurrent);
    
    this.acceptDuplicateUrls = acceptDuplicateUrls;
    this.timeoutEach = timeoutEach;
    this.timeunitEach = timeunitEach;
    this.maxFailsAllowed = maxFailsAllowed;
  }
  
  @Override
  public List<String> getTaskNames() {
    CapturerApp cap = IdiscApp.getInstance().getCapturerApp();
    List<String> _tn = new ArrayList(new HashSet(cap.getConfigFactory().getSitenames()));
    _tn.remove(cap.getDefaultConfigname());
    return _tn;
  }

  @Override
  public NewsCrawler createNewTask(final String site) {
      
    JsonConfig config = CapturerApp.getInstance().getConfigFactory().getContext(site).getConfig();
    
    Objects.requireNonNull(config, JsonConfig.class.getSimpleName()+" for site: "+site+" is null");
    
    NewsCrawler crawler = new NewsCrawler(
            config, this.timeoutEach, this.timeunitEach, this.maxFailsAllowed, 
            getResult(), false, !WebFeedTask.this.acceptDuplicateUrls) {
      @Override
      protected Collection<Feed> doCall() {
        try{
          return super.doCall();
        }finally{
          try{
            ((IncrementableValues<String>)getTasknameSorter()).incrementAndGet(site, this.getScrapped());
          }catch(ClassCastException ignored) { }
        }
      }
      @Override
      public String getTaskName() {
        return this.getClass().getSimpleName() + " for " + this.getContext().getConfig().getName();
      }
    };

    String url = config.getString(new Object[] { "url", "start" });
    
    crawler.setStartUrl(url);
    
XLogger.getInstance().log(Level.FINER, "Created task {0} for {1}", 
        this.getClass(), crawler.getClass().getName(), site);

    return crawler;
  }
}

/**
 * 
  @Override
  protected void beforeShutdown() {
    IdiscApp app = IdiscApp.getInstance();
    Configuration config = app.getConfiguration();
    
    final long timeoutPerSiteSeconds = getTimePerSiteSeconds(config, getMaxConcurrent());
    final long timeoutMillis = this.getTimeoutUnit().toMillis(this.getTimeout());
    if ((timeoutPerSiteSeconds > 0L) && (timeoutPerSiteSeconds < timeoutMillis)) {
      final int maxFailsAllowed = config.getInt("maxFailsAllowedPerSite", 9);  
      boolean scheduled = false;
      final String poolName = WebFeedTask.class.getSimpleName()+"_"+TaskTerminator.class.getSimpleName()+
              "[timeout: "+timeoutPerSiteSeconds+", max fails: "+maxFailsAllowed+"]";
      final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor(
          new NamedThreadFactory(poolName));
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
    long __timePerTaskSeconds = config.getLong(ConfigNames.WEB_TIMEOUT_PER_SITE_SECONDS, computeDefaultTimePerTask(maxConcurrent));
    long timePerTask = TimeUnit.SECONDS.toMillis(__timePerTaskSeconds);
    return timePerTask;
  }
  
  private long computeDefaultTimePerTask(int maxConcurrent) {
    List<String> taskNames = getTaskNames();
    int taskCount = taskNames.size();
    
    long timeoutMillis = this.getTimeoutUnit().toMillis(this.getTimeout());
    long timePerTask; 
    if (taskCount > maxConcurrent) {
      long factor = taskCount / maxConcurrent;
      timePerTask = timeoutMillis / factor;
    } else {
      timePerTask = timeoutMillis;
    }
    return timePerTask;
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
      try{
      StoppableTask[] tasks = WebFeedTask.this.getTasks();
      Future[] futures = WebFeedTask.this.getFutures();
      for (int i = 0; i < tasks.length; i++) {
        try {
          NewsCrawler task = (NewsCrawler)tasks[i];
          Future future = futures[i];
          process(task, future);
        } catch (Exception e) {
          XLogger.getInstance().log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), getClass(), e);
        }
      }
      }catch(Exception e) {
        XLogger.getInstance().log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), getClass(), e);  
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
 * 
 */
