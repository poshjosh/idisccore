package com.idisc.core;

import com.bc.util.XLogger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;





















public abstract class Service<E extends Runnable>
{
  private boolean startAttempted;
  private ScheduledExecutorService svc;
  
  public abstract E newTask();
  
  public void startAt(String timeString, TimeZone timeZone)
    throws ParseException
  {
    SimpleDateFormat fmt = new SimpleDateFormat();
    fmt.applyPattern("HH:mm");
    fmt.setTimeZone(timeZone);
    Date startHours = fmt.parse(timeString);
    Calendar cal = Calendar.getInstance();
    Date currDateDefault = cal.getTime();
    cal.setTimeZone(timeZone);
    Date currDateWAT = cal.getTime();
    cal.set(11, 0);
    cal.set(12, 0);
    cal.set(13, 0);
    cal.set(14, 0);
    long updatedTime = cal.getTimeInMillis() + startHours.getTime();
    cal.setTimeInMillis(updatedTime);
    startHours = cal.getTime();
    


    XLogger.getInstance().log(Level.FINE, "Send news as email service, start date: {0}, curr date: {1}, curr date WAT: {2}", getClass(), startHours, currDateDefault, currDateWAT);
    
    startAt(startHours.getTime(), 1L, TimeUnit.DAYS);
  }
  





  public void startAt(long startTime, long interval, TimeUnit timeUnit)
  {
    long initialDelay = getMillisUntil(startTime, interval, timeUnit);
    
    start(initialDelay, interval, timeUnit);
  }
  
  public void start(long initialDelay, long interval, TimeUnit timeUnit)
  {
    if (this.startAttempted) {
      throw new UnsupportedOperationException("Service: " + getClass() + " already started");
    }
    
    this.startAttempted = true;
    
    this.svc = Executors.newSingleThreadScheduledExecutor();
    
    E runnable = newTask();
    
    XLogger.getInstance().log(Level.INFO, "Scheduling {0} to run every {1} {2} starting on {3}", getClass(), runnable, Long.valueOf(interval), timeUnit, new Date(System.currentTimeMillis() + initialDelay));
    

    this.svc.scheduleWithFixedDelay(runnable, initialDelay, interval, timeUnit);
    
    Thread svcShutdownThread = new ServiceShutdownThread();
    svcShutdownThread.setDaemon(true);
    
    Runtime.getRuntime().addShutdownHook(svcShutdownThread);
  }
  
  private class ServiceShutdownThread extends Thread {
    private ServiceShutdownThread() {}
    
    @Override
    public void run() {
      try {
        XLogger.getInstance().log(Level.INFO, "Shutting down: {0}", getClass(), Service.this);
        Service.this.shutdownAndAwaitTermination(2L, TimeUnit.SECONDS);
      } catch (RuntimeException e) {
        XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
      }
    }
  }
  



  public boolean shutdownAndAwaitTermination(long timeout, TimeUnit unit)
  {
    if (this.svc != null) {
      boolean success = shutdownAndAwaitTermination(this.svc, timeout, unit, null);
      if (success) {
        this.svc = null;
      }
      return success;
    }
    return false;
  }
  













  private boolean shutdownAndAwaitTermination(ExecutorService pool, long timeout, TimeUnit unit, List<Runnable> addInterruptedHere)
  {
    pool.shutdown();
    
    if (timeout <= 0L) {
      timeout = 1L;
    }
    

    try
    {
      if (!pool.awaitTermination(timeout, unit))
      {
        List interrupted = pool.shutdownNow();
        
        if ((addInterruptedHere != null) && (interrupted != null) && (!interrupted.isEmpty()))
        {
          addInterruptedHere.addAll(interrupted);
        }
        

        if (!pool.awaitTermination(timeout, unit)) {
          return false;
        }
      }
    }
    catch (InterruptedException ie)
    {
      List interrupted = pool.shutdownNow();
      
      if ((addInterruptedHere != null) && (interrupted != null) && (!interrupted.isEmpty()))
      {
        addInterruptedHere.addAll(interrupted);
      }
      

      Thread.currentThread().interrupt();
    }
    
    return true;
  }
  
  private long getMillisUntil(long targetTime, long interval, TimeUnit timeUnit) {
    Calendar calendar = Calendar.getInstance();
    long currTime = calendar.getTimeInMillis();
    return currTime < targetTime ? targetTime - currTime : targetTime - currTime + timeUnit.toMillis(interval);
  }
  
  public boolean isStartAttempted() {
    return this.startAttempted;
  }
}
