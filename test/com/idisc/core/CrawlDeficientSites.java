package com.idisc.core;

import com.bc.jpa.context.JpaContext;
import com.idisc.core.extraction.web.WebFeedCrawler;
import com.idisc.core.extraction.web.TestNewsCrawler;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import com.bc.json.config.JsonConfig;
import com.idisc.pu.FeedService;

/**
 * @author Josh
 */
public class CrawlDeficientSites extends IdiscTestBase {
    
    private final boolean debug = false;
    private final boolean resumable = false;
    private final boolean resume = false;
    private final int maxAgeHours = 4;
    private final int parseLimit = maxAgeHours * 10;
    private final int crawlLimit = parseLimit * 3;
    private final int max = parseLimit / 2;
    
    private final int timeout = 4;
    private final TimeUnit timeoutUnit = TimeUnit.MINUTES;
    private final int maxFailsAllowed = 20;
    
    private final Date maxAge;
    
    public CrawlDeficientSites() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
        maxAge = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(maxAgeHours));
    }
    
    @Test
    public void testCrawl() {
        
        String [] sites = this.getSitenames();
        
        List<String> list = new ArrayList(Arrays.asList(sites));
        
//        Collections.reverse(list);
        
        sites = list.toArray(new String[0]);
        
        final String defaultConfigName = this.getCapturerApp().getDefaultConfigname();
     
        for(String site:sites) {
          
            if(defaultConfigName.equalsIgnoreCase(site)) {
                continue;
            }
            
//            final boolean accept = this.acceptSpecific(site);
//            if(!accept) {
//                continue;
//            }

            
            
            final boolean toBeUpdated;
            final String key;
            
            if(false) {
                toBeUpdated = this.isDeficient(site);
                key = "deficient";
            }else{
                toBeUpdated = this.isAged(site);
                key = "aged";
            }
            
System.out.println("==== = ==  === = = = = = = = =  Site: "+site+", " + key + ':' + toBeUpdated);
            
            if(toBeUpdated) {
                
                Integer result = this.crawlsite(site, this.crawlLimit, this.parseLimit);
                
System.out.println("==== = ==  === = = = = = = = =  Updated: "+result);
            }
        }
    }
    
    private boolean acceptSpecific(String site) {
        String s = site.toLowerCase();
        final boolean accept = (
                s.startsWith("lindaikeji") || 
                s.startsWith("bellanaija") ||
                s.startsWith("dailytrust") ||
//                s.startsWith("naij") ||
                s.startsWith("leadership")
        ); 
        return accept;
    }
    
    public Integer crawlsite(String site, int crawlLimit, int parseLimit) {
        
        final JsonConfig config = this.getCapturerApp().getConfigFactory().getConfig(site);
        
        final FeedHandler feedHandler = new InsertFeedToDatabase(this.getIdiscApp().getJpaContext());
        
        WebFeedCrawler crawler = new TestNewsCrawler(
                debug, config, timeout, timeoutUnit, 
                maxFailsAllowed, feedHandler, resumable, resume);
        
        crawler.setCrawlLimit(crawlLimit);
        
        crawler.setParseLimit(parseLimit);
        
        return crawler.call();
    }

    public boolean isDeficient(String site) {
        
        final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
        
        final Integer count = new FeedService(jpaContext).getNumberOfFeedsAfter(site, maxAge);
        
        return count == null || count < max;
    }
    
    public boolean isAged(String site) {
        
        Feed mostRecent = new FeedService(this.getIdiscApp().getJpaContext()).getMostRecentForSite(site).orElse(null);
        
        if(mostRecent == null) {
            return true;
        }else{
            Date feeddate = mostRecent.getFeeddate();
System.out.println("==== = ==  === = = = = = = = =  For site: "+site+", most recent has feeddate: "+feeddate);            
            return feeddate.before(maxAge);
        }
    }
}
