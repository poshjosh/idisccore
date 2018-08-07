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

package com.idisc.core.extraction.rss;

import com.bc.jpa.context.JpaContext;
import java.util.logging.Logger;
import com.idisc.core.FeedHandler;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.core.extraction.FeedCreationContext;
import com.idisc.core.extraction.FeedCreatorFromContext;
import com.idisc.core.extraction.ScrapSiteTaskImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;
import javax.persistence.EntityManager;
import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.context.ExtractionConfig;
import com.bc.webdatex.context.ExtractionContextFactory;
import com.idisc.core.extraction.ScrapContext;
import com.idisc.core.extraction.TaskLifeCycleListenerImpl;
import com.idisc.core.extraction.scrapconfig.ScrapConfig;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactory;
import com.bc.timespent.TimeSpent;
import com.idisc.core.timespent.TimeSpentListenerImpl;
import java.util.function.Consumer;
import com.idisc.core.extraction.ScrapSiteTask;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 30, 2017 7:46:17 PM
 */
public class RssFeedTaskProvider implements Function<String, ScrapSiteTask<SyndEntry, Integer>> {
    
    private transient static final Logger LOG = Logger.getLogger(RssFeedTaskProvider.class.getName());
    
    private final JpaContext jpaContext;
  
    private final ExtractionContextFactory extractionContextFactory;
    
    private final ScrapConfigFactory scrapConfigFactory;
  
    private final Properties feedProperties;

    public RssFeedTaskProvider(
            JpaContext jpaContext, ExtractionContextFactory contextFactory,
            ScrapConfigFactory scrapConfigFactory, Properties feedProperties) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.extractionContextFactory = Objects.requireNonNull(contextFactory);
        this.feedProperties = Objects.requireNonNull(feedProperties);
        this.scrapConfigFactory = Objects.requireNonNull(scrapConfigFactory);
    }
  
    @Override
    public ScrapSiteTask<SyndEntry, Integer> apply(final String feedName) {
    
        LOG.entering(this.getClass().getName(), "createNewTask(String)", feedName);

        final String url = this.feedProperties.getProperty(feedName);
        
        final Iterator<SyndEntry> iter = new RssDataSupplier().apply(url).iterator();
        
        final EntityManager entityManager = jpaContext.getEntityManager();
        
        final ScrapConfig scrapConfig = this.scrapConfigFactory.get(ScrapConfig.TYPE_RSS, feedName);
        
        final Predicate<SyndEntry> scrapTest = scrapConfig.isAcceptDuplicateLinks() ? (entry) -> true : new RssScrapTest(entityManager);
        
        final ExtractionContext extractionContext = this.extractionContextFactory.getContext(feedName);
        final ExtractionConfig nodeExtractorConfig = extractionContext.getExtractionConfig();
        final FeedCreationContext feedCreationContext = FeedCreationContext.builder()
                .with(jpaContext, ScrapContext.TYPE_RSS, nodeExtractorConfig)
                .imagesFilter(url)
                .build();
        
        final FeedCreatorFromContext<SyndEntry> feedCreator = 
                new RssFeedCreator(extractionContext, feedCreationContext);

        final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);
        
        final ScrapSiteTask<SyndEntry, Integer> task = new ScrapSiteTaskImpl<>(
                iter, scrapTest, feedCreator, feedHandler, scrapConfig
        );
        
        task.addLifeCycleListener(new TaskLifeCycleListenerImpl((arg) -> {}, (arg) -> entityManager.close()));
        
        final Consumer<TimeSpent> timeSpentConsumer =
                (timeSpent) -> scrapConfigFactory.updateTimeSpent(ScrapConfig.TYPE_RSS, feedName, timeSpent);
        
        task.addLifeCycleListener(new TimeSpentListenerImpl(timeSpentConsumer));

        return task;
    }
}
