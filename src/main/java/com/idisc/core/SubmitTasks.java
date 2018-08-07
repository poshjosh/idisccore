package com.idisc.core;

import com.bc.task.AbstractStoppableTask;
import com.bc.util.Util;
import com.bc.util.concurrent.NamedThreadFactory;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Logger;
import com.idisc.core.extraction.ScrapContext;
import com.idisc.core.extraction.ScrapSiteTask;

public class SubmitTasks<SOURCE_DATA_TYPE, TASK_RESULT_TYPE>
  extends AbstractStoppableTask<Integer> implements Serializable {

  private transient static final Logger LOG = Logger.getLogger(SubmitTasks.class.getName());
  
  private final long timeout;

  private final TimeUnit timeoutUnit;
  
  private final int maxConcurrent;
  
  private final Collection<String> tasknames;
  
  private final Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT_TYPE>> taskProvider;

  public SubmitTasks(ScrapContext<SOURCE_DATA_TYPE, TASK_RESULT_TYPE> scrapContext) {
      this(scrapContext.getNextNames(),
              scrapContext.getTaskProvider(),
              scrapContext.getConfig().getTimeout(),
              scrapContext.getConfig().getTimeUnit(),
              scrapContext.getConfig().getMaxConcurrentUnits());
  }
  
  public SubmitTasks(
          Collection<String> tasknames,
          Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT_TYPE>> taskProvider,
          long timeout, TimeUnit timeUnit, int maxConcurrent) {
    this.tasknames = Objects.requireNonNull(tasknames);
    this.taskProvider = Objects.requireNonNull(taskProvider);
    this.timeout = timeout;
    this.timeoutUnit = Objects.requireNonNull(timeUnit);
    this.maxConcurrent = maxConcurrent;
  }
  
  @Override
  public String getTaskName() {
    return this.getClass().getSimpleName();
  }

  @Override
  protected Integer doCall() {
      
    final int threadCount = maxConcurrent > tasknames.size() ? tasknames.size() : maxConcurrent;
    
    final ExecutorService es = Executors.newFixedThreadPool(threadCount,
            new NamedThreadFactory("Extract_" + tasknames + "_ThreadFactory"));
    
    LOG.info(() -> MessageFormat.format(
            "Timeout: {0} {1}, Max concurrent: {2}, Tasks: {3}", 
            timeout, timeoutUnit.name().toLowerCase(), threadCount, tasknames));
    
    int submitted = 0;
    
    try {
        
      for (String taskname : tasknames) {
          
        if(this.isTimedout(timeoutUnit.toMillis(timeout)) || this.isStopRequested()) {
            break;
        }  
          
        final Runnable task = taskProvider.apply(taskname);
        
        LOG.fine(() -> "Submitting task: " + task.getClass().getName());
        
        es.submit(task);
       
        ++submitted;
      }
    } finally {
      Util.shutdownAndAwaitTermination(es, this.timeout, timeoutUnit);  
    }
    
    return submitted;
  }
}
