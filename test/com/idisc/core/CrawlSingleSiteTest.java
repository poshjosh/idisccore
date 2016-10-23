package com.idisc.core;

import com.idisc.core.web.NewsCrawler;
import com.idisc.core.web.TestNewsCrawler;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
        boolean updateDatabase = false;
        boolean debug = true;
//        boolean prelocateTarget = true;
//        this.extractSingleNode(site, "targetNode0", prelocateTarget);
        this.extractSingleUrl(site, updateDatabase, debug);
//        this.extractSite(site, 100, 30, updateDatabase, debug);
        
    }
    
    private String getSite() {
        String site;
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
            boolean updateDatabase, boolean debug) throws Exception {
        
        NewsCrawler crawler = new TestNewsCrawler(
                site, debug, 5, TimeUnit.MINUTES, 9, true, false){
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
        
        Collection<Feed> feeds = crawler.call();
        
log("Extracted "+(feeds==null?null:feeds.size())+" feeds");  

        if(updateDatabase) {
            
            final FeedResultUpdater updater = new FeedResultUpdater();
        
            Collection<Feed> failedToCreate = updater.process("Web Feeds", feeds);
        
log("Failed to create: "+failedToCreate.size()+" feeds: "+failedToCreate);        
        }
        
        Iterator<Feed> iter = feeds.iterator();
        
log(true, " = = = = = = = = = = = = PRINTING FEED(S) = = = = = = = = = = = =  ");            
        while(iter.hasNext()) {
        
            Feed feed = iter.next();
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
        }
        
//        JpaContext jpaContext = this.getIdiscApp().getJpaContext();
//        final int updateCount = jpaContext.getEntityController(Feed.class, Integer.class).create(new ArrayList(feeds));
//log("Update count: "+updateCount);        
    }
}
