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


/**
 * @(#)Service.java   25-Feb-2015 23:43:39
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @param <E>
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class Service<E extends Runnable> {
    
    private boolean startAttempted;
    
    public abstract E newTask();
    
    /**
     * @param timeString Time string of format HH:mm
     * @param timeZone
     * @throws ParseException 
     */
    public void startAt(String timeString, TimeZone timeZone) throws ParseException {
        
        SimpleDateFormat fmt = new SimpleDateFormat();
        fmt.applyPattern("HH:mm");
        fmt.setTimeZone(timeZone);
        Date startHours = fmt.parse(timeString);
        Calendar cal = Calendar.getInstance();
        Date currDateDefault = cal.getTime();
        cal.setTimeZone(timeZone);
        Date currDateWAT = cal.getTime();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long updatedTime = cal.getTimeInMillis() + startHours.getTime();
        cal.setTimeInMillis(updatedTime);
        startHours = cal.getTime();
            
        // Run every day at the specified time
        //
XLogger.getInstance().log(Level.FINE, "Send news as email service, start date: {0}, curr date: {1}, curr date WAT: {2}", 
this.getClass(), startHours, currDateDefault, currDateWAT);
        this.startAt(startHours.getTime(), 1, TimeUnit.DAYS);
    }

    /**
     * @param startTime The time to start the service, (not delay but time e.g time of day based on TimeUnit.DAYS
     * @param interval The interval between invokations if the task returned by 
     * @param timeUnit 
     */
    public void startAt(long startTime, long interval, TimeUnit timeUnit) {
        
        long initialDelay = this.getMillisUntil(startTime, interval, timeUnit);
        
        this.start(initialDelay, interval, timeUnit);
    }
    
    public void start(long initialDelay, long interval, TimeUnit timeUnit) {
       
        if(startAttempted) {
            throw new UnsupportedOperationException("Service: "+this.getClass()+" already started");
        }
        
        startAttempted = true;
        
        final ScheduledExecutorService svc = 
                Executors.newSingleThreadScheduledExecutor();
    
        E runnable = this.newTask();
        
XLogger.getInstance().log(Level.INFO, "Scheduling {0} to run every {1} {2} starting on {3}", 
this.getClass(), runnable, interval, timeUnit, new Date(System.currentTimeMillis()+initialDelay));

        svc.scheduleWithFixedDelay(runnable, initialDelay, interval, timeUnit);
        
        Thread svcShutdownThread = new ServiceShutdownThread(svc);
        svcShutdownThread.setDaemon(true);
        
        Runtime.getRuntime().addShutdownHook(svcShutdownThread);
    }
    
    private class ServiceShutdownThread extends Thread{
        private final ExecutorService svc;
        private ServiceShutdownThread(ExecutorService svc) {
            this.svc = svc;
        }
        @Override
        public void run() {
            try{
XLogger.getInstance().log(Level.INFO, "Shutting down: {0}", this.getClass(), Service.this);
                shutdownAndAwaitTermination(svc, 1, TimeUnit.SECONDS);
            }catch(RuntimeException e) {
                XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
            }
        }
    }
    
    /**
     * @return true if the ExecutorService terminated correctly, false otherwise
     * @see com.bc.process.ProcessManager#shutdownAndAwaitTermination(java.util.concurrent.ExecutorService, long, java.util.concurrent.TimeUnit, java.util.List) 
     * @see java.util.concurrent.ExecutorService
     * @throws java.lang.InterruptedException
     */
    private boolean shutdownAndAwaitTermination(
            ExecutorService pool, long timeout, TimeUnit unit) {
        
        return shutdownAndAwaitTermination(pool, timeout, unit, null);
    }
    
    /**
     * Shuts down an ExecutorService in two phases, first by calling 
     * shutdown to reject incoming tasks, and then calling shutdownNow,
     * if necessary, to cancel any lingering tasks.
     * <br/><br/>
     * <b>Note:</b> This method was culled from the ExecutorService documentation
     * @return true if the ExecutorService terminated correctly, false otherwise
     * @see java.util.concurrent.ExecutorService
     * @throws java.lang.InterruptedException
     */
    private boolean shutdownAndAwaitTermination(
            ExecutorService pool, long timeout, 
            TimeUnit unit, List<Runnable> addInterruptedHere) {
        
        pool.shutdown(); // Disable new tasks from being submitted

        if(timeout <= 0) {
            timeout = 1;
        }

        try {
             
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(timeout, unit)) {
                 
                List interrupted = pool.shutdownNow(); // Cancel currently executing tasks
                
                if(addInterruptedHere != null && 
                        (interrupted != null && !interrupted.isEmpty())) {
                    addInterruptedHere.addAll(interrupted);
                }
               
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(timeout, unit)) { 
                    return false;
                }    
             }
        } catch (InterruptedException ie) {
            
            // (Re-)Cancel if current thread also interrupted
            List interrupted = pool.shutdownNow();
            
            if(addInterruptedHere != null && 
                    (interrupted != null && !interrupted.isEmpty())) {
                addInterruptedHere.addAll(interrupted);
            }
            
            // Preserve interrupt status
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
        return startAttempted;
    }
}
