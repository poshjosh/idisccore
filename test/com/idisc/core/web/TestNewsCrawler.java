package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.bc.webdatex.nodedata.Dom;
import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public class TestNewsCrawler extends NewsCrawler {
    
    private final boolean debug;
    
    public TestNewsCrawler(String site, boolean debug, 
            long timeout, TimeUnit timeoutUnit, int maxFailsAllowed,
            boolean resumable, boolean resume) {
        
        super(CapturerApp.getInstance().getConfigFactory().getContext(site).getConfig(),
                timeout, timeoutUnit, maxFailsAllowed, new ArrayList<Feed>(),
                resumable, resume);
        
        this.debug = debug;
        
        JsonConfig config = this.getContext().getConfig();
        
        String url = config.getString("url", "start");

        this.setStartUrl(url);
    }
    
    @Override
    public boolean isToBeCrawled(String link) {
        boolean b = super.isToBeCrawled(link); 
if(debug && b) System.out.println("Links left: "+this.getPageLinks().size()+", is to be crawled: "+b+", link: "+link);                
        return b;
    }

    @Override
    public NodeList parse(String url) throws ParserException {
        NodeList list = super.parse(url); 
if(debug) System.out.println("Links left: "+this.getPageLinks().size()+", last parse yielded: "+(list==null?null:list.size())+" nodes, link: "+url);                
        return list;
    }

    @Override
    public Dom next() {
        Dom nodes = super.next();
        return nodes;
    }

    @Override
    public boolean hasNext() {
        boolean b = super.hasNext();
if(debug && !b) System.out.println("Has next: "+b);                
        return b;
    }
    
    @Override
    public boolean shouldStop() {
        boolean shouldStop = super.shouldStop();
        if(debug && shouldStop) {
            System.out.println("Should stop: "+shouldStop+", timeout: "+this.getTimeout()+
                    ", timespent: "+this.getTimeSpent()+", max fails: "+this.getMaxFailsAllowed()+
                    ", current fails: "+this.getFailedCount());
        }
        return shouldStop;
    }
    
    @Override
    public String getTaskName() {
        return this.getClass().getName() + " for " + this.getContext().getConfig().getName();
    }
}
