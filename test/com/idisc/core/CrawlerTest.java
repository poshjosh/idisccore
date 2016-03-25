package com.idisc.core;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import com.scrapper.Crawler;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Arrays;
import java.util.logging.Level;
import org.htmlparser.util.NodeList;
import org.junit.Test;


/**
 * @(#)TestLauncher.java   09-Jun-2015 19:02:24
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
public class CrawlerTest {
    
    @Test
    public void testCrawler() throws Exception {
        
        if(!IdiscApp.getInstance().isInitialized()) {
            IdiscApp.getInstance().init();
        }
        
        if(true) {
            this.testCrawler("tribune");
            return;
        }

        String [] sites = IdiscApp.getInstance().getCapturerApp().getSiteNames();

System.out.println(this.getClass().getName()+". Sites: "+(sites==null?null:Arrays.asList(sites)));

        for(String site:sites) {
        
            this.testCrawler(site);
        }
    }
    
    public void testCrawler(String site) throws Exception {
        
        CapturerApp app = IdiscApp.getInstance().getCapturerApp();
        
//        app.init(false);
        
        XLogger.getInstance().setLogLevel(Level.FINE);
        
        ScrapperConfigFactory factory = app.getConfigFactory();
        
        CapturerContext ctx = factory.getContext(site);
        
        JsonConfig config = ctx.getConfig();

        Crawler c = new Crawler(ctx){
            @Override
            public boolean isResume() {
                return false;
            }
        };

//        final TransverseFilterIx filter = new DefaultTransverseFilter(config, "targetNode0");
        
//        final TransverseFilterIx filter = new BaseTransverseFilter(this.get(config, "targetNode0"), "TestTransverseFilter");

//        final TransverseFilterIx filter = new TransverseFilterImpl(this.get(config, "targetNode0"), "TestTransverseFilter");
        
//        final DefaultVisitingFilterProcess nvFilter = new DefaultVisitingFilterProcess("TestNodeVisitor", null, null);
//        nvFilter.setTransverseEnabled(true);
//        nvFilter.setTransverseFilter(filter);

        String url = config.getString("url", "start");
        
        c.setStartUrl(url);
        
        while(c.hasNext()) {
            
            PageNodes pageNodes = c.next();
            
            if(pageNodes == null) {
                continue;
            }
            
            NodeList nodeList = pageNodes.getNodeList();

System.out.println("Found "+(nodeList==null?null:nodeList.size())+" nodes in url: "+pageNodes.getFormattedURL());            
            
        }
    }
}
