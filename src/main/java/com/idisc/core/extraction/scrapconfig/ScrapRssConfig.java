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
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 1:56:03 PM
 */
public class ScrapRssConfig extends AbstractScrapConfig {

    public ScrapRssConfig(Configuration config) {
        super(config);
    }

    @Override
    public long getTimeout(Configuration config) {
        return config.getLong(ConfigNames.RSS_TIMEOUT_PER_TASK_SECONDS, 300);
    }

    @Override
    public long getTimeoutPerSite(Configuration config) {
        return config.getLong(ConfigNames.RSS_TIMEOUT_PER_SITE_SECONDS, 90);
    }

    @Override
    public boolean isAcceptDuplicateLinks(Configuration config) {
        return config.getBoolean(ConfigNames.RSS_ACCEPT_DUPLICATE_LINKS, false);
    }
}
