package com.idisc.core;

import com.idisc.pu.entities.Feed;
import java.util.Comparator;

/**
 * @author Josh
 */
public class FeedComparatorRelatedContents implements Comparator<Feed> {
    
    private final Feed feed;
    
    public FeedComparatorRelatedContents(Feed feed) {
        this.feed = feed;
    }

    @Override
    public int compare(Feed o1, Feed o2) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
