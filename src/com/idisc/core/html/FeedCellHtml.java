package com.idisc.core.html;

import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public abstract class FeedCellHtml extends AbstractFeedHtml {
    
    private final String cellTagName;
    private final String cellStyle;
    
    protected FeedCellHtml(String baseUrl, String contextPath) {
        this(baseUrl, contextPath, null, null);
    }
    
    protected FeedCellHtml(
            String baseUrl, String contextPath, 
            String cellTagName, String cellStyle) {
        super(baseUrl, contextPath);
        this.cellTagName = cellTagName;
        this.cellStyle = cellStyle;
    }
    
    protected abstract void doAppendHtml(Feed feed, StringBuilder appendTo);

    @Override
    public final void appendHtml(Feed feed, StringBuilder appendTo) {
        if(this.cellTagName != null) {
            this.tagStart(cellTagName, "style", cellStyle, appendTo);
        }
        this.doAppendHtml(feed, appendTo);
        if(this.cellTagName != null) {
            this.tagEnd(cellTagName, appendTo);
        }
    }
    
    public final String getCellTagName() {
        return cellTagName;
    }

    public final String getCellStyle() {
        return cellStyle;
    }
}
