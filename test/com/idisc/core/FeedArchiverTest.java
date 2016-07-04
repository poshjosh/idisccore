package com.idisc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class FeedArchiverTest extends IdiscTestBase {
    
    public FeedArchiverTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{ 
    }
    
    @BeforeClass
    public static void setUpClass() {}
    @AfterClass
    public static void tearDownClass() { }
    @Before
    public void setUp() { }
    @After
    public void tearDown() { }

    /**
     * Test of archiveFeeds_1 method, of class FeedArchiver.
     */
    @Test
    public void testArchiveFeeds() {
        System.out.println(this.getClass().getName()+"#archiveFeeds");
        long maxAge = 180;
        TimeUnit timeUnit = TimeUnit.DAYS;
        FeedArchiver instance = new FeedArchiver();
        int result = instance.archiveFeeds(maxAge, timeUnit, 100);
    }
}
