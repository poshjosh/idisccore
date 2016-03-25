package com.idisc.core;

import com.bc.util.IntegerArray;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.util.List;
import java.util.logging.Level;


/**
 * @(#)Feedo.java   21-Feb-2015 14:24:13
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class FeedFrequency {

    private final IntegerArray siteIds;
    private final IntegerArray siteFrequencies;

    public FeedFrequency() {
        siteIds = new IntegerArray();
        siteFrequencies = new IntegerArray();
    }
    
    public FeedFrequency(List<Feed> feeds) {
        this();
        FeedFrequency.this.setFeeds(feeds);
    }
    
    public void setFeeds(List<Feed> feeds) {
        if(feeds == null) {
            throw new NullPointerException();
        }else
        if(!feeds.isEmpty()) {
            siteIds.clear();
            siteFrequencies.clear();
            for(Feed feed:feeds) {
                this.updateSiteFrequency(feed);
            }
        }
    }
    
    public int getSiteCount() {
        return siteFrequencies == null ? 0 : siteFrequencies.size();
    }
    
    public int getSiteFrequency(Feed feed) {
        Site site = feed.getSiteid();
        if(site == null || siteIds == null || siteFrequencies == null) {
            return -1;
        }
        int pos = siteIds.indexOf(site.getSiteid());
        return siteFrequencies.get(pos);
    }
    
    private int updateSiteFrequency(Feed feed) {
        
        Site site = feed.getSiteid();

        if(site == null) {
XLogger.getInstance().log(Level.WARNING, 
"No site found for Feed:: ID: {0}, title: {1}", 
this.getClass(), feed.getFeedid(), feed.getTitle());
            return -1;
        }

        int siteid = site.getSiteid();

        int index = siteIds.indexOf(siteid);

        int siteFreq;

        if(index == -1) {
            siteFreq = 0;
            siteIds.add(siteid);
            siteFrequencies.add(++siteFreq);
        }else{
            siteFreq = siteFrequencies.get(index);
            siteFrequencies.set(index, ++siteFreq);
        }
        
        return siteFreq;
    }
}
