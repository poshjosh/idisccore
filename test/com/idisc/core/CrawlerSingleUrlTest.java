package com.idisc.core;

import com.bc.json.config.JsonConfig;
import com.bc.webdatex.locator.TagLocator;
import com.idisc.core.web.NewsCrawler;
import com.idisc.pu.entities.Feed;
import com.scrapper.URLParser;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import com.scrapper.extractor.MultipleNodesExtractor;
import com.scrapper.extractor.MultipleNodesExtractorIx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;
import com.bc.jpa.JpaContext;

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
public class CrawlerSingleUrlTest extends IdiscTestBase {
    
    public CrawlerSingleUrlTest() throws Exception {
        super(Level.FINER);
    }
    
    @Test
    public void test() throws Exception {
        boolean preLocateTarget = true;
        final String site = this.getSite();
//        this.extractSingleNode(site, "targetNode0", preLocateTarget);
        this.extractSingleUrl(site);
    }
    
    private String getSite() {
        String site;
//        site = "thisday";
//        site = "naij";
//        site = "dailytrust";
//        site = "punchng";
//        site = "channelstv_headlines";
        site = "bellanaija";
//        site = "lindaikeji.blogspot";
//        site = "ngrguardiannews";
//        site = "thenationonlineng";
        return site;
    }
    
    public void extractSingleUrl(String site) throws Exception {
        
        String sampleUrl = this.getUrl(site);
        
log("URL to extract: "+ sampleUrl);        
 
        ScrapperConfigFactory factory = getCapturerApp().getConfigFactory();
        
        CapturerContext ctx = factory.getContext(site);
        
        JsonConfig config = ctx.getConfig();
        Collection<Feed> resultBuffer = new ArrayList<>();
        
        NewsCrawler crawler = new NewsCrawler(config, 5, TimeUnit.MINUTES, 20, resultBuffer){
            @Override
            public boolean isResume() {
                return false;
            }
        };
        
//        com.bc.manager.Filter<String> urlFilter;
//        urlFilter = crawler.getContext().getCaptureUrlFilter();
//        if(urlFilter instanceof DefaultUrlFilter) {
//            ((DefaultUrlFilter)urlFilter).setMaxAgeDays(2);
//        }
        
        crawler.setParseLimit(1);
        crawler.setCrawlLimit(1);
        
        List<String> urls = crawler.getPageLinks();
        if(urls == null) {
            urls = new ArrayList<>();
            crawler.setPageLinks(urls);
        }else{
            urls.clear();
        }
        urls.add(sampleUrl);
        
log("Begining extract");        
        crawler.run();
        
        Collection<Feed> feeds = crawler.getResult();
        
log("Extracted "+(feeds==null?null:feeds.size())+" feeds");        
        
        Iterator<Feed> iter = feeds.iterator();
        
        while(iter.hasNext()) {
        
            Feed feed = iter.next();
log(true, " = = = = = = = = = = = =  PRINTING FEED = = = = = = = = = = = =  ");            
log(false, "Title: "+feed.getTitle());            
log(false, "Author: "+feed.getAuthor());
log(false, "Feeddate: "+feed.getFeeddate());
log(false, "Image URL: "+feed.getImageurl());
log(false, "Description: "+feed.getDescription());
log(false, "Content: "+feed.getContent());
        }
        
//        JpaContext jpaContext = this.getIdiscApp().getJpaContext();
//        final int updateCount = jpaContext.getEntityController(Feed.class, Integer.class).create(new ArrayList(feeds));
//log("Update count: "+updateCount);        
    }
    
    private void extractSingleNode(String site, String key, boolean preLocateTarget) throws ParserException {
        
        MultipleNodesExtractorIx pageExtractor = getPageExtractor(site);

        com.bc.webdatex.extractor.NodeExtractor nodeExtractor = 
                ((MultipleNodesExtractor)pageExtractor).getExtractor(key);

        String url = this.getUrl(site);

        URLParser p = new URLParser();
        NodeList nodes = p.parse(url);

        if(preLocateTarget) {
            
            NodeList targetNodes = preLocateTarget(nodes, nodeExtractor);

            if(targetNodes != null) {
                nodes = targetNodes;
            }
        }
        
        nodes.visitAllNodesWith(nodeExtractor);
            
log("EXTRACT:\n"+nodeExtractor.getExtract());
    }

    private MultipleNodesExtractorIx getPageExtractor(String site) {
        CapturerContext ctx;
        ctx = getCapturerApp().getConfigFactory().getContext(site);
        MultipleNodesExtractorIx pageExtractor = ctx.getExtractor();
        return pageExtractor;
    }
    
    private NodeList preLocateTarget(
            NodeList nodes, com.bc.webdatex.extractor.NodeExtractor nodeExtractor) 
            throws ParserException {

        TagLocator locator = (TagLocator)nodeExtractor.getFilter().getTagLocator();

        List<String> [] transverse = locator.getTransverse();
        
log("transverse:\n"+(transverse==null?null:Arrays.toString(transverse)));

        nodes.visitAllNodesWith(locator);

        Tag target = locator.getTarget();

log("Target:\n"+(target==null?null:target.toHtml(false)));

        if(target == null) {
            return null;
        }
        
        nodes = new NodeList();
        nodes.add(target);

        return nodes;
    }

    private Object [] get(JsonConfig props, String propertyKey) {
        // DIV,,,DIV,,,DIV,,,SPAN P SPAN
        // Parents = DIV,DIV,DIV
        // Siblings = SPAN,P,SPAN (of which target is the last SPAN)
        //
        Object [] pathToValue = {propertyKey, Config.Extractor.transverse};
        
        if(pathToValue == null || pathToValue.length == 0) {
            throw new NullPointerException();
        }

        Object [] expectedAll = props.getArray(pathToValue);
        
        if(expectedAll == null) {
            throw new IllegalArgumentException("["+Arrays.toString(pathToValue) + "=null]");
        }
        
        return expectedAll;
    }
    
    public String getUrl(String site) {
        String [] urls;
        switch(site) {
            case "bellanaija":
                urls = new String[]{
                    "https://www.bellanaija.com/2016/06/get-inspired-with-forbes-list-of-amercias-richest-self-made-women-oprah-winfrey-beyonce-taylor-swift-sheryl-sandberg-more/"//,
//                    "https://www.bellanaija.com/2016/03/ty-bello-is-bimpe-onakoyas-biggest-fan-read-her-inspiring-story-on-the-makeup-maestro/",
//                    "https://www.bellanaija.com/2015/06/09/designer-deola-sagoe-is-a-vision-in-gold-in-her-own-piece/"
                };
                break;
            case "lindaikeji.blogspot":
                urls = new String []{
                    "http://www.lindaikejisblog.com/2016/05/okonjo-iweala-reacts-to-reports-that.html",
                    "http://www.lindaikejisblog.com/2016/05/comedian-elenu-and-his-wife-jane-join.html"
//                    "http://www.lindaikejisblog.com/2016/04/manny-pacquiao-beefs-up-security-after.html",
//                    "http://www.lindaikejisblog.com/2016/04/former-nollywood-actress-anita-hogan.html",
//                    "http://www.lindaikejisblog.com/2015/06/dear-lib-readers-my-wife-complains-that.html",
//                    "http://www.lindaikejisblog.com/2015/06/photos-femi-otedolas-daughter-graduates.html",
//                    "http://www.lindaikejisblog.com/2015/06/former-miss-mississippis-boobs-rots.html"
                };
                break;
            case "naij":
                urls = new String[]{
                    "https://www.naij.com/812314-shocking-efcc-uncovers-12-9-billion-arms-deal-fraud.html",
                    "https://www.naij.com/460524-read-happened-men-trekked-atiku.html",
                    "https://www.naij.com/460495-live-ngr-vs-chad-afcon-qualifier.html",
                    "https://www.naij.com/460491-photos-dprince-is-now-a-father.html"
                };
                break;
            case "dailytrust":
                urls = new String[]{
                    "http://www.dailytrust.com.ng/news/general/polio-fg-to-include-govs-in-task-force/130064.html",
                    "http://www.dailytrust.com.ng/news/general/metuh-s-handcuffs-in-order-oshiomhole/130057.html",
                    "http://www.dailytrust.com.ng/news/politics/2019-inec-to-deploy-new-tech-for-collation/130017.html"};
                break;
            case "ngrguardiannews":
                urls = new String[]{
                    "http://www.ngrguardiannews.com/2015/06/malaysian-villagers-beg-spirits-to-end-quake-aftershocks/",
                    "http://www.ngrguardiannews.com/2015/06/singapore-gay-rights-rally-draws-record-crowd-organisers/",
                    "http://www.ngrguardiannews.com/2015/06/new-york-rally-launches-clintons-bid-for-white-house/"
                };
                break;
            case "punchng":
                urls = new String[]{
                    "http://www.punchng.com/bizarre-social-media-craze-accident-victims-groan-as-sympathisers-record-agony-on-smart-phones/",
                    "http://www.punchng.com/rivers-rerun-police-confirm-one-death-12-arrests/"
                };
                break;
            case "saharareporters":
                urls = new String[]{
                    "http://saharareporters.com/2015/06/15/exclusive-garba-shehu-speaks-saharatv-explains-delayed-ministerial-appointments",
                    "http://saharareporters.com/2015/06/15/serap-icc-should-refer-south-africa-un-security-council-refusing-arrest-al-bashir",
                    "http://saharareporters.com/2015/06/15/us-commits-5-billion-military-assistance-against-boko-haram"
                };
                break;
            case "sunnewsonline_breaking":
                urls = new String[]{
                    "http://sunnewsonline.com/new/?p=123586",
                    "http://sunnewsonline.com/new/?p=123571",
                    "http://sunnewsonline.com/new/?p=123565"
                };
                break;
            case "sunnewsonline_national":
                urls = new String[]{
                    "http://sunnewsonline.com/new/?p=123586",
                    "http://sunnewsonline.com/new/?p=123571",
                    "http://sunnewsonline.com/new/?p=123565"
                };
                break;
            case "thenationonlineng":    
                urls = new String[]{
                    "http://thenationonlineng.net/new/first-lady-dont-pay-any-money-to-see-president/",
                    "http://thenationonlineng.net/new/bpe-in-n1-45b-scam/",
                    "http://thenationonlineng.net/new/buhari-considers-balance-in-sgf-choice/"
                };
                break;
            case "thisday":
                urls = new String[]{
                    "http://www.thisdaylive.com/articles/nerc-to-revoke-inoperative-power-generation-licences/212109/",
                    "http://www.thisdaylive.com/articles/ssanu-ask-buhari-to-sack-nuc-executive-secretary/212108/",
                    "http://www.thisdaylive.com/articles/i-have-no-interest-in-sgf-position-says-oyegun/212106/"
                };
                break;
            case "channelstv_headlines":
                urls = new String[]{
                    "http://www.channelstv.com/2015/10/21/tribunal-adjourns-trial-of-bukola-saraki-till-november-5/",
                    "http://www.channelstv.com/2015/10/21/reps-to-investigate-nnpc-joint-venture-operations/",
                    "http://www.channelstv.com/2015/10/19/buhari-meets-with-service-chiefs-gets-assurance-of-peace-in-north-east/"
                };
                break;
            case "aitonline_news":
                urls = new String[]{
                    "http://www.aitonline.tv/post-council_of_state_approves_president___s_nomination_of_new_inec_chairman__5_national_commissioners",
                    "http://www.aitonline.tv/post-lagos_state_govt_donates_n150m_to_adamawa__yobe_and_borno",
                    "http://www.aitonline.tv/post-don_t_compare_me_with_ronaldo_and_messi___lewandowski"
                };
                break;
            default:    
                throw new UnsupportedOperationException("Unexpected site: "+site);
        }        

        int random = com.bc.util.Util.randomInt(urls.length);
        
        return urls[random];
    }
}
