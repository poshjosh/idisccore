package com.idisc.core.web;

import com.idisc.core.FeedResultUpdater;
import com.idisc.core.IdiscTestBase;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
     * Test of doCall method, of class NewsCrawler.
     */
    @Test
    public void testRun() {
        
        System.out.println("run");
        String site = "lindaikeji.blogspot";
//        site = "dailytrust";
        site = "bellanaija";
        
        NewsCrawler instance = new TestNewsCrawler(site, true);
        
        instance.setCrawlLimit(200);
        instance.setParseLimit(60);
        
        final Collection<Feed> result = instance.call();
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Results: "+(result==null?null:result.size()));

        final int updated = new FeedResultUpdater().process(instance.getTaskName(), result);
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Updated: "+updated);
    }
}
