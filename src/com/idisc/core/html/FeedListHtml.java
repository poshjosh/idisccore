package com.idisc.core.html;

import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public class FeedListHtml extends FeedsHtml {

    public FeedListHtml(
            String baseUrl, String contextPath, String iconRelativePath) {
        this(new FeedListItemHtml(baseUrl, contextPath, iconRelativePath));
    }
    
    public FeedListHtml(String baseUrl, String contextPath, String iconRelativePath, 
            int maxContentLength, int iconWidthPixels, int iconHeightPixels,
            String listStyle, String listItemStyle) {
        this(new FeedListItemHtml(
                baseUrl, contextPath, iconRelativePath,
                maxContentLength, iconWidthPixels, iconHeightPixels),
             listStyle, listItemStyle   
        );
    }
    
    public FeedListHtml(ToHtml<Feed> listItemHtml) {
        //Use list-style-type:none to remove bullets
        //@Microsoft IE9 and below demands specifying list-style-type to none in each <li> also
        this(listItemHtml, "list-style-type:none; margin:0; padding:0; font-size:1.5em; background:#eeeeee", "list-style-type:none; margin:0.5em; padding:0.5em; background:#ffffff");
    }
    
    public FeedListHtml(ToHtml<Feed> listItemHtml, String listStyle, String listItemStyle) {
        super(listItemHtml, listStyle, listItemStyle, "ul", "li");
    }
}
