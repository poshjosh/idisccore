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
import com.bc.jpa.fk.EnumReferences;
import com.bc.meta.Metadata;
import com.bc.nodelocator.ConfigName;
import com.bc.util.Util;
import com.bc.webdatex.context.CapturerContext;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.ExtractionTestBase;
import com.idisc.core.SiteNames;
import com.idisc.pu.References;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.htmlparser.NodeFilter;
import org.htmlparser.dom.HtmlDocument;
import org.junit.Test;
import com.bc.webdatex.context.CapturerContextFactory;

/**
 * @author Josh
 */
public class WebFeedCreatorTest extends ExtractionTestBase {
    
    private static CapturerContextFactory contextFactory;
    private static JpaContext jpa;
    private static float tolerance;
    private static Sitetype sitetype;

    public WebFeedCreatorTest() {  
        if(contextFactory == null) {
            try{
                contextFactory = this.getContextFactory();
                jpa = this.createJpaContext();
                tolerance = 0.0f;
                final EnumReferences refs = jpa.getEnumReferences();
                sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
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
    public void testUpdateFeed_4args() {
        this.testUpdateFeed(
                SiteNames.AITONLINE_NEWS, SiteNames.THISDAY, SiteNames.NGRGUARDIANNEWS, 
                SiteNames.DAILY_TRUST, SiteNames.NAIJ, SiteNames.PUNCH_NG);
//        this.testUpdateFeed(this.getSitenames());
    }
    
    public void testCreateFeed(String... sites) {
        for(String site : sites) {
            this.test(site, "createFeed");
        }
    }
    
    public void testUpdateFeed(String... sites) {
        for(String site : sites) {
            this.test(site, "updateFeed");
        }
    }
    
    public void test(String site, String type) {
        System.out.println(type);
        Runtime.getRuntime().gc();
        try{ Thread.sleep(2000); }catch(InterruptedException e) { e.printStackTrace(); }
        final String [] urls = this.getUrls(site, new String[0]);
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
        
        feeds.forEach((feed) -> System.out.println(instance.toString(feed)));
    }

    public WebFeedCreator getInstance(String siteName) {
        final Site site = new SiteDao(jpa).from(siteName, sitetype, false);
        final CapturerContext context = contextFactory.getContext(siteName);
        final String baseUrl = context.getConfig().getString(ConfigName.url, "value");
        final NodeFilter nodeFilter = new ImageNodeFilter(baseUrl);
        return new WebFeedCreator(contextFactory.getContext(siteName), 
                jpa, site, nodeFilter, tolerance){
            @Override
            public boolean hasEnoughData(Metadata metadata) {
                return super.hasEnoughData(metadata);
            }
        };
    }
}
