package com.idisc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class FeedUpdateTaskTest extends IdiscTestBase {
    
    public FeedUpdateTaskTest()             
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{ 
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception { }
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
