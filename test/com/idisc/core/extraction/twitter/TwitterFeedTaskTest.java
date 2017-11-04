package com.idisc.core.extraction.twitter;

import com.idisc.core.extraction.twitter.TwitterFeedTask;
import com.idisc.core.IdiscTestBase;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;


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
public class TwitterFeedTaskTest extends IdiscTestBase {
    
    public TwitterFeedTaskTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
    }

    /**
     * Test of run method, of class TwitterFeedTask.
     */
    @Test
    public void testRun() {
System.out.println("run");
        final TwitterFeedTask instance = new TwitterFeedTask();
        
        instance.run();
    }
}
