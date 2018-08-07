/*
 * Copyright 2017 NUROX Ltd.
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

import com.bc.jpa.context.JpaContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import com.idisc.core.extraction.rss.RssFeedTaskProvider;
import com.idisc.core.extraction.web.WebFeedTaskProvider;
import com.bc.webdatex.context.ExtractionContextFactory;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactory;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 11:23:46 AM
 */
public class ScrapContextFactoryImpl implements ScrapContextFactory {

    private static final Logger LOG = Logger.getLogger(ScrapContextFactoryImpl.class.getName());

    private final Map<String, ScrapContext> contexts;
    
    private final Properties rssProperties;
    
    private final JpaContext jpaContext;
    
    private final ExtractionContextFactory extractionContextFactory;
    
    private final ScrapConfigFactory scrapConfigFactory;

    public ScrapContextFactoryImpl(
            Properties rssProperties,
            JpaContext jpaContext, 
            ExtractionContextFactory extractionContextFactory, 
            ScrapConfigFactory scrapConfigFactory) {
        this.rssProperties = Objects.requireNonNull(rssProperties);
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.extractionContextFactory = Objects.requireNonNull(extractionContextFactory);
        this.scrapConfigFactory = Objects.requireNonNull(scrapConfigFactory);
        this.contexts = new HashMap<>();
    }
    
    @Override
    public ScrapContext apply(String type) {
        ScrapContext context = this.contexts.get(type);
        if(context == null) {
            context = this.createExtractionContext(type);
            contexts.put(type, context);
        }
        return context;
    }
    
    public ScrapContext createExtractionContext(String type) {
        
        LOG.finer(() -> "Creating extraction context for: " + type);
        
        switch(type) {
            
            case ScrapContext.TYPE_WEB:
                return new ScrapContextImpl(
                        new ArrayList(extractionContextFactory.getConfigService().getConfigNamesLessDefaultConfig()), 
                        jpaContext, 
                        scrapConfigFactory.get(type),
                        new WebFeedTaskProvider(jpaContext, extractionContextFactory, scrapConfigFactory)
                );
            
            case ScrapContext.TYPE_RSS:
                
                return new ScrapContextImpl(
                        new ArrayList(this.rssProperties.stringPropertyNames()), 
                        jpaContext, 
                        scrapConfigFactory.get(type),
                        new RssFeedTaskProvider(jpaContext, extractionContextFactory, scrapConfigFactory, this.rssProperties)
                );

            default: 
                throw new IllegalArgumentException("Unexpected type: " + type);
        }
    }
}
