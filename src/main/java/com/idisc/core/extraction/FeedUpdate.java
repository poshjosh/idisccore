/*
 * Copyright 2018 NUROX Ltd.
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

package com.idisc.core.extraction;

import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 2:19:54 PM
 */
public class FeedUpdate implements Serializable {
    
    private FeedCreationConfig config;

    public FeedUpdate() {
        this(null);
    }
  
    public FeedUpdate(FeedCreationConfig config) {
        this.config = config;
    }

    public void updateFeed(Feed feed, Integer feedid, String title, String contents) {
        
        final String author = this.getDefaultAuthor();
    
        this.updateFeed(feed, feedid, author, new Date(), title, contents, getDefaultCategories());
    }
  
    public void updateFeed(
            Feed feed, Integer feedid, String author, Date date, 
            String title, String contents, Object category) {
    
        Objects.requireNonNull(feedid);  
        Objects.requireNonNull(contents); 

        final String categories = category == null ? getDefaultCategories() : category.toString();
        feed.setFeedid(feedid);
        feed.setAuthor(author);
        feed.setCategories(categories);
        feed.setContent(contents);
        feed.setDatecreated(date);
        feed.setFeeddate(date);
        feed.setKeywords(categories);

        feed.setSiteid(config.getSite());
        feed.setTitle(title);
    }
    
    public String getDefaultAuthor() {
        return config.getSite() == null ? null : config.getSite().getSite();
    }
    
    public String getDefaultCategories() {
        return config == null ? null : config.getDefaultCategories();
    }
}
