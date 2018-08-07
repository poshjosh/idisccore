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

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 5:03:06 PM
 */
public interface FeedCreator<T> {
    
    default Feed createFeed(T sourceData) {
        final Feed feed = new Feed();
        this.updateFeed(feed, sourceData);
        return feed;
    }
    
    void updateFeed(Feed feed, T sourceData);
}
