package com.idisc.core.extraction.web;

import com.bc.json.config.JsonConfig;
import com.idisc.core.FeedHandler;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.InsertFeedToDatabase;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class NewsCrawlerTest extends IdiscTestBase {
    
    public NewsCrawlerTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
    }

    /**
     * Test of call method, of class WebFeedCrawler.
     */
    @Test
    public void testCall() {
        
        System.out.println("call()");
        String site = "lindaikeji.blogspot";
//        site = "dailytrust";
//        site = "bellanaija";
        
        final JsonConfig config = this.getCapturerApp().getConfigFactory().getConfig(site);
        
        final FeedHandler feedHandler =  new InsertFeedToDatabase(this.getIdiscApp().getJpaContext());
        
        WebFeedCrawler instance = new TestNewsCrawler(true, config, 2, TimeUnit.MINUTES, 20, feedHandler, true, false);
        
        instance.setCrawlLimit(200);
        instance.setParseLimit(60);
        
        final Integer updateCount = instance.call();
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Update count: "+updateCount);
    }
}
