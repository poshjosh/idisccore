package com.idisc.core.web;

import com.bc.process.ProcessManager;
import com.bc.process.StoppableTask;
import com.idisc.core.Setup;
import com.scrapper.CapturerApp;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
//    @org.junit.Test
    public void testRun() {
System.out.println("run");
        final WebFeedTask instance = new WebFeedTask(60, TimeUnit.SECONDS);
        instance.run();
    }

    /**
     * Test of newTask method, of class WebFeedTask_old.
     */
    @org.junit.Test
    public void testNewTask() {
System.out.println("newTask");
        
        final WebFeedTask instance = new WebFeedTask(60, TimeUnit.SECONDS);
        String sitename = this.getSitename();
        sitename = "ngrguardiannews";
        
        final StoppableTask result = instance.createNewTask(sitename);
        
// Doesn't work        
//XLogger.getInstance().setRootOnly(true);
//XLogger.getInstance().setRootLoggerName("com.bc.ROOT");
//XLogger.setLogLevel("com.bc.ROOT", Level.FINE);

        final ScheduledExecutorService stopSvc = Executors.newSingleThreadScheduledExecutor();
        stopSvc.schedule(new Runnable(){
            @Override
            public void run() {
System.out.println("...................... Stopping");                
                result.stop();
                ProcessManager.shutdownAndAwaitTermination(stopSvc, 500, TimeUnit.MILLISECONDS);
            }
        }, 60, TimeUnit.SECONDS);
        
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
