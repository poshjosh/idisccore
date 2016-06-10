package com.idisc.core.metrics;

import com.idisc.core.FeedHitcountComparator;
import com.idisc.core.Setup;
import com.idisc.core.FeedSelector;
import com.idisc.pu.entities.Feed;
import java.util.Date;
import java.util.List;

/**
 * @author poshjosh
 */
public class HitcountMetrics {
    
    public static void main(String [] args) {
        
        try{
            
            Setup.setupApp();
            
            FeedSelector feedSelector = new FeedSelector();
            
            final int maxAgeDays = 7;

            List<Feed> selected = feedSelector.getList(maxAgeDays, -1, 1000);
log("Selected %s feeds", selected==null?null:selected.size());            
            
            List<Feed> outputList = feedSelector.sort(selected, new FeedHitcountComparator(true), 10); 

for(Feed feed:outputList) {
    System.out.print(sizeOf(feed.getFeedhitList())+", ");
}            
System.out.println();
            
int i = 0;            
for(Feed feed:outputList) {
    print(i++, feed, 150);
}            
            
//            ToHtml<List<Feed>> listHtml = new FeedListTableHtml("http://www.looseboxes.com", "/idisc", "/images/appicon.png");
            
//            String outputStr = listHtml.toHtml(outputList);
            
//System.out.println(outputStr);

        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
    
    private static void print(int serial, Feed feed, int maxContentLen) {
log("Serial=%d, Feedid=%d, Site=%s, Hitcount=%d, Feeddate=%s, Categories=%s\nTitle=%s, Url=%s, ImageUrl=%s\n%s\n", 
        serial, feed.getFeedid(), feed.getSiteid().getSite(), sizeOf(feed.getFeedhitList()), 
        feed.getFeeddate(), feed.getCategories(), 
        feed.getTitle(), feed.getUrl(), feed.getImageurl(),
        truncate(feed.getContent(), maxContentLen, true));        
    }
    
    private static int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }
    
    private static String truncate(String str, int maxLen, boolean ellipsize) {
        String output;
        if(str == null || str.isEmpty() || str.length() <= maxLen) {
            output = str;
        }else {
            final String prefix = ellipsize ? "..." : "";
            output = str.substring(0, maxLen-prefix.length()) + prefix;
        }
        return output;
    }
    
    private static void log(String format, Object... format_args) {
        log(String.format(format, format_args));
    }

    private static void log(Object msg) {
        System.out.println(new Date()+". "+msg);
    }
}
