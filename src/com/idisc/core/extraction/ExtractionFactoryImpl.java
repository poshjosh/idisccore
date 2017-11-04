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
import com.idisc.core.IdiscApp;
import com.idisc.core.comparator.site.SitenameComparatorLastFeeddate;
import com.idisc.core.extraction.ExtractionContext;
import com.idisc.core.extraction.ExtractionFactory;
import com.idisc.core.extraction.rss.RssMgr;
import com.scrapper.config.JsonConfigFactory;
import com.scrapper.config.ScrapperConfigFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 11:23:46 AM
 */
public class ExtractionFactoryImpl implements ExtractionFactory {

    private static final Logger logger = Logger.getLogger(ExtractionFactoryImpl.class.getName());

    private final Map<String, ExtractionContext> contexts;
    
    private final IdiscApp app;

    public ExtractionFactoryImpl(IdiscApp app) {
        this.app = Objects.requireNonNull(app);
        this.contexts = new HashMap<>();
    }
    
    @Override
    public ExtractionContext getExtractionContext(String key) {
        ExtractionContext context = this.contexts.get(key);
        if(context == null) {
            context = this.createExtractionContext(key);
            contexts.put(key, context);
        }
        return context;
    }
    
    public ExtractionContext createExtractionContext(String key) {
        
        logger.finer(() -> "Creating extraction context for: " + key);
        
        final JpaContext jpa = app.getJpaContext();
        
        switch(key) {
            
            case "web":
                final ScrapperConfigFactory configFactory = app.getCapturerApp().getConfigFactory();
                return new ExtractionContextForWebPages(
                        this.getNames(key), jpa, configFactory, app.getConfiguration());
            
            case "rss":
                return new ExtractionContextForRss(
                        this.getNames(key), jpa, app.getConfiguration());

            default: 
                throw new IllegalArgumentException(this.getUnexpectedExtractionContextTypeMesssage(key));
        }
    }
    
    @Override
    public Collection<String> getNames(String key) {
        final List<String> sitenames;
        switch(key) {
            
            case "web":
                final JsonConfigFactory configFactory = app.getCapturerApp().getConfigFactory();
                sitenames = new ArrayList(
                        configFactory.getConfigNamesLessDefaultConfig()
                );
                break;
            
            case "rss":
                sitenames = new ArrayList(
                        new RssMgr().getFeedNamesProperties().stringPropertyNames()
                );
                break;

            default: 
                throw new IllegalArgumentException(this.getUnexpectedExtractionContextTypeMesssage(key));
        }
        
        Collections.sort(sitenames, this.getNamesComparator(key));
        
        return sitenames;
    }

    @Override
    public Comparator<String> getNamesComparator(String key) {
        return new SitenameComparatorLastFeeddate(app.getJpaContext());
    }
    
    private String getUnexpectedExtractionContextTypeMesssage(String key) {
        return "Unexpected " + ExtractionContext.class.getSimpleName() + "type" + key;
    }
}
