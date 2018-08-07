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

package com.idisc.core.extraction.rss;

import com.idisc.core.functions.ExistingValueTest;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import com.rometools.rome.feed.synd.SyndEntry;
import java.util.function.Predicate;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 11:06:39 PM
 */
public class RssScrapTest implements Predicate<SyndEntry>, AutoCloseable {

    private final ExistingValueTest test;
    
    public RssScrapTest(EntityManager entityManager) { 
        test = new ExistingValueTest(entityManager, Feed.class);
    }

    @Override
    public void close() throws Exception {
        test.close();
    }
  
    @Override
    public boolean test(SyndEntry entry) {
        return test.test(Feed_.url.getName(), entry.getLink());
    }
}
