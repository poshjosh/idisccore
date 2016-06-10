package com.idisc.core.html;

import com.bc.html.HtmlGen;
import com.idisc.pu.entities.Feed;
import java.util.List;

/**
 * @author Josh
 */
public class FeedsHtml extends HtmlGen implements ToHtml<List<Feed>>{
    
    private final ToHtml<Feed> listItemHtml;
    private final String tableStyle;
    private final String tableTagName;
    private final String rowStyle;
    private final String rowTagName;

    public FeedsHtml(
            ToHtml<Feed> listItemHtml, 
            String tableStyle, String rowStyle,
            String tableTagName, String rowTagName) {
        this.listItemHtml = listItemHtml;
        this.tableStyle = tableStyle;
        this.rowStyle = rowStyle;
        this.tableTagName = tableTagName;
        this.rowTagName = rowTagName;
        
    }
    
    @Override
    public String toHtml(List<Feed> feedList) {
        StringBuilder appendTo = new StringBuilder();
        this.appendHtml(feedList, appendTo);
        return appendTo.toString();
    }

    @Override
    public void appendHtml(List<Feed> feedList, StringBuilder appendTo) {
        this.tagStart(tableTagName, "style", tableStyle, appendTo);
        for(Feed feed:feedList) {
            this.tagStart(rowTagName, "style", rowStyle, appendTo);
            this.appendRowContent(feed, appendTo);
            this.tagEnd(rowTagName, appendTo);
        }
        this.tagEnd(tableTagName, appendTo);
    }
    
    protected void appendRowContent(Feed feed, StringBuilder appendTo) {
        this.listItemHtml.appendHtml(feed, appendTo);
    }
    
    public final ToHtml<Feed> getListItemHtml() {
        return listItemHtml;
    }

    public final String getTableStyle() {
        return tableStyle;
    }

    public final String getTableTagName() {
        return tableTagName;
    }

    public final String getRowStyle() {
        return rowStyle;
    }

    public final String getRowTagName() {
        return rowTagName;
    }
}
