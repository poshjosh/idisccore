package com.idisc.core.html;

/**
 * @author Josh
 */
public class FeedListTableHtml extends FeedsHtml {
    
    public static class FeedListTableRowContentsHtml extends FeedListItemHtml {

        public FeedListTableRowContentsHtml(String baseUrl, String contextPath, String iconRelativePath) {
            this(baseUrl, contextPath, iconRelativePath, null);
        }

        public FeedListTableRowContentsHtml(String baseUrl, String contextPath, String iconRelativePath, String[] cellStyles) {
            super(baseUrl, contextPath, iconRelativePath, new String[]{"td", "td"}, cellStyles);
        }

        public FeedListTableRowContentsHtml(
                String baseUrl, String contextPath, String iconRelativePath, 
                int maxContentLength, int iconWidthPixels, int iconHeightPixels) {
            this(baseUrl, contextPath, iconRelativePath, maxContentLength, iconWidthPixels, iconHeightPixels, null);
        }

        public FeedListTableRowContentsHtml(
                String baseUrl, String contextPath, String iconRelativePath, 
                int maxContentLength, int iconWidthPixels, int iconHeightPixels, String[] cellStyles) {
            super(baseUrl, contextPath, iconRelativePath, maxContentLength, 
                    iconWidthPixels, iconHeightPixels, new String[]{"td", "td"}, cellStyles);
        }
    }
    
    public FeedListTableHtml(
            String baseUrl, String contextPath, String iconRelativePath) {
        this(
                new FeedListTableRowContentsHtml(
                        baseUrl, contextPath, iconRelativePath
                )
        );
    }
    
    public FeedListTableHtml(
            String baseUrl, String contextPath, String iconRelativePath, 
            int maxContentLength, int iconWidthPixels, int iconHeightPixels, 
            String tableStyle, String rowStyle, String [] cellStyles) {
        this(
                new FeedListTableRowContentsHtml(
                        baseUrl, contextPath, iconRelativePath, maxContentLength, 
                        iconWidthPixels, iconHeightPixels, cellStyles
                ),
                tableStyle, rowStyle
        );
    }

    public FeedListTableHtml(FeedListTableRowContentsHtml listItemHtml) {
        this(listItemHtml, "font-size:1.5em; background:#eeeeee", "vertical-align:top; margin:0.5em; padding:0.5em; background:#ffffff");
    }
    
    public FeedListTableHtml(FeedListTableRowContentsHtml listItemHtml, String tableStyle, String rowStyle) {
        super(listItemHtml, tableStyle, rowStyle, "table", "tr");
    }
}
