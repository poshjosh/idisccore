/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.core.util.Util;
import com.idisc.pu.entities.Feed;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 22, 2016 4:23:49 PM
 */
public class SpreadBySite {
    
  public SpreadBySite() { }

  public List<Feed> spread(List<Feed> feeds) {
   
    return this.spread(feeds, feeds.size());
  }
  
  public List<Feed> spread(List<Feed> feeds, int outputSize) {
  
    return this.spread(feeds, outputSize, 1);
  }
  
  public List<Feed> spread(List<Feed> feeds, int outputSize, int factor) {

    this.printFirstDateLastDateAndFeedIds(Level.FINE, "BEFORE REARRANGE", feeds);
    
    final int numOfSites = this.countSites(feeds);

    final int max = this.getAveragePerSite(numOfSites, feeds, outputSize, factor);
    
    if (max != -1) {
        
      final List<Feed> moveToEndOfList = new LinkedList();
        
      SiteFrequency siteFreq = new SiteFrequency(numOfSites);
      
      Iterator<Feed> iter = feeds.iterator();
      
      while(iter.hasNext()) {
          
        Feed feed = iter.next();
        
        final int freq = siteFreq.increment(feed);
        
        if(freq > max) {
            
          iter.remove();
          
          moveToEndOfList.add(feed);
          
          XLogger.getInstance().log(Level.FINEST, "Relocating Feed. ID: {0}, Date: {1} to end of list", 
              getClass(), feed.getFeedid(), feed.getFeeddate());
        }
      }
      
      if(!moveToEndOfList.isEmpty()) {
      
        feeds.addAll(moveToEndOfList);
      }
      
      this.printFirstDateLastDateAndFeedIds(Level.FINE, "AFTER REARRANGE", feeds);
    }
    
    List<Feed> output =  this.truncate(feeds, outputSize);
    
    return output;
  }
  
  protected int getAveragePerSite(int numOfSites, List<Feed> feeds, int outputSize, int factor) {

    int output;
    
    final int numOfFeeds = feeds.size();

    if ((outputSize > 0 && outputSize > numOfSites) && (numOfFeeds > numOfSites * factor)) {
        
      final int div = outputSize > 0 ? outputSize : numOfFeeds;
      
      int ave = div / numOfSites;
      if (ave < 1) {
        ave = 1;
      }
      
      output = ave * factor;
      
    }else{
      
      output = -1;
    } 
    
    return output;
  }  
  
  protected int countSites(List<Feed> feeds) {
    Set<Integer> siteids = new HashSet();
    for(Feed feed:feeds) {
      Integer siteid = feed.getSiteid().getSiteid();
      siteids.add(siteid);
    }
    return siteids.size();
  }

  protected List<Feed> truncate(List<Feed> feeds, int limit) {
    return feeds.size() <= limit ? feeds : feeds.subList(0, limit);  
  }
  
  protected int sizeOf(Collection<Feed> feeds) {
    return feeds == null ? 0 : feeds.size();
  }
  
  protected void printFirstDateLastDateAndFeedIds(Level level, String key, List<Feed> feeds) {
    if (XLogger.getInstance().isLoggable(level, this.getClass()) && feeds != null && !feeds.isEmpty()) {
      Feed first = (Feed)feeds.get(0);
      Feed last = (Feed)feeds.get(feeds.size() - 1);
      XLogger.getInstance().log(level, "{0}. First feed, date: {1}. Last feed, date: {2}\n{3}", 
              this.getClass(), key, first.getFeeddate(), last.getFeeddate(), Util.toString(feeds));
    }
  }
}
