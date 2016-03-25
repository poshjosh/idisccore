package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.core.rss.RSSFeedTask;
import com.idisc.core.web.WebFeedTask;
import com.idisc.pu.entities.Feed;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;


/**
 * @(#)FeedUpdateTask.java   22-Apr-2015 21:59:26
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
public class FeedUpdateTask implements Runnable{
    
    public static Level LOG_LEVEL = Level.FINER;
    
    private long _flt;
    public long getFeedLoadTimeoutSeconds() {
        if(_flt < 1) {
            Configuration config = IdiscApp.getInstance().getConfiguration();
            _flt = config.getLong(AppProperties.TIMEOUT_PER_TASK_SECONDS, 300);
XLogger.getInstance().log(Level.FINE, "Feed load timeout: {0} seconds", this.getClass(), _flt);
        }
        return _flt;
    }

    @Override
    public void run() {
            
        this.downloadFeeds();

        this.archiveFeeds();
    }
    
    public boolean downloadFeeds() {
        
//long tb4 = System.currentTimeMillis();
//long mb4 = Runtime.getRuntime().freeMemory();
        
        boolean success = this.downloadFeeds_1();
        
//System.out.println(this.getClass().getName()+"#downloadFeeds(). Consumed memory:"+
//        (mb4-Runtime.getRuntime().freeMemory())+", time: "+(System.currentTimeMillis()-tb4));
        return success;
    }
    
    private boolean downloadFeeds_1() {
//@todo REMOVED TwitterFeedTask due to the error below
//Jan 17, 2016 8:03:35 PM com.idisc.core.twitter.TwitterFeedTask 
//WARNING: 401:Authentication credentials (https://dev.twitter.com/pages/auth) were missing or incorrect. Ensure that you have set valid consumer key/secret, access token/secret, and the system clock is in sync.
//message - Could not authenticate you.
//code - 32

//Relevant discussions can be found on the Internet at:
//	http://www.google.co.jp/search?q=e5488403 or
//	http://www.google.co.jp/search?q=0ab23671
//TwitterException{exceptionCode=[e5488403-0ab23671], statusCode=401, message=Could not authenticate you., code=32, retryAfter=-1, rateLimitStatus=null, version=3.0.5}
        
        try{

            final long feedLoadTimeoutSeconds = this.getFeedLoadTimeoutSeconds();

            Map<String, TaskHasResult<Collection<Feed>>> tasks = new HashMap<>(3, 1.0f);

            tasks.put("Web Feeds", new WebFeedTask(feedLoadTimeoutSeconds, TimeUnit.SECONDS));
            tasks.put("RSS Feeds", new RSSFeedTask(feedLoadTimeoutSeconds, TimeUnit.SECONDS));
//            tasks.put("Twitter Feeds", new TwitterFeedTask());

            ExecutorService es = Executors.newFixedThreadPool(tasks.size());

            try{
                for(TaskHasResult task:tasks.values()) {
                    es.submit(task);
                }
            }finally{

                com.bc.util.Util.shutdownAndAwaitTermination(es, feedLoadTimeoutSeconds, TimeUnit.SECONDS);
            }
            
            FeedResultUpdater updater = new FeedResultUpdater();
            
            for(String name:tasks.keySet()) {
                
                TaskHasResult<Collection<Feed>> task = tasks.get(name);
                
                updater.process(name, task.getResult());
            }
            
            return true;
            
        }catch(Exception e) {
            
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
            
            return false;
        }
    }
    
    public boolean archiveFeeds() {
        
        try{
            
            Configuration config = IdiscApp.getInstance().getConfiguration();
            
            long maxAge = config.getLong(AppProperties.MAX_FEED_AGE);

            int batchSize = config.getInt(AppProperties.ARCHIVE_BATCH_SIZE);

            new FeedArchiver().archiveFeeds(maxAge, TimeUnit.MINUTES, batchSize);

XLogger.getInstance().log(Level.FINER, "Done archiving feeds.", this.getClass());
            
            return true;
            
        }catch(RuntimeException e) {
            
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
            
            return false;
        }
    }
}
/**
 * 
    private boolean downloadFeeds_old() {
        
        try{
        
            final long feedLoadTimeout = this.getFeedLoadTimeoutSeconds();

            FeedLoader feedLoader = new FeedLoader(feedLoadTimeout, TimeUnit.SECONDS);

            feedLoader.run();

            feedLoader.waitForFutures(true);

            Map<MultiFeedTask.TaskName, Collection<Feed>> task = feedLoader.getResult();

feedLoader.print(task, Level.FINE);

            Set<TaskName> names = task.keySet();
            
            FeedResultUpdater updater = new FeedResultUpdater();
            
            for(TaskName name:names) {
                
                updater.process(name.toString(), task.get(name));
            }
            
            return true;
            
        }catch(RuntimeException e) {
            
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
            
            return false;
        }
    }
    
 * 
 */