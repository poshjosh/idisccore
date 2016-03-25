package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.process.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.idisc.core.AppProperties;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.FeedUpdateTask;
import com.idisc.core.IdiscApp;
import com.scrapper.CapturerApp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;

/**
 * @author Josh
 */
public class WebFeedTask extends ConcurrentTaskList {
    
    public WebFeedTask(long timeout, TimeUnit timeUnit) {  
        super(timeout, timeUnit);
    }
    
    @Override
    protected void beforeShutdown(){
        IdiscApp app = IdiscApp.getInstance();
        Configuration config = app.getConfiguration();
        final int maxFailsAllowed = config.getInt(AppProperties.MAX_FAILS_ALLOWED, 5); 
        final long timePerTask = this.getTimePerTask(config, this.getMaxConcurrent());
        final long timeoutMillis = this.getTimeoutMillis();
        if(timePerTask > 0 && timePerTask < timeoutMillis) {
            boolean scheduled = false;
            final ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
            try{
                TaskTerminator terminator = new TaskTerminator(timePerTask, maxFailsAllowed);
                long interval = timePerTask / this.getTaskNames().size();
                if(interval < 1000) {
                    interval = 1000; // Minimum value
                }
                ses.scheduleWithFixedDelay(terminator, interval, interval, TimeUnit.MILLISECONDS);
                scheduled = true;
            }finally{
                if(scheduled) {
                    Thread separateProcess = new Thread() {
                        @Override
                        public void run() {
                            Util.awaitTermination(ses, timeoutMillis, TimeUnit.MILLISECONDS);
                        }
                    };
                    separateProcess.start();
                }
            }
        }
    }
    
    private long getTimePerTask(Configuration config, int maxConcurrent) {
        long __timePerTaskSeconds = config.getLong(AppProperties.TIMEOUT_PER_SITE_SECONDS, this.computeDefaultTimePerTask(maxConcurrent));
        long timePerTask = TimeUnit.SECONDS.toMillis(__timePerTaskSeconds);
        return timePerTask;
    }
    
    private long computeDefaultTimePerTask(int maxConcurrent) {
        List<String> taskNames = this.getTaskNames();
        int taskCount = taskNames.size();
        long timePerTask;
        long timeoutMillis = this.getTimeoutMillis();
        if(taskCount > maxConcurrent) {
            long factor = taskCount / maxConcurrent;
            timePerTask = timeoutMillis / factor;
        }else{
            timePerTask = timeoutMillis;
        }
        return timePerTask;
    }

    @Override
    public NewsCrawler createNewTask(final String site) {

        JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(site);

        NewsCrawler crawler = new NewsCrawler(config, this.getResult()){
            @Override
            public String getTaskName() {
                return "Extract Web Feeds from "+this.getSitename();
            }
            @Override
            public boolean isResume() {
                return !WebFeedTask.this.isAcceptDuplicates();
            }
        };

        String url = config.getString("url", "start");

        crawler.setStartUrl(url);

        return crawler;
    }

    @Override
    public List<String> getTaskNames() {
        CapturerApp cap = IdiscApp.getInstance().getCapturerApp();
        List<String> _tn = new ArrayList<>(new HashSet(cap.getConfigFactory().getSitenames()));
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
            StoppableTask [] tasks = WebFeedTask.this.getTasks();
            Future [] futures = WebFeedTask.this.getFutures();
            for(int i=0; i<tasks.length; i++) {
                try{
                    NewsCrawler task = (NewsCrawler)tasks[i];
                    Future future = futures[i];
                    process(task, future);
                }catch(Exception e) {
                    XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                }
            }    
        }

        private void process(NewsCrawler task, Future future) {
            try{

                long timeSpent = System.currentTimeMillis() - task.getStartTime();

                Set<String> failed = task.getFailed();
                
                int failedCount = failed == null ? 0 : failed.size();
if(failedCount == 1) {
XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
"Task: {0}, has recorded first fail", this.getClass(), task.getTaskName());
}                
                
                if((task.isStarted() && !task.isCompleted() && timeSpent >= timePerTask) ||
                   (maxFailsAllowed > 0 && failedCount > maxFailsAllowed)) {
                    
                    if(!task.isStopInitiated() && !future.isCancelled()) {
XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
        "Stopping task: {0}, Time spent: {1}, fails: {2}", 
        this.getClass(), task.getTaskName(), timeSpent, failedCount);
                        try{
// This may not stop immediately, so we use Future.cancel(true)
                            task.stop(); 
                        }finally{
                            future.cancel(true);
                        }
                    }
                }
            }catch(Exception e) {
                XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
            }
        }
    }
}
