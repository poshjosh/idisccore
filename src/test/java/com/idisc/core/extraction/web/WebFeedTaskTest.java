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
import com.idisc.core.extraction.ExtractionContext;
import com.idisc.core.functions.GetSubList;
import com.idisc.pu.FeedDao;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import org.junit.Test;

/**
 * @author Josh
 */
public class WebFeedTaskTest extends IdiscTestBase {
    
    private final boolean acceptDuplicates = false;
    private final int timeoutPerSiteSeconds = 360;
    private final int maxFails = 9;
    private final int maxConcurrent = 3;
    private final int sitesPerBatch = 6;
    private final int timeoutSeconds = 720; //timeoutPerSite * sitesPerBatch / maxConcurrent;

    private final ExtractionContext webContext;
    
    private final IdiscApp app;
    
    private final int maxAgeHours = 6;
    private final int parseLimit = maxAgeHours * 10;
    private final int crawlLimit = parseLimit * 10;
    private final int max = maxAgeHours * 5;
    private final Date maxAge;
    
    public WebFeedTaskTest() { 
        
        maxAge = new Date(System.currentTimeMillis() - TimeUnit.HOURS.toMillis(maxAgeHours));
        System.out.println("Max age: " + maxAge);

        this.app = this.getIdiscApp();
        
        webContext = new ExtractionContextForWebPages(
                app.getExtractionFactory().getNames("web"), 
                new GetSubList(),
                new WebFeedTaskProvider(
                        app.getJpaContext(), 
                        app.getScrapperContextFactory(), 
                        0.1f,
                        timeoutPerSiteSeconds, TimeUnit.SECONDS,
                        crawlLimit, parseLimit,
                        maxFails, acceptDuplicates
                )
        );
    }
    
    /**
     * Test of call method, of class WebFeedTask.
     */
    @Test
    public void testCallMultipleTimes() {
        System.out.println("  All names: " + webContext.getNames());

        final List<String> webNames = new ArrayList(webContext.getNextNames(sitesPerBatch));
        
        final Predicate<String> isDeficient = (name) -> this.isDeficient(name);
        
//        this.call(webNames.stream().filter(isDeficient).collect(Collectors.toList()));

        final List<String> customNames = Arrays.asList(
                SiteNames.AITONLINE_NEWS, SiteNames.THISDAY, SiteNames.NGRGUARDIANNEWS, 
                SiteNames.DAILY_TRUST, SiteNames.NAIJ, SiteNames.PUNCH_NG);
        
        this.call(customNames);
    }
    
    private void call(List<String> webNames) {
        
        long tb4 = System.currentTimeMillis();
        long mb4 = com.bc.util.Util.availableMemory();
        
        Collections.sort(webNames, app.getExtractionFactory().getNamesComparator("web"));
        
        System.out.println("Sorted names: " + webNames);
        
        new SubmitTasks(
                webNames, 
                webContext.getTaskProvider(),
                timeoutSeconds,
                TimeUnit.SECONDS,
                maxConcurrent
        ).call();
        
        System.out.println("Completed tasks: " + webNames + 
                "\n--------------------------------------\n"+
                "Consumed time: " + (System.currentTimeMillis()-tb4)+
                ", memory: "+com.bc.util.Util.usedMemory(mb4));
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
