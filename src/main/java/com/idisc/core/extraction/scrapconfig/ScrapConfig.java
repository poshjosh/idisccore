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

import java.util.concurrent.TimeUnit;
import com.idisc.core.extraction.ScrapContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 1:17:18 PM
 */
public interface ScrapConfig {
    
    String TYPE_WEB = ScrapContext.TYPE_WEB;
    
    String TYPE_RSS = ScrapContext.TYPE_RSS;
    
    int getCrawlLimit();

    int getMaxConcurrentUnits();

    int getMaxFailsAllowed();

    int getParseLimit();

    int getScrapLimit();

    int getSiteLimit();
        
    TimeUnit getTimeUnit();

    long getTimeout();

    long getTimeoutPerSite();

    float getTolerance();

    boolean isAcceptDuplicateLinks();
}
