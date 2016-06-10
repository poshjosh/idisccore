package com.idisc.core.html;

import com.bc.html.HtmlGen;
import com.idisc.pu.entities.Feed;
import java.util.Arrays;
import java.util.List;

/**
 * @author Josh
 */
public class FeedListItemHtml extends HtmlGen implements ToHtml<Feed> {

    private List<ToHtml<Feed>> itemCellHtmls;
    
    public FeedListItemHtml(String baseUrl, String contextPath, String iconRelativePath) {
        this(
            new FeedIconHtml(baseUrl, contextPath, iconRelativePath), 
            new FeedSummaryHtml(baseUrl, contextPath)
        );
    }

    public FeedListItemHtml(
            String baseUrl, String contextPath, String iconRelativePath,
            String [] cellTagNames, String [] cellStyles) {
        this(
            new FeedIconHtml(
                    baseUrl, contextPath, iconRelativePath, 
                    cellTagNames==null?null:cellTagNames[0], cellStyles==null?null:cellStyles[0]
            ), 
            new FeedSummaryHtml(
                    baseUrl, contextPath, 
                    cellTagNames==null?null:cellTagNames[1], cellStyles==null?null:cellStyles[1]
            )
        );
    }

    public FeedListItemHtml(String baseUrl, String contextPath, String iconRelativePath, 
            int maxContentLength, int iconWidthPixels, int iconHeightPixels) {
        this(
            new FeedIconHtml(baseUrl, contextPath, iconRelativePath, iconWidthPixels, iconHeightPixels),
            new FeedSummaryHtml(baseUrl, contextPath, maxContentLength)
        );
    }
    
    public FeedListItemHtml(String baseUrl, String contextPath, String iconRelativePath, 
            int maxContentLength, int iconWidthPixels, int iconHeightPixels,
            String [] cellTagNames, String [] cellStyles) {
        this(
            new FeedIconHtml(
                    baseUrl, contextPath, iconRelativePath, 
                    iconWidthPixels, iconHeightPixels, 
                    cellTagNames==null?null:cellTagNames[0], cellStyles==null?null:cellStyles[0]
            ),
            new FeedSummaryHtml(
                    baseUrl, contextPath, maxContentLength, 
                    cellTagNames==null?null:cellTagNames[1], cellStyles==null?null:cellStyles[1]
            )
        );
    }
    
    public FeedListItemHtml(ToHtml<Feed>... itemCellHtmls) {
        this.itemCellHtmls = Arrays.asList(itemCellHtmls);
    }

    @Override
    public String toHtml(Feed feed) {
        StringBuilder appendTo = new StringBuilder();
        this.appendHtml(feed, appendTo);
        return appendTo.toString();
    }

    @Override
    public void appendHtml(Feed feed, StringBuilder appendTo) {
        for(ToHtml<Feed> toHtml:this.itemCellHtmls) {
            toHtml.appendHtml(feed, appendTo);
        }
    }

    public final List<ToHtml<Feed>> getItemCellHtmls() {
        return itemCellHtmls;
    }
}
