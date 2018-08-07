/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.idisc.core.extraction.web;

import com.bc.jpa.context.JpaContext;
import com.idisc.core.IdiscApp;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.SiteNames;
import com.idisc.core.SubmitTasks;
import com.idisc.core.extraction.ScrapContextImpl;
import com.idisc.pu.FeedDao;
import com.idisc.pu.entities.Feed;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import com.idisc.core.extraction.ScrapContext;
import com.idisc.core.extraction.scrapconfig.ScrapWebConfig;
import org.htmlparser.dom.HtmlDocument;

/**
 * @author Josh
 */
public class WebFeedTaskTest extends IdiscTestBase {
    
    private final ScrapContext<HtmlDocument, Integer> scrapContext;
    
    private final IdiscApp app;
    
    private final int maxAgeHours = 6;
    private final int max = maxAgeHours * 5;
    private final Date maxAge;
    
    public WebFeedTaskTest() { 
        
        maxAge = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(maxAgeHours));
        System.out.println("Max age: " + maxAge);

        this.app = this.getIdiscApp();
        
        final ScrapWebConfig webScrapConfig = new ScrapWebConfig(app.getConfiguration());
        
        webScrapConfig.setTimeoutPerSite(360);
        webScrapConfig.setMaxFailsAllowed(9);
        webScrapConfig.setMaxConcurrentUnits(3);
        webScrapConfig.setSiteLimit(6);
        webScrapConfig.setTimeout(720);
        webScrapConfig.setTimeUnit(TimeUnit.SECONDS);
        webScrapConfig.setCrawlLimit(maxAgeHours * 500);
        webScrapConfig.setParseLimit(webScrapConfig.getCrawlLimit() * 10);
        webScrapConfig.setScrapLimit(webScrapConfig.getParseLimit() * 10);
        
        final Collection<String> names;
        
//        names = new ArrayList(
//                app.getExtractionContextFactory().getConfigService().getConfigNamesLessDefaultConfig()
//        );
        names = Arrays.asList(
//                SiteNames.AITONLINE_NEWS, 
                SiteNames.THISDAY, SiteNames.NGRGUARDIANNEWS, 
                SiteNames.DAILY_TRUST, SiteNames.NAIJ, SiteNames.PUNCH_NG);
        
        System.out.println("  All names: " + names);
        
        scrapContext = new ScrapContextImpl(
                names, 
                app.getJpaContext(),
                webScrapConfig,
                new WebFeedTaskProvider(
                        app.getJpaContext(), 
                        app.getExtractionContextFactory(), 
                        app.getScrapConfigFactory()
                )
        );
    }
    
    /**
     * Test of call method, of class WebFeedTask.
     */
    @Test
    public void testCallMultipleTimes() {
        
        long tb4 = System.currentTimeMillis();
        long mb4 = com.bc.util.Util.availableMemory();

        try{
            
            new SubmitTasks(scrapContext).call();
            
        }catch(Exception e) {
            
            e.printStackTrace();
            
        }finally{
            System.out.println("Completed tasks: " +  
                    "\n--------------------------------------\n"+
                    "Consumed time: " + (System.currentTimeMillis()-tb4)+
                    ", memory: "+com.bc.util.Util.usedMemory(mb4));
        }
    }

    public boolean isDeficient(String site) {
        
        final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
        
        final Long count = new FeedDao(jpaContext).getNumberOfFeedsAfter(site, maxAge);
        
        final boolean deficient = count == null || count < max;
        
        System.out.println("Is deficient: " + deficient + ", site: " + site + ", max age: " + maxAge);
        
        return deficient;
    }
    
    public boolean isAged(String site) {
        
        Feed mostRecent = new FeedDao(this.getIdiscApp().getJpaContext()).getMostRecentForSite(site).orElse(null);
        
        if(mostRecent == null) {
            return true;
        }else{
            Date feeddate = mostRecent.getFeeddate();
System.out.println("==== = ==  === = = = = = = = =  For site: "+site+", most recent has feeddate: "+feeddate);            
            return feeddate.before(maxAge);
        }
    }
}
