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

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 9:49:57 PM
 */
public class RssDataSupplier implements Function<String, List<SyndEntry>> {

    private transient static final Logger LOG = Logger.getLogger(RssDataSupplier.class.getName());

    @Override
    public List<SyndEntry> apply(String url) {
        try{
            return this.build(url);
        }catch(IOException | FeedException e) {
            LOG.log(Level.WARNING, "Exception building feed for url: " + url, e.toString());
            LOG.log(Level.FINE, "Stacktrace for debugging purposes", e);
            return Collections.EMPTY_LIST;
        }
    }
    
    public List<SyndEntry> build(String url) throws IOException, FeedException {
        
        final List<SyndEntry> output;
        
        final SyndFeed syndFeed = new RssMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
            LOG.log(Level.WARNING, "Failed to create SyndFeed for: {0}", url);
            output = Collections.EMPTY_LIST;
        }else{
            LOG.log(Level.FINER, "Successfully created SyndFeed for: {0}", url);
            output = syndFeed.getEntries();
        }
        
        return output == null ? Collections.EMPTY_LIST : output;
    }
}
