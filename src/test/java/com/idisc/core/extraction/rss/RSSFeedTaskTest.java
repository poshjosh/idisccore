package com.idisc.core.extraction.rss;

import com.bc.jpa.context.JpaContext;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.SubmitTasks;
import com.idisc.core.extraction.ExtractionContext;
import com.idisc.core.extraction.web.ExtractionContextForWebPages;
import com.idisc.core.functions.GetSubList;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import com.bc.json.config.JsonConfigService;

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
    
        final JpaContext jpaContext = this.getIdiscApp().getJpaContext();
        
        final JsonConfigService configService = this.getContextFactory().getConfigService();
        
        final List<String> sitenames = new ArrayList(configService.getConfigNamesLessDefaultConfig());
        
        Collections.sort(sitenames, this.getIdiscApp().getExtractionFactory().getNamesComparator("web"));
        
        final Properties props = new RssMgr().getFeedNamesProperties();
        String name;
        name = "Aljazeera"; 
        name = "Premier League@talksport.com"; 
//        props.clear(); // Un comment this out to test all feeds
        props.setProperty(name, props.getProperty(name));
        
        final ExtractionContext webContext = new ExtractionContextForWebPages(
                sitenames, 
                new GetSubList(),
                new RssFeedTaskProvider(jpaContext, 
                        props, 120, TimeUnit.SECONDS, false
                )
        );

        new SubmitTasks(
                webContext.getNextNames(5), 
                webContext.getTaskProvider(),
                240,
                TimeUnit.SECONDS,
                Runtime.getRuntime().availableProcessors()
        ).call();
    }
    
    private Properties getLocalFeedProperties() {
        Properties props = new Properties();
        String dir = "file://"+System.getProperty("user.home")+"/Documents/NetBeansProjects/idisccore/src/test/java/com/idisc/core";
        props.setProperty("Vanguard Nigeria", dir + "/vanguardngr.xht");
        props.setProperty("Aljazeera", dir + "/aljazeera.xht");
        return props;
    }
}
