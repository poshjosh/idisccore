package com.idisc.core.html;

import com.bc.html.HtmlGen;
import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public abstract class AbstractFeedHtml extends HtmlGen implements ToHtml<Feed> {
    
    private final String contextPath;
    private final String baseUrl;
    
    protected AbstractFeedHtml(String baseUrl, String contextPath) {
        this.baseUrl = baseUrl;
        this.contextPath = contextPath;
    }
    
    @Override
    public abstract void appendHtml(Feed feed, StringBuilder appendTo);
    
    @Override
    public String toHtml(Feed feed) {
        StringBuilder builder = new StringBuilder();
        this.appendHtml(feed, builder);
        return builder.toString();
    }

    protected String getLinkId(Feed feed, int maxLen) {
        final String shortText = this.getSummary(feed, maxLen);
        String localUrl = getUrl("/feed/"+feed.getFeedid()+"_"+shortText.replaceAll("[^a-zA-Z0-9]", "_")+".jsp");        
        return localUrl;
    }
    
    protected String getUrl(String relativePath) {
        return getContextUrl() + relativePath;
    }
    
    protected String getContextUrl() {
        return baseUrl + contextPath;
    }
    
    protected String getSummary(Feed feed, int maxLen) {
        String st = feed.getTitle();
        if(st == null) {
            st = feed.getDescription();
            if(st == null) {
                st = feed.getContent();
            }
        }
        return this.truncate(st, maxLen, true);
    }
    
    protected String truncate(String str, int maxLen, boolean ellipsize) {
        String output;
        if(str == null || str.isEmpty() || str.length() <= maxLen) {
            output = str;
        }else {
            final String prefix = ellipsize ? "..." : "";
            output = str.substring(0, maxLen-prefix.length()) + prefix;
        }
        return output;
    }

    public final String getContextPath() {
        return contextPath;
    }

    public final String getBaseUrl() {
        return baseUrl;
    }
}
