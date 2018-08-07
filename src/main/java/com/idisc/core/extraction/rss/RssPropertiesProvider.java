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

import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 1, 2018 3:03:00 PM
 */
public class RssPropertiesProvider implements Function<Configuration, Properties> {

    @Override
    public Properties apply(Configuration config) {
        
        Objects.requireNonNull(config);
        
        final Properties output = new Properties();
        
        final Configuration subset = config.subset("nigerian_newsmedia");
        
        final Iterator<String> keys = subset.getKeys();
        
        while (keys.hasNext()) {
        
            final String key = keys.next();
          
            output.setProperty(key, subset.getString(key));
        }
        
        return output;
    }
}
