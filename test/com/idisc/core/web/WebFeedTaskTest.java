package com.idisc.core.web;

import com.bc.task.StoppableTask;
import com.bc.util.Util;
import com.bc.util.XLogger;
import com.bc.util.concurrent.NamedThreadFactory;
import com.idisc.core.IdiscTestBase;
import com.scrapper.CapturerApp;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author Josh
 */
public class WebFeedTaskTest extends IdiscTestBase {
    
    public WebFeedTaskTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
    }

    /**
     * Test of run method, of class WebFeedTask_old.
     */
    @org.junit.Test
    public void testRun() {
System.out.println("run");
        final WebFeedTask instance = new WebFeedTask(5, TimeUnit.MINUTES, 2, TimeUnit.MINUTES, 3, 20, false);
        instance.run();
    }

    /**
     * Test of newTask method, of class WebFeedTask_old.
     */
//    @org.junit.Test
    public void testNewTask() {
System.out.println("newTask");

        int timeSeconds = 120;
        
        final WebFeedTask instance = new WebFeedTask(5, TimeUnit.MINUTES, 2, TimeUnit.MINUTES, 1, 20, false);
        String sitename = this.getRandomSitename();
        sitename = "ngrguardiannews";
        sitename = "bellanaija";
        
        final StoppableTask result = instance.createNewTask(sitename);

        String packageLoggerName = com.idisc.core.IdiscApp.class.getPackage().getName();
        XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
        XLogger.getInstance().setLogLevel(packageLoggerName, Level.FINE);

        final ScheduledExecutorService stopSvc = Executors.newSingleThreadScheduledExecutor(
                new NamedThreadFactory(this.getClass().getName()+"_ThreadPool"));
        stopSvc.schedule(new Runnable(){
            @Override
            public void run() {
                try{
System.out.println("...................... Stopping");                
                    result.stop();
                    com.bc.util.Util.shutdownAndAwaitTermination(stopSvc, 500, TimeUnit.MILLISECONDS);
                }catch(RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }, timeSeconds, TimeUnit.SECONDS);
        
        result.run();
    }

    private String getRandomSitename() {
        
        String [] sitenames = this.getSitenames();
        
        final int n = Util.randomInt(sitenames.length);
        
        return sitenames[n];
    }
}
