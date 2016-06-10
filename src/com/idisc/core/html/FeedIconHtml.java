package com.idisc.core.html;

import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public class FeedIconHtml extends FeedCellHtml {
    
    private final int iconHeightPx;
    private final int iconWidthPx;
    private final String iconUrl;

    public FeedIconHtml(
            String baseUrl, String contextPath, String iconRelativePath) {
        this(baseUrl, contextPath, iconRelativePath, 64, 64);
    }
    
    public FeedIconHtml(
            String baseUrl, String contextPath, String iconRelativePath,
            String cellTagName, String cellStyle) {
        this(baseUrl, contextPath, iconRelativePath, 64, 64, cellTagName, cellStyle);
    }
    
    public FeedIconHtml(
            String baseUrl, String contextPath, String iconRelativePath, 
            int iconWidthPixels, int iconHeightPixels) {
        this(baseUrl, contextPath, iconRelativePath, iconWidthPixels, iconHeightPixels, null, null);
    }    
    
    public FeedIconHtml(
            String baseUrl, String contextPath, String iconRelativePath, 
            int iconWidthPixels, int iconHeightPixels, String cellTagName, String cellStyle) {
        super(baseUrl, contextPath, cellTagName, cellStyle);
        this.iconUrl = FeedIconHtml.this.getUrl(iconRelativePath);
        this.iconWidthPx = iconWidthPixels;
        this.iconHeightPx = iconHeightPixels;
    }
    
    @Override
    protected void doAppendHtml(Feed feed, StringBuilder appendTo) {
        appendTo.append("<img width=\"").append(iconWidthPx).append("\" height=\"").append(iconHeightPx).append('"');
        appendTo.append(" style=\"width:").append(iconWidthPx).append("px; height:").append(iconHeightPx).append("px;\"");
        appendTo.append(" src=\"").append(feed.getImageurl()==null?iconUrl:feed.getImageurl()).append("\"/>");
    }

    public final int getIconHeightPx() {
        return iconHeightPx;
    }

    public final int getIconWidthPx() {
        return iconWidthPx;
    }

    public final String getIconUrl() {
        return iconUrl;
    }
}
