package com.idisc.core.metrics;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.idisc.core.IdiscApp;
import com.idisc.core.Setup;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * @author poshjosh
 */
public class HitcountMetrics {
    
    public static void main(String [] args) {
        
        try{
            
            Setup.setupApp();
            
            HitcountMetrics metrics = new HitcountMetrics();
            
            metrics.execute(1000, 100, 100, 200);
            
        }catch(Throwable t) {
            
            t.printStackTrace();
        }
        
    }
    
    public void execute(int max, int batch, int metricsSize, int maxContentLen) {
log(this.getClass().getName()+"#exectue(int, int, int, int)");        
        try{
            
            ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
            
            EntityController<Feed, Integer> ec = factory.getEntityController(Feed.class, Integer.class);
            
            int offset = 0;
            
            List<Feed> feeds = new ArrayList<>(max);
            
            do{
                
                List<Feed> found = ec.find(batch, offset);
                
                final int feed_count = this.sizeOf(found);
                
log("Batch size: %1d, offset: %2d, results: %3d", batch, offset, feed_count);                
                if(feed_count < 1) {
                    break;
                }
                
                feeds.addAll(found);
                
                offset += feed_count;
                
            }while(offset < max);
            
            Collections.sort(feeds, Collections.reverseOrder(new FeedHitcountComparator()));
            
            final int size = feeds.size() < metricsSize ? feeds.size() : metricsSize;
            for(int i=0; i<size; i++) {

                Feed feed = feeds.get(i);
print(i, feed, maxContentLen);                
            }
        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
    
    private class FeedHitcountComparator implements Comparator<Feed> {
        @Override
        public int compare(Feed f1, Feed f2) {
            int c1 = sizeOf(f1.getFeedhitList());
            int c2 = sizeOf(f2.getFeedhitList());
            return Integer.compare(c1, c2);
        }
    }
    
    private int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }
    
    private String truncate(String str, int maxLen, boolean ellipsize) {
        String output;
        if(str == null || str.isEmpty() || str.length() <= maxLen) {
            output = str;
        }else {
            final String prefix = ellipsize ? "..." : "";
            output = str.substring(0, maxLen-prefix.length()) + prefix;
        }
        return output;
    }
    
    private void print(int serial, Feed feed, int maxContentLen) {
log("Serial=%d, Feedid=%d, Site=%s, Hitcount=%d, Feeddate=%s, Categories=%s\nTitle=%s, Url=%s, ImageUrl=%s\n%s\n", 
        serial, feed.getFeedid(), feed.getSiteid().getSite(), this.sizeOf(feed.getFeedhitList()), 
        feed.getFeeddate(), feed.getCategories(), 
        feed.getTitle(), feed.getUrl(), feed.getImageurl(),
        truncate(feed.getContent(), maxContentLen, true));        
    }
    
    private void log(String format, Object... format_args) {
        log(String.format(format, format_args));
    }

    private void log(Object msg) {
        System.out.println(new Date()+". "+msg);
    }
}
