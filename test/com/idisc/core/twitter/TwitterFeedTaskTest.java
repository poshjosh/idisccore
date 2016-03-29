package com.idisc.core.twitter;

import com.idisc.core.Setup;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;


/**
 * @(#)TwitterFeedTaskTest.java   16-Jun-2015 16:06:42
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
public class TwitterFeedTaskTest {
    
    public TwitterFeedTaskTest() { }

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
     * Test of run method, of class WebFeedTask.
     */
    @org.junit.Test
    public void testRun() {
System.out.println("run");
        final TwitterFeedTask instance = new TwitterFeedTask();
        
        instance.run();
    }
}
