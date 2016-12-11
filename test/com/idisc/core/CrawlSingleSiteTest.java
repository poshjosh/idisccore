package com.idisc.core;

import com.bc.json.config.JsonConfig;
import com.idisc.core.web.NewsCrawler;
import com.idisc.core.web.TestNewsCrawler;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.junit.Test;

/**
 * @(#)CrawlerSingleUrlTest.java   13-Jun-2015 15:57:46
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class CrawlSingleSiteTest extends ExtractionTestBase {
    
    public CrawlSingleSiteTest() throws Exception {
        super(Level.FINE);
    }
    
    @Test
    public void test() throws Exception {
        String site = this.getSite();
        boolean updateDatabase = true;
        boolean debug = true;
//        boolean prelocateTarget = true;
//        this.extractSingleNode(site, "targetNode0", prelocateTarget);
        this.extractSingleUrl(site, updateDatabase, debug);
//        this.extractSite(site, 100, 30, updateDatabase, debug);
        
    }
    
    private String getSite() {
        String site;
//        site = "sunnewsonline";
        site = "naij";
//        site = "dailytrust";
//        site = "punchng";
//        site = "channelstv_headlines";
//        site = "bellanaija";
//        site = "lindaikeji.blogspot";
//        site = "thisday";
//        site = "ngrguardiannews";
//        site = "thenationonlineng";
//        site = "vanguardngr";
//        site = "thenewsminute";
        return site;
    }
    
    private void extractSite(String site, int crawlLimit, int parseLimit, 
            boolean updateDatabase, boolean debug) throws Exception {
     
        this.extract(site, null, parseLimit, crawlLimit, updateDatabase, debug);
    }
    
    private void extractSingleUrl(String site, boolean updateDatabase, boolean debug) throws Exception {
        String sampleUrl = this.getUrl(site);
        this.extractSingleUrl(site, sampleUrl, updateDatabase, debug);
    }
  
    private void extractSingleUrl(String site, String sampleUrl, boolean updateDatabase, boolean debug) throws Exception {
log("URL to extract: "+ sampleUrl); 
        this.extract(site, sampleUrl, 1, 1, updateDatabase, debug);
    }
    
    private void extract(
            String site, String sampleUrl, int crawlLimit, int parseLimit, 
            final boolean updateDatabase, boolean debug) throws Exception {
        
        final JsonConfig config = this.getCapturerApp().getConfigFactory().getConfig(site);
        
        final FeedHandler feedHandler =  new InsertFeedToDatabase(this.getIdiscApp().getJpaContext()){
            @Override
            public boolean process(Feed feed) {
log(true, " = = = = = = = = = = = = PRINTING FEED = = = = = = = = = = = =  ");                
//log(false, "ID: "+feed.getFeedid()); // value is null at this stage
log(false, "Title: "+feed.getTitle());      

log(false, "URL: "+feed.getUrl());
log(false, "Author: "+feed.getAuthor());
log(false, "Feeddate: "+feed.getFeeddate());
log(false, "Image URL: "+feed.getImageurl());
log(false, "Keywords: "+feed.getKeywords());      
log(false, "Categories: "+feed.getCategories());    
log(false, "Description: "+feed.getDescription());
log(false, "Content length: "+(feed.getContent()==null?null:feed.getContent().length()));
log(false, "Content: "+(feed.getContent()));
                if(updateDatabase) {
                    return super.process(feed);
                }else{
                    return true; 
                }
            }
        };
        
        NewsCrawler crawler = new TestNewsCrawler(
                debug, config, 5, TimeUnit.MINUTES, 9, feedHandler, true, false){
//            @Override
//            public boolean isInDatabase(String link) {
//                return false;
//            }
        };
        
//        com.bc.manager.Filter<String> urlFilter;
//        urlFilter = crawler.getContext().getCaptureUrlFilter();
//        if(urlFilter instanceof DefaultUrlFilter) {
//            ((DefaultUrlFilter)urlFilter).setMaxAgeDays(2);
//        }

        crawler.setCrawlLimit(crawlLimit);
        crawler.setParseLimit(parseLimit);
        
        if(sampleUrl != null) {
            List<String> urls = crawler.getPageLinks();
            if(urls == null) {
                urls = new ArrayList<>();
                crawler.setPageLinks(urls);
            }else{
                urls.clear();
            }
            urls.add(sampleUrl);
        }
        
log("Begining extract");        
        
        Integer updateCount = crawler.call();
        
log("Extracted "+updateCount+" feeds");  
    }
}
