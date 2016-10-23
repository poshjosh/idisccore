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

package com.idisc.core.comparator.feed;

import com.idisc.pu.entities.Feed;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 18, 2016 10:59:40 PM
 */
public class FeedComparatorFeedid extends BaseFeedComparator {

    public FeedComparatorFeedid() { }

    public FeedComparatorFeedid(boolean reverseOrder) {
        super(reverseOrder);
    }

    @Override
    public long getScore(Feed feed) {
        return feed.getFeedid();
    }
}
