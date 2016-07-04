package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.util.Util;
import com.idisc.core.FeedResultUpdater;
import com.idisc.core.IdiscTestBase;
import com.idisc.pu.entities.Feed;
import com.scrapper.CapturerApp;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.ConfigurationException;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class NewsCrawlerTest extends IdiscTestBase {
    
    public NewsCrawlerTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
    }

    /**
     * Test of doRun method, of class NewsCrawler.
     */
    @Test
    public void testRun() {
        
        System.out.println("run");
        String site = "lindaikeji.blogspot";
//        site = "dailytrust";
//        site = "bellanaija";
        
        NewsCrawler instance = this.createCrawler(site); 
        
        instance.setCrawlLimit(100);
        instance.setParseLimit(30);
        
        instance.run();
        
        final Collection<Feed> result = instance.getResult();
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Results: "+(result==null?null:result.size()));

        final int updated = new FeedResultUpdater().process(instance.getTaskName(), result);
        
System.out.println("= = = = = == ==== = ==  === = = = = = = = =  Updated: "+updated);
    }
    
    private String getLink(String site) {
        String [] links;
        switch(site) {
            case "dailytrust":
                links = new String[]{
                    "http://www.dailytrust.com.ng/news/general/polio-fg-to-include-govs-in-task-force/130064.html",
                    "http://www.dailytrust.com.ng/news/general/metuh-s-handcuffs-in-order-oshiomhole/130057.html",
                    "http://www.dailytrust.com.ng/news/politics/2019-inec-to-deploy-new-tech-for-collation/130017.html"};
                break;
            case "lindaikeji.blogspot":
                links = new String[]{"http://www.lindaikejisblog.com/2016/01/origin-of-first-rosary.html"};
                break;
            default:
                throw new IllegalArgumentException();
        }
        
        int random = Util.randomInt(links.length);
        
        return links[random];
    }
    
    private NewsCrawler createCrawler(String site) {
        
        if(site == null) {
            throw new NullPointerException("Site == null");
        }
        
        JsonConfig config = CapturerApp.getInstance().getConfigFactory().getConfig(site);
        
//System.out.println("oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo\nSite: "+site+", config: "+config);

        if(config == null) {
            throw new NullPointerException("Config == null for site: "+site);
        }

        Collection buffer = new ArrayList<>();
        
        NewsCrawler crawler = new NewsCrawler(config, 2, TimeUnit.MINUTES, 20, buffer){
            @Override
            public boolean isToBeCrawled(String link) {
                boolean b = super.isToBeCrawled(link); 
if(b) System.out.println("Links left: "+this.getPageLinks().size()+", is to be crawled: "+b+", link: "+link);                
                return b;
            }

            @Override
            public NodeList parse(String url) throws ParserException {
                NodeList list = super.parse(url); 
System.out.println("Links left: "+this.getPageLinks().size()+", last parse yielded: "+(list==null?null:list.size())+" nodes, links: "+url);                
                return list;
            }

            @Override
            public PageNodes next() {
                PageNodes nodes = super.next();
                return nodes;
            }

            @Override
            public boolean hasNext() {
                boolean b = super.hasNext();
//System.out.println("Has next: "+b);                
                return b;
            }
            @Override
            public String getTaskName() {
                return "Extract Web Feeds from "+this.getSitename();
            }
            @Override
            public boolean isResumable() {
                return false;
            }
            @Override
            public boolean isResume() {
                return false;
            }
        };

        String url = config.getString("url", "start");

        crawler.setStartUrl(url);
        
        return crawler;
    }
}
