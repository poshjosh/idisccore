package com.idisc.core;

import com.bc.process.StoppableTask;
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

/**
 * @author Josh
 */
public abstract class ConcurrentTaskList implements Serializable, TaskHasResult<Collection<Feed>> {
    
    private boolean acceptDuplicates;
    
    private final long timeoutMillis;
    
    private final int maxConcurrent;
    
    private StoppableTask [] tasks;
    
    private Future [] futures;
    
    private final Collection<Feed> result;
    
    public ConcurrentTaskList(long timeout, TimeUnit timeUnit) {  
        this.timeoutMillis = timeUnit.toMillis(timeout);
        IdiscApp app = IdiscApp.getInstance();
        Configuration config = app.getConfiguration();
        this.maxConcurrent = config.getInt(AppProperties.MAXCONCURRENT, 3); 
        this.result = Collections.synchronizedCollection(new ArrayList<Feed>());
    }
    
    public abstract StoppableTask createNewTask(final String site);

    public abstract List<String> getTaskNames();
    
    @Override
    public final void run() {
        try{
            this.doRun();
        }catch(Exception e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }
    }
    
    protected void doRun() {
        
        ExecutorService es = Executors.newFixedThreadPool(maxConcurrent);
        
        List<String> siteNames = this.getTaskNames();
        
        //Shuffle this so the sites are equally distributed
        Collections.shuffle(siteNames);
        
XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
"Timeout: {0} minutes, Task count: {1}, max concurrent tasks: {2}",
this.getClass(), timeoutMillis<1000?0:TimeUnit.MILLISECONDS.toMinutes(timeoutMillis), siteNames.size(), maxConcurrent);
        
        this.tasks = new StoppableTask[siteNames.size()];

        this.futures = new Future[siteNames.size()];

        try{
            
            for(int i=0; i<siteNames.size(); i++) {

                StoppableTask task = this.createNewTask(siteNames.get(i));

                Future future = es.submit(task); 

                tasks[i] = task;
                
                futures[i] = future;
            }
        }finally{
            try{
                this.beforeShutdown();
            }finally{
                com.bc.util.Util.shutdownAndAwaitTermination(es, timeoutMillis, TimeUnit.MILLISECONDS);
            }
        }
    }
    
    protected void beforeShutdown() {}
    
    @Override
    public Collection<Feed> getResult() {
        return result;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public int getMaxConcurrent() {
        return maxConcurrent;
    }

    public StoppableTask[] getTasks() {
        return tasks;
    }

    public Future[] getFutures() {
        return futures;
    }

    public boolean isAcceptDuplicates() {
        return acceptDuplicates;
    }

    public void setAcceptDuplicates(boolean acceptDuplicates) {
        this.acceptDuplicates = acceptDuplicates;
    }
}

