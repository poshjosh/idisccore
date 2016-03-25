package com.idisc.core;

import com.bc.process.ConcurrentProgressList;
import com.bc.process.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.scrapper.util.SortByFrequency;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;


/**
 * @(#)ConcurrentTaskList.java   03-Mar-2015 17:13:18
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class ConcurrentTaskList_old 
        extends ConcurrentProgressList
        implements TaskHasResult<Collection<Feed>> {
    
    private long timeout;
    
    private final List<StoppableTask> tasks;
    
    private final SortByFrequency sortByFrequency;
    
    private final Collection<Feed> result;
    
    public ConcurrentTaskList_old() {  

        sortByFrequency = new SortByFrequency();
        
        result = Collections.synchronizedCollection(new ArrayList<Feed>());
        
        IdiscApp app = IdiscApp.getInstance();
        
        Configuration config = app.getConfiguration();
        
        int maxConcurrent = config.getInt(AppProperties.MAXCONCURRENT);        
        this.setMaxConcurrentProcesses(maxConcurrent);
        
        final String category = app.getControllerFactory().getMetaData().getTableName(Feed.class);
        
        final List<String> taskNames = ConcurrentTaskList_old.this.getTaskNames();
        
        sortByFrequency.setCategory(category);
        
        Collections.sort(taskNames, sortByFrequency);
        
        

        // If we don't do this, some sites may never get airtime
        //
        // Factor: 0.2f
        //  Input: [a, b, c, d, e, f, g, h, i, j]
        // Output: [a, b, i, j, c, d, e, f, g, h]
        //
        sortByFrequency.rearrange(taskNames, 0.2f);
        
        this.tasks = new ArrayList<StoppableTask>(taskNames.size());
XLogger.getInstance().log(Level.FINE, "Sites: {0}", this.getClass(), taskNames);

        for(String sitename:taskNames) {
XLogger.getInstance().log(Level.FINER, "Creating extractor task for: {0}", this.getClass(), sitename);
            StoppableTask task = ConcurrentTaskList_old.this.newTask(result, sitename);
            this.tasks.add(task);
        }
    }
    
    public abstract List<String> getTaskNames();
    
    protected abstract StoppableTask newTask(Collection<Feed> resultBuffer, String name);

    @Override
    public void run() {
        
        try{
            
            super.run();
            
            if(timeout > 0) {
                this.waitForTasks(timeout, TimeUnit.MILLISECONDS, true);
            }
            
        }catch(InterruptedException e) {    
            XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
        }catch(RuntimeException e){
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception: {0}", this.getClass(), e.toString());
        }

XLogger.getInstance().log(Level.FINE, "Web feed count: {0}", this.getClass(), result==null?null:result.size());            
XLogger.getInstance().log(Level.FINER, "Web feed: {0}", this.getClass(), result);            
    }
    
    @Override
    protected List<StoppableTask> getList() {
        return tasks;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getName();
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    
    @Override
    public Collection<Feed> getResult() {
        return result;
    }
}
