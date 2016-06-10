package com.idisc.core.html;

import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public class FeedSummaryHtml extends FeedCellHtml {

    private final int maxContentLength;

    public FeedSummaryHtml(
            String baseUrl, String contextPath) {
        this(baseUrl, contextPath, 150);
    }
    
    public FeedSummaryHtml(
            String baseUrl, String contextPath,
            String cellTagName, String cellStyle) {
        this(baseUrl, contextPath, 150, cellTagName, cellStyle);
    }
    
    public FeedSummaryHtml(
            String baseUrl, String contextPath, int maxContentLength) {
        super(baseUrl, contextPath, null, null);
        this.maxContentLength = maxContentLength < 1 ? Integer.MAX_VALUE : maxContentLength;
    }

    public FeedSummaryHtml(
            String baseUrl, String contextPath, int maxContentLength,
            String cellTagName, String cellStyle) {
        super(baseUrl, contextPath, cellTagName, cellStyle);
        this.maxContentLength = maxContentLength < 1 ? Integer.MAX_VALUE : maxContentLength;
    }
    
    @Override
    protected void doAppendHtml(Feed feed, StringBuilder appendTo) {
        appendTo.append("<a href=\"").append(this.getLinkId(feed, maxContentLength)).append("\">");
        appendTo.append(this.getSummary(feed, maxContentLength));
        appendTo.append("</a>");
    }

    public final int getMaxContentLength() {
        return maxContentLength;
    }
}
