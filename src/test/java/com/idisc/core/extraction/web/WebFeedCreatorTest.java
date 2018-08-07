/*
 * Copyright 2018 NUROX Ltd.
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
import com.bc.util.Util;
import com.idisc.core.ExtractionTestBase;
import com.idisc.core.SiteNames;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.htmlparser.dom.HtmlDocument;
import org.junit.Test;
import com.bc.webdatex.context.ExtractionContext;
import com.idisc.core.extraction.FeedCreationContext;
import com.bc.webdatex.context.ExtractionContextFactory;
import com.idisc.core.extraction.ScrapContext;

/**
 * @author Josh
 */
public class WebFeedCreatorTest extends ExtractionTestBase {
    
    private static ExtractionContextFactory contextFactory;
    private static JpaContext jpa;
    private static float tolerance;

    public WebFeedCreatorTest() {  
        if(contextFactory == null) {
            try{
                contextFactory = this.getExtractionContextFactory();
                jpa = this.createJpaContext();
                tolerance = 0.0f;
            }catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Test of createFeed method, of class WebFeedCreator.
     */
    @Test
    public void testCreateFeed() {
        this.testCreateFeed(
                SiteNames.AITONLINE_NEWS, SiteNames.THISDAY, SiteNames.NGRGUARDIANNEWS, 
                SiteNames.DAILY_TRUST, SiteNames.NAIJ, SiteNames.PUNCH_NG);
//        this.testCreateFeed(this.getSitenames());
    }
    
    /**
     * Test of updateFeed method, of class WebFeedCreator.
     */
//    @Test
    public void testUpdateFeed() {
        this.testUpdateFeed(
                SiteNames.AITONLINE_NEWS, SiteNames.THISDAY, SiteNames.NGRGUARDIANNEWS, 
                SiteNames.DAILY_TRUST, SiteNames.NAIJ, SiteNames.PUNCH_NG);
//        this.testUpdateFeed(this.getSitenames());
    }
    
    public void testCreateFeed(String... sites) {
        for(String site : sites) {
            Runtime.getRuntime().gc();
            try{ Thread.sleep(2000); }catch(InterruptedException e) { e.printStackTrace(); }
            this.test(site, "createFeed");
        }
    }
    
    public void testUpdateFeed(String... sites) {
        for(String site : sites) {
            Runtime.getRuntime().gc();
            try{ Thread.sleep(2000); }catch(InterruptedException e) { e.printStackTrace(); }
            this.test(site, "updateFeed");
        }
    }
    
    public void test(String site, String type) {
        final String [] urls = this.getUrls(site, new String[0]);
        this.test(site, type, urls);
    }
    
    public void test(String site, String type, String [] urls) {
        System.out.println(type);
        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        final List<Feed> feeds = new ArrayList();
        final WebFeedCreator instance = this.getInstance(site);
        for(String url : urls) {
            final HtmlDocument doc;
            try{
                doc = this.getNodes(url);
            }catch(IOException e) {
                System.err.println("Failed to parse: " + url);
                e.printStackTrace();
                continue;
            }
//            final Date dateCreatedIfNone = new Date();
            final Feed feed;
            switch(type) {
                case "createFeed":
                    feed = instance.createFeed(doc);
                    break;
                case "updateFeed":    
                    feed = new Feed();
                    instance.updateFeed(feed, doc, instance.parseMetadata(doc));
                    break;
                default:
                    throw new UnsupportedOperationException("Unexpected type: " + type);
            }
            if(feed != null) {
                feeds.add(feed);
            }
        }
        System.out.println("Done creating "+feeds.size()+" feeds for site: " + site + 
                ", consumed. time: " + (System.currentTimeMillis()-tb4) +
                ", memory: " + Util.usedMemory(mb4));
        
        
        feeds.forEach((feed) -> System.out.println(instance.getContext().toString(feed)));
    }

    public WebFeedCreator getInstance(String siteName) {
        final ExtractionContext extractionContext = contextFactory.getContext(siteName);
        final FeedCreationContext creationContext = FeedCreationContext.builder()
                .with(jpa, ScrapContext.TYPE_WEB, extractionContext.getExtractionConfig())
                .build();
        return new WebFeedCreator(extractionContext, creationContext, tolerance);
    }
}
