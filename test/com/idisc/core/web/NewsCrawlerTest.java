package com.idisc.core.web;

import com.idisc.core.FeedResultUpdater;
import com.idisc.core.IdiscTestBase;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
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
     * Test of call method, of class NewsCrawler.
     */
    @Test
    public void testCall() {
        
        System.out.println("call()");
        String site = "lindaikeji.blogspot";
//        site = "dailytrust";
        site = "bellanaija";
        
        NewsCrawler instance = new TestNewsCrawler(site, true, 2, TimeUnit.MINUTES, 20, true, false);
        
        instance.setCrawlLimit(200);
        instance.setParseLimit(60);
        
        final Collection<Feed> result = instance.call();
        
        final int resultSize = (result==null?null:result.size());
                
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Results: "+resultSize);

        final Collection<Feed> failedToCreate = new FeedResultUpdater().process(instance.getTaskName(), result);
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Updated: "+(resultSize-failedToCreate.size()));
    }
}
