package com.idisc.core.web;

import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.Setup;
import com.scrapper.CapturerApp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * @author Josh
 */
public class WebFeedTaskTest {
    
    public WebFeedTaskTest() { }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        Setup.setupApp();
    }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    /**
     * Test of run method, of class WebFeedTask_old.
     */
    @org.junit.Test
    public void testRun() {
System.out.println("run");
        final WebFeedTask instance = new WebFeedTask(120, TimeUnit.SECONDS);
        instance.setMaxConcurrent(1);
        instance.run();
    }

    /**
     * Test of newTask method, of class WebFeedTask_old.
     */
//    @org.junit.Test
    public void testNewTask() {
System.out.println("newTask");

        int timeSeconds = 120;
        
        final WebFeedTask instance = new WebFeedTask(timeSeconds, TimeUnit.SECONDS);
        String sitename = this.getSitename();
        sitename = "ngrguardiannews";
        sitename = "bellanaija";
        
        final StoppableTask result = instance.createNewTask(sitename);

        String packageLoggerName = com.idisc.core.IdiscApp.class.getPackage().getName();
        XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
        XLogger.getInstance().setLogLevel(packageLoggerName, Level.FINE);

        final ScheduledExecutorService stopSvc = Executors.newSingleThreadScheduledExecutor();
        stopSvc.schedule(new Runnable(){
            @Override
            public void run() {
System.out.println("...................... Stopping");                
                result.stop();
                com.bc.util.Util.shutdownAndAwaitTermination(stopSvc, 500, TimeUnit.MILLISECONDS);
            }
        }, timeSeconds, TimeUnit.SECONDS);
        
        result.run();
    }

    private String getSitename() {
        
        String [] sitenames = CapturerApp.getInstance().getSiteNames();
        
        String sitename = null;
        for(String s:sitenames) {
            if("default".equals(s)) {
                continue;
            }
            sitename = s;
            break;
        }
System.out.println("Selected site: "+sitename);        
        return sitename;
    }
}
