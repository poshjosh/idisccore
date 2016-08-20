package com.idisc.core.metrics;

import com.idisc.core.IdiscApp;
import com.idisc.core.comparator.BaseFeedComparator;
import com.idisc.pu.SelectByDate;
import com.idisc.core.IdiscTestBase;
import com.idisc.html.FeedCellHtml;
import com.idisc.html.FeedListHtml;
import com.idisc.html.FeedListTableHtml;
import com.idisc.html.ToHtml;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import com.bc.jpa.dao.BuilderForSelect;

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
            
            SelectByDate<Feed, Integer> feedSelector = new SelectByDate(
                    IdiscApp.getInstance().getJpaContext(), Feed.class, Integer.class);
            
            final int maxAgeDays = 7;

            List<Feed> selected = feedSelector.getResultList(Feed_.feeddate.getName(), BuilderForSelect.GT, maxAgeDays, TimeUnit.DAYS, -1, 1000);
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

            final String baseUrl = "http://www.looseboxes.com";
            final String context = "/idisc";
            ToHtml<Feed> listItemHtml = new FeedCellHtml(baseUrl, context){
                @Override
                protected void doAppendHtml(Feed feed, StringBuilder appendTo) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }
            };
            
            //Use list-style-type:none to remove bullets
            //@Microsoft IE9 and below demands specifying list-style-type to none in each <li> also
            final ToHtml<List<Feed>> feedListHtml = new FeedListHtml("list-style-type:none; margin:0; padding:0; font-size:1.5em; background:#eeeeee", 
                    "list-style-type:none; margin:0.5em; padding:0.5em; background:#ffffff",
                    listItemHtml
            );            
        
            final ToHtml<List<Feed>> feedTableHtml = new FeedListTableHtml(
                    baseUrl, context, "/images/appicon.png", 150,
                    64, 64, "font-size:1.5em; background:#eeeeee", 
                    "vertical-align:top; margin:0.5em; padding:0.5em; background:#ffffff", 
                    null, null
            );
            
            String outputStr = feedTableHtml.toHtml(outputList);
            
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
