package com.idisc.core.comparator;

import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.List;

/**
 * @author Josh
 */
public class BaseFeedComparator extends AbstractComparator<Feed> implements Serializable {
    
    public BaseFeedComparator() { }

    public BaseFeedComparator(boolean reverseOrder) {
        
        super(reverseOrder);
    }
    
    @Override
    public long getScore(Feed feed) {
        
        long score = this.countFeedHits(feed);
        
        String imageUrl = feed.getImageurl();
        
        if(imageUrl != null) {
            
            score = score * 2;
        }
        
        return score;
    }
    
    public final int countFeedHits(Feed feed) {
        
        return sizeOf(feed.getFeedhitList());
    }

    private int sizeOf(List list) {
        
        return list == null ? 0 : list.size();
    }
}
