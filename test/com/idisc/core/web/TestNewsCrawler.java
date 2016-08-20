package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.util.PageNodes;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Josh
 */
public class TestNewsCrawler extends NewsCrawler {
    
    private final boolean debug;
    
    public TestNewsCrawler(String site, boolean debug) {
        
        super(CapturerApp.getInstance().getConfigFactory().getContext(site).getConfig(),
                2, TimeUnit.MINUTES, 20, new ArrayList());
        
        
        
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
if(debug) System.out.println("Links left: "+this.getPageLinks().size()+", last parse yielded: "+(list==null?null:list.size())+" nodes, links: "+url);                
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
}
