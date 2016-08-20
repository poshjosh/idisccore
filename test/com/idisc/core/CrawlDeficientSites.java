package com.idisc.core;

import com.bc.jpa.JpaContext;
import com.idisc.core.web.NewsCrawler;
import com.idisc.core.web.TestNewsCrawler;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Site_;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.persistence.TypedQuery;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;
import com.bc.jpa.dao.BuilderForSelect;

/**
 * @author Josh
 */
public class CrawlDeficientSites extends IdiscTestBase {
    
    private final boolean debug = false;
    private final int maxAgeHours = 4;
    private final int parseLimit = maxAgeHours * 10;
    private final int crawlLimit = parseLimit * 3;
    private final int max = parseLimit / 2;
    
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
        
        for(String site:sites) {
            
            if("default".equalsIgnoreCase(site)) {
                continue;
            }
            
            final boolean accept = this.acceptSpecific(site);
            if(!accept) {
                continue;
            }
            
            final boolean deficient = this.isDeficient(site);
            
System.out.println("==== = ==  === = = = = = = = =  Site: "+site+", deficient: "+deficient);
            
            if(deficient) {
                
                Collection<Feed> result = this.crawlsite(site, this.crawlLimit, this.parseLimit);
                
System.out.println("==== = ==  === = = = = = = = =  Results: "+(result==null?null:result.size()));

                final int updated = new FeedResultUpdater().process(site, result);
        
System.out.println("==== = ==  === = = = = = = = =  Updated: "+updated);
            }
        }
    }
    
    private boolean acceptSpecific(String site) {
        String s = site.toLowerCase();
        final boolean accept = (
                s.startsWith("lindaikeji") || 
                s.startsWith("bellanaija") ||
                s.startsWith("dailytrust") ||
                s.startsWith("naij") ||
                s.startsWith("leadership")
        ); 
        return accept;
    }
    
    public boolean isDeficient(String site) {
        
        JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
        
        final Integer siteId;
        try(BuilderForSelect<Integer> qb = jpaContext.getBuilderForSelect(Site.class, Integer.class)) {
            siteId = qb.select(Site.class, Site_.siteid.getName())
            .where(Site_.site.getName(), site).createQuery().getSingleResult();
        }catch(javax.persistence.NoResultException e) {
            throw e;
        }
        
        try(BuilderForSelect<Number> qb = jpaContext.getBuilderForSelect(Feed.class, Number.class)) {
            TypedQuery<Number> tq = qb.count(Feed.class, Feed_.feedid.getName())
              .where(Feed_.siteid.getName(), siteId)
              .and().where(Feed_.datecreated.getName(), BuilderForSelect.GT, maxAge).createQuery();
            Number count = tq.getSingleResult();
System.out.println("==== = ==  === = = = = = = = =  Site: "+site+", has "+count+" feeds younger than "+maxAgeHours+" hours ago");            
            return count == null || count.intValue() < max;
        }catch(javax.persistence.NoResultException e) {
            return true;
        }
    }
    
    public Collection<Feed> crawlsite(String site, int crawlLimit, int parseLimit) {
        
        NewsCrawler crawler = new TestNewsCrawler(site, debug);
        
        crawler.setCrawlLimit(crawlLimit);
        
        crawler.setParseLimit(parseLimit);
        
        return crawler.call();
    }
    
}
