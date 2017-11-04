package com.idisc.core;

import com.bc.jpa.context.JpaContext;
import com.bc.util.XLogger;
import com.idisc.core.extraction.ExtractionContext;
import com.idisc.core.extraction.ExtractionFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;

public class FeedUpdateTask implements Runnable {

    private static final Logger logger = Logger.getLogger(FeedUpdateTask.class.getName());
  
    public FeedUpdateTask(){ }
  
    @Override
    public void run() {

        try{

            this.downloadFeeds();

//            this.sleep();

//            this.archiveFeeds();

        }catch(RuntimeException e) {

            this.handleRuntimeException(e);
        }
    }
  
    protected void handleRuntimeException(RuntimeException e) {
        logger.log(Level.WARNING, "Thread: "+Thread.currentThread().getName(), e);  
    }
  
    protected void sleep() {
        final long interval = this.getIntervalMillis();
        if(interval > 0) {
            Runtime.getRuntime().gc();
            this.sleep(interval);
        }        
    }
  
    private void sleep(long sleepTime) {
        if(sleepTime > 0) {
            final Thread curr = Thread.currentThread();
            try { 
                Thread.sleep(sleepTime); 
            } catch (InterruptedException e) { 
                XLogger.getInstance().log(Level.WARNING, "Interrupted, thread: " + curr.getName() + "#id: " + curr.getId(), getClass(), e);
                curr.interrupt();
            }
        }
    }
  
    public long getIntervalMillis() {
        return 0;
    }

    public boolean downloadFeeds(){
        try {
            
            final long tb4 = System.currentTimeMillis();
            final long mb4 = com.bc.util.Util.availableMemory();

            logger.fine("Downloading feeds");

            final int availableProcessors = Runtime.getRuntime().availableProcessors();
            
            final int maxConcurrent = 
                    (int)this.getLongProperty(ConfigNames.MAXCONCURRENT, availableProcessors);
            
            final int sitesPerBatch = 
                    (int)this.getLongProperty(ConfigNames.SITES_PER_BATCH, availableProcessors);

            final IdiscApp app = IdiscApp.getInstance();
            
            final ExtractionFactory extFactory = app.getExtractionFactory();
            
            final ExtractionContext webContext = extFactory.getExtractionContext("web");
            final List<String> webNames = new ArrayList(webContext.getNextNames(sitesPerBatch));
            Collections.sort(webNames, extFactory.getNamesComparator("web"));
            new SubmitTasks(
                    webNames, 
                    webContext.getTaskProvider(),
                    this.getLongProperty(ConfigNames.WEB_TIMEOUT_PER_TASK_SECONDS, 600),
                    TimeUnit.SECONDS,
                    maxConcurrent
            ).call();
            
            final ExtractionContext rssContext = extFactory.getExtractionContext("rss");
            final List<String> rssNames = new ArrayList(rssContext.getNextNames(sitesPerBatch));
            Collections.sort(rssNames, extFactory.getNamesComparator("rss"));
            new SubmitTasks(
                    rssNames, 
                    rssContext.getTaskProvider(),
                    this.getLongProperty(ConfigNames.RSS_TIMEOUT_PER_TASK_SECONDS, 300),
                    TimeUnit.SECONDS,
                    maxConcurrent
            ).call();

            logger.info(() -> "Done downloading feeds. Consumed time: " + 
                    TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis()-tb4)) + 
                    " seconds, memory: " + com.bc.util.Util.usedMemory(mb4));

            return true;

        }catch (Exception e) {

            logger.log(Level.WARNING, "Unexpected exception while downloading feeds", e);

            return false;
        }
    }
  
    public int archiveFeeds() {
        try {
            logger.fine("Archiving feeds");

            Configuration config = IdiscApp.getInstance().getConfiguration();

            long maxAge = config.getLong("maxFeedAgeDays");

            int batchSize = config.getInt("archiveBatchSize");

            final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();

            final int updateCount = new FeedArchiver(jpaContext).archiveFeedsBefore(maxAge, TimeUnit.DAYS, batchSize);

            logger.fine("Done archiving feeds");

            return updateCount;

        }catch (RuntimeException e) {

            logger.log(Level.WARNING, "Unexpected exception while archiving feeds", e);

            return 0;
        }
    }

    private long getLongProperty(String key, long defaultValue) {
        Configuration config = IdiscApp.getInstance().getConfiguration();
        final long value = config.getLong(key, defaultValue);
        logger.fine(() -> key + " = " + value);
        return value;
    }
}
