package com.idisc.core.comparator;

import com.idisc.pu.entities.Feed;

/**
 * @author Josh
 */
public class FeedComparatorRelatedContents extends BaseFeedComparator {
    
    private final Feed feed;

    public FeedComparatorRelatedContents(Feed feed) {
        this.feed = feed;
    }

    public FeedComparatorRelatedContents(Feed feed, boolean reverseOrder) {
        super(reverseOrder);
        this.feed = feed;
    }

    @Override
    public int compare(Feed o1, Feed o2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public final Feed getFeed() {
        return feed;
    }
}
