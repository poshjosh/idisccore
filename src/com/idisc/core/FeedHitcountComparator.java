package com.idisc.core;

import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author Josh
 */
public class FeedHitcountComparator implements Comparator<Feed>, Serializable {
    
    private final boolean reverseOrder;

    public FeedHitcountComparator() {
        this(false);
    }

    public FeedHitcountComparator(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }
    
    @Override
    public int compare(Feed f1, Feed f2) {
        int c1 = sizeOf(f1.getFeedhitList());
        int c2 = sizeOf(f2.getFeedhitList());
        return !reverseOrder ? Integer.compare(c1, c2) : Integer.compare(c2, c1);
    }

    private int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }
    
}
