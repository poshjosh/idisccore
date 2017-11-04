package com.idisc.core;

import com.bc.jpa.context.JpaContext;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 * @author Josh
 */
public class FeedArchiverTest extends IdiscTestBase {
    
    public FeedArchiverTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{ 
    }
    
    /**
     * Test of archiveFeeds_1 method, of class FeedArchiver.
     */
    @Test
    public void testArchiveFeeds() {
        
        System.out.println(this.getClass().getName()+"#archiveFeeds");
        
        final JpaContext jpaContext = this.getIdiscApp().getJpaContext();
        
        final int limit = 100;
        
        List<Feed> feeds = jpaContext.getDaoForSelect(Feed.class)
                .ascOrder(Feed_.feeddate.getName())
                .getResultsAndClose(0, limit);
        
        for(Feed feed : feeds) {
//System.out.println();            
//System.out.println("ID: "+feed.getFeedid()+", title: "+feed.getTitle());        
//System.out.println("Feeddate: "+feed.getFeeddate()+", datecreated: "+feed.getDatecreated()+", ");
        }
        
        Feed feed = feeds.get(feeds.size() - 1); 
        
        FeedArchiver instance = new FeedArchiver(this.getIdiscApp().getJpaContext());
        
long mb4 = com.bc.util.Util.availableMemory();
long tb4 = System.currentTimeMillis();

        final int archivedCount = instance.archiveFeedsBefore(feed.getFeeddate(), limit);
     
System.out.println("Archived count: "+archivedCount+", consumed. time: "+(System.currentTimeMillis()-tb4)+", memory: "+com.bc.util.Util.usedMemory(mb4));        
    }
}
