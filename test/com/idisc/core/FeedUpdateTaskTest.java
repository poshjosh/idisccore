package com.idisc.core;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class FeedUpdateTaskTest {
    
    public FeedUpdateTaskTest() { }
    
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
     * Test of downloadFeeds method, of class FeedUpdateTask.
     */
    @Test
    public void testDownloadFeeds() {
        FeedUpdateTask instance = new FeedUpdateTask();
        instance.downloadFeeds();
    }
}
