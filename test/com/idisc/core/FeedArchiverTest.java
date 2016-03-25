package com.idisc.core;

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class FeedArchiverTest {
    
    public FeedArchiverTest() { }
    
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
