package com.idisc.core.rss;

import com.bc.util.XLogger;
import com.idisc.core.IdiscTestBase;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
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
public class RSSFeedTaskTest extends IdiscTestBase {
    
    public RSSFeedTaskTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
    }


    /**
     * Test of run method, of class WebFeedTask.
     */
    @Test
    public void testRun() {
log("run");
    
        String packageLoggerName = com.idisc.core.IdiscApp.class.getPackage().getName();
        XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
        XLogger.getInstance().setLogLevel(packageLoggerName, Level.FINER);

        final RSSFeedTask instance = new RSSFeedTask(4, TimeUnit.MINUTES, 2, TimeUnit.MINUTES, 5, false);
        
//        instance.setFeedProperties(this.getLocalFeedProperties());

        Properties props = instance.getFeedProperties();
        String name;
        name = "Aljazeera"; 
        name = "Premier League@talksport.com"; 
//        props.clear(); // Un comment this out to test all feeds
        props.setProperty(name, props.getProperty(name));
log("Properties: "+props);

        instance.run();
    }
    
    private Properties getLocalFeedProperties() {
        Properties props = new Properties();
        String dir = "file://"+System.getProperty("user.home")+"/Documents/NetBeansProjects/idisccore/test/com/idisc/core";
        props.setProperty("Vanguard Nigeria", dir + "/vanguardngr.xht");
        props.setProperty("Aljazeera", dir + "/aljazeera.xht");
        return props;
    }
}
