package com.idisc.core.extraction.web;

import com.bc.json.config.JsonConfig;
import java.util.concurrent.TimeUnit;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import com.idisc.core.FeedHandler;
import com.scrapper.context.CapturerContext;
import com.bc.dom.HtmlDocument;

/**
 * @author Josh
 */
public class TestNewsCrawler extends WebFeedCrawler {
    
    private final boolean debug;

    public TestNewsCrawler(boolean debug, JsonConfig config, long timeout, TimeUnit timeoutUnit, int maxFailsAllowed, FeedHandler feedHandler) {
        super(config, timeout, timeoutUnit, maxFailsAllowed, feedHandler);
        this.debug = debug;
        this.init();
    }

    public TestNewsCrawler(boolean debug, JsonConfig config, long timeout, TimeUnit timeoutUnit, int maxFailsAllowed, FeedHandler feedHandler, boolean resumable, boolean toResume) {
        super(config, timeout, timeoutUnit, maxFailsAllowed, feedHandler, resumable, toResume);
        this.debug = debug;
        this.init();
    }

    public TestNewsCrawler(boolean debug, CapturerContext context, long timeout, TimeUnit timeoutUnit, int maxFailsAllowed, FeedHandler feedHandler) {
        super(context, timeout, timeoutUnit, maxFailsAllowed, feedHandler);
        this.debug = debug;
        this.init();
    }

    public TestNewsCrawler(boolean debug, CapturerContext context, long timeout, TimeUnit timeoutUnit, int maxFailsAllowed, FeedHandler feedHandler, boolean resumable, boolean toResume) {
        super(context, timeout, timeoutUnit, maxFailsAllowed, feedHandler, resumable, toResume);
        this.debug = debug;
        this.init();
    }
    
    private void init() {
        JsonConfig config = this.getContext().getConfig();
        String url = config.getString("url", "start");
        this.setStartUrl(url);
    }
    
    @Override
    public boolean isToBeCrawled(String link) {
        boolean b = super.isToBeCrawled(link); 
if(debug) System.out.println("Links left: "+this.getPageLinks().size()+", is to be crawled: "+b+", link: "+link);                
        return b;
    }

    @Override
    public NodeList parse(String url) throws ParserException {
        NodeList list = super.parse(url); 
if(debug) System.out.println("Links left: "+this.getPageLinks().size()+", last parse yielded: "+(list==null?null:list.size())+" nodes, link: "+url);                
        return list;
    }

    @Override
    public HtmlDocument next() {
        HtmlDocument nodes = super.next();
        return nodes;
    }

    @Override
    public boolean hasNext() {
        boolean b = super.hasNext();
if(debug) System.out.println("Has next: "+b);                
        return b;
    }
    
    @Override
    public boolean shouldStop() {
        boolean shouldStop = super.shouldStop();
        if(debug) {
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
