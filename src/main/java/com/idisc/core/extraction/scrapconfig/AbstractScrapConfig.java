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

package com.idisc.core.extraction.scrapconfig;

import com.idisc.core.ConfigNames;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 1:05:58 PM
 */
public abstract class AbstractScrapConfig extends ScrapConfigBean {

    public AbstractScrapConfig() { }

    public AbstractScrapConfig(Configuration config) {
        
        this.setTimeUnit(TimeUnit.SECONDS);
        
        this.setTimeout(this.getTimeout(config));
        
        this.setTimeoutPerSite(this.getTimeoutPerSite(config));
        
        this.setAcceptDuplicateLinks(this.isAcceptDuplicateLinks(config));
        
        final int availableProcessors = Runtime.getRuntime().availableProcessors();

        final int maxConcurrent = (int)config.getLong(ConfigNames.MAXCONCURRENT, availableProcessors);
        this.setMaxConcurrentUnits(maxConcurrent);
        
        this.setCrawlLimit(config.getInt(ConfigNames.CRAWL_LIMIT, 5000));
        
        this.setParseLimit(config.getInt(ConfigNames.PARSE_LIMIT, 500));
        
        this.setScrapLimit(config.getInt(ConfigNames.SCRAPP_LIMIT, 50));
        
        this.setMaxFailsAllowed(config.getInt(ConfigNames.MAX_FAILS_ALLOWED, 9));
        
        this.setTolerance(config.getFloat(ConfigNames.TOLERANCE, 0.0f));

        final int sitesPerBatch = (int)config.getLong(ConfigNames.SITES_PER_BATCH, availableProcessors);
        this.setSiteLimit(sitesPerBatch);
    }
    
    protected abstract long getTimeout(Configuration config);

    protected abstract long getTimeoutPerSite(Configuration config);

    protected abstract boolean isAcceptDuplicateLinks(Configuration config);
}
