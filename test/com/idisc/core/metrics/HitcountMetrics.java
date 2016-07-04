package com.idisc.core.metrics;

import com.idisc.core.comparator.BaseFeedComparator;
import com.idisc.core.FeedSelector;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.html.FeedListTableHtml;
import com.idisc.core.html.ToHtml;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author poshjosh
 */
public class HitcountMetrics extends IdiscTestBase {

    public HitcountMetrics() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException {
    }
    
    public static void main(String [] args) {
        try{
            new HitcountMetrics().run();
        }catch(ConfigurationException | IOException | IllegalAccessException | 
                InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
    
    public void run() {
        
        try{
            
            FeedSelector feedSelector = new FeedSelector();
            
            final int maxAgeDays = 7;

            List<Feed> selected = feedSelector.getList(maxAgeDays, -1, 1000);
log("Selected %s feeds", selected==null?null:selected.size());            
            
            List<Feed> outputList = feedSelector.sort(selected, new BaseFeedComparator(true), 10); 

for(Feed feed:outputList) {
    System.out.print(sizeOf(feed.getFeedhitList())+", ");
}            
System.out.println();
            
int i = 0;            
for(Feed feed:outputList) {
    print(i++, feed, 150);
}            
            
System.out.println("======================== PRINTING HTML =========================");
            ToHtml<List<Feed>> listHtml = new FeedListTableHtml("http://www.looseboxes.com", "/idisc", "/images/appicon.png");
            
            String outputStr = listHtml.toHtml(outputList);
            
System.out.println(outputStr);

        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
    
    private void print(int serial, Feed feed, int maxContentLen) {
log("Serial=%d, Feedid=%d, Site=%s, Hitcount=%d, Feeddate=%s, Categories=%s\nTitle=%s, Url=%s, ImageUrl=%s\n%s\n", 
        serial, feed.getFeedid(), feed.getSiteid().getSite(), sizeOf(feed.getFeedhitList()), 
        feed.getFeeddate(), feed.getCategories(), 
        feed.getTitle(), feed.getUrl(), feed.getImageurl(),
        truncate(feed.getContent(), maxContentLen, true));        
    }
    
}
