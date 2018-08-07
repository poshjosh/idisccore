package com.idisc.core.extraction.rss;

import com.bc.jpa.context.JpaContext;
import com.idisc.core.IdiscTestBase;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.bc.webdatex.context.ExtractionContextFactory;
import com.idisc.core.IdiscApp;
import com.idisc.core.SubmitTasks;
import com.idisc.core.extraction.ScrapContextImpl;
import com.idisc.core.extraction.ScrapContext;
import com.idisc.core.extraction.scrapconfig.ScrapConfig;
import com.idisc.core.extraction.scrapconfig.ScrapConfigBean;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactory;
import java.net.MalformedURLException;
import java.util.Set;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;

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
    
    public RSSFeedTaskTest() {
    }


    /**
     * Test of run method, of class WebFeedTask.
     */
    @Test
    public void testRun() {
log("run");
    
        final IdiscApp app = this.getIdiscApp();
        
        final JpaContext jpaContext = app.getJpaContext();
        
        final ExtractionContextFactory contextFactory = app.getExtractionContextFactory();
        
        final ScrapConfigFactory scf = app.getScrapConfigFactory();
        
        final ScrapConfigBean scrapConfig = new ScrapConfigBean(scf.get(ScrapConfig.TYPE_RSS));
        scrapConfig.setTimeout(120);
        scrapConfig.setTimeUnit(TimeUnit.SECONDS);
        scrapConfig.setMaxConcurrentUnits(Runtime.getRuntime().availableProcessors());
        
        final Configuration config;
        try{
            config = this.loadConfiguration();
        }catch(MalformedURLException | ConfigurationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        final Properties rssProps = new RssPropertiesProvider().apply(config);
//        String name;
//        name = "Aljazeera"; 
//        name = "Premier League@talksport.com"; 
//        final String value = props.getProperty(name);
//        rssProps.clear(); 
//        rssProps.setProperty(name, value);

        final Set<String> names = rssProps.stringPropertyNames();
        
        System.out.println("All names: " + names);
        
        final ScrapContext scrapRssContext = new ScrapContextImpl(
                names, 
                jpaContext,
                scrapConfig,
                new RssFeedTaskProvider(jpaContext, contextFactory, scf, rssProps)
        );

        try{
            
            new SubmitTasks(scrapRssContext).call();
            
        }catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private Properties getLocalFeedProperties() {
        Properties props = new Properties();
        String dir = "file://"+System.getProperty("user.home")+"/Documents/NetBeansProjects/idisccore/src/test/java/com/idisc/core";
        props.setProperty("Vanguard Nigeria", dir + "/vanguardngr.xht");
        props.setProperty("Aljazeera", dir + "/aljazeera.xht");
        return props;
    }
}
