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

package com.idisc.core.extraction.web;

import org.htmlparser.dom.HtmlDocument;
import com.bc.jpa.context.JpaContext;
import com.bc.json.config.JsonConfig;
import com.bc.net.RetryConnectionFilter;
import com.bc.nodelocator.ConfigName;
import com.bc.webcrawler.ConnectionProvider;
import com.bc.webcrawler.CrawlerContext;
import com.bc.webcrawler.ResumeHandler;
import com.bc.webcrawler.UrlParser;
import com.bc.webcrawler.predicates.HtmlLinkIsToBeCrawledTest;
import com.idisc.core.extraction.FeedDownloadResumeHandler;
import com.idisc.core.extraction.LinkExtractor;
import com.idisc.core.extraction.UrlParserImpl;
import com.idisc.pu.entities.Feed;
import com.idisc.core.FeedHandler;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.core.extraction.ConnectionProviderImpl;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import com.idisc.core.extraction.ScrapSiteTaskImpl;
import com.bc.webdatex.context.ExtractionContext;
import com.idisc.core.extraction.FeedCreationContext;
import com.idisc.core.extraction.TaskLifeCycleListenerImpl;
import com.bc.webdatex.context.ExtractionContextFactory;
import com.idisc.core.extraction.ScrapContext;
import com.idisc.core.extraction.scrapconfig.ScrapConfig;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactory;
import com.bc.timespent.TimeSpent;
import com.idisc.core.timespent.TimeSpentListenerImpl;
import java.util.function.Consumer;
import com.idisc.core.extraction.ScrapSiteTask;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 6:26:07 PM
 */
public class WebFeedTaskProvider implements Function<String, ScrapSiteTask<HtmlDocument, Integer>> {

    private static final Logger LOG = Logger.getLogger(WebFeedTaskProvider.class.getName());
    
    private final JpaContext jpaContext;

    private final ExtractionContextFactory extractionContextFactory;
    
    private final ScrapConfigFactory scrapConfigFactory;
    
    public WebFeedTaskProvider(
            JpaContext jpaContext, 
            ExtractionContextFactory contextFactory,
            ScrapConfigFactory scrapConfigFactory) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.extractionContextFactory = Objects.requireNonNull(contextFactory);
        this.scrapConfigFactory = Objects.requireNonNull(scrapConfigFactory);
    }
  
    @Override
    public ScrapSiteTask<HtmlDocument, Integer> apply(final String siteName) {
    
        final ExtractionContext extractionContext = this.extractionContextFactory.getContext(siteName);

        final EntityManager entityManager = this.jpaContext.getEntityManager(Feed.class);
        
        final ScrapConfig scrapConfig = this.scrapConfigFactory.get(ScrapConfig.TYPE_WEB, siteName);

        final ResumeHandler resumeHandler = scrapConfig.isAcceptDuplicateLinks() ? 
                ResumeHandler.NO_OP : new FeedDownloadResumeHandler(entityManager);

        final JsonConfig jsonConfig = extractionContext.getConfig();

        final String startUrl = this.getStartUrl(jsonConfig);

        final CrawlerContext<HtmlDocument> crawlerContext =
                this.createCrawlerContext(extractionContext, resumeHandler, scrapConfig);
        
        final com.bc.webcrawler.Crawler<HtmlDocument> crawler = 
                crawlerContext.newCrawler(Collections.singleton(startUrl));    

        LOG.fine(() -> MessageFormat.format("Created task {0} for {1}", 
            crawler.getClass().getName(), siteName));
        
        final Predicate<String> notStartUrl = (link) -> !link.equals(startUrl);
        final Predicate<HtmlDocument> documentTest = (doc) ->
                notStartUrl.and(extractionContext.getScrappUrlFilter()).test(doc.getURL());
        
        final FeedCreationContext creationContext = FeedCreationContext.builder()
                .with(jpaContext, ScrapContext.TYPE_WEB, extractionContext.getExtractionConfig())
                .build();

        final WebFeedCreator feedCreator = new WebFeedCreator(
                extractionContext, creationContext, scrapConfig.getTolerance());
        
        final FeedHandler feedHandler = this.createFeedHandler(jpaContext, extractionContext);
        
        final ScrapSiteTask<HtmlDocument, Integer> task = new ScrapSiteTaskImpl<>(
                crawler, documentTest, feedCreator, feedHandler, scrapConfig);
        
        final Consumer<TimeSpent> timeSpentConsumer =
                (timeSpent) -> scrapConfigFactory.updateTimeSpent(ScrapConfig.TYPE_WEB, siteName, timeSpent);
        
        task.addLifeCycleListener(new TimeSpentListenerImpl(timeSpentConsumer));

        task.addLifeCycleListener(new TaskLifeCycleListenerImpl((arg) -> {}, (arg) -> {
            entityManager.close();
            crawler.shutdown();
        }));
        
        return task;
    }

    public CrawlerContext<HtmlDocument> createCrawlerContext(
            ExtractionContext capturerContext, ResumeHandler resumeHandler, ScrapConfig scrapConfig) {
        
        final long timeoutPerSiteMillis = this.getTimeoutPerSiteMillis(scrapConfig, 0L);
        final int unit = (int)(timeoutPerSiteMillis / 3);
        final int connectTimeoutMillis = unit;
        final int readTimeoutMillis = (int)timeoutPerSiteMillis - connectTimeoutMillis;
        
        final JsonConfig jsonConfig = capturerContext.getConfig();
        
        final String name = jsonConfig.getName();
        
        Objects.requireNonNull(capturerContext, ExtractionContext.class.getSimpleName() + 
                " for site: "+name+" is null");

        final UrlParser<HtmlDocument> urlParser = new UrlParserImpl(
                connectTimeoutMillis, readTimeoutMillis, false);
        
        final ConnectionProvider connProvider = new ConnectionProviderImpl(
                () -> urlParser.getCookieList()
        );

        final Predicate<String> crawlHtmlLinks = new HtmlLinkIsToBeCrawledTest(
                connProvider, urlParser, 5_000, 10_000, true);

        final Predicate<String> crawlUrlTest = capturerContext.getCaptureUrlFilter().and(crawlHtmlLinks);
        
        final CrawlerContext<HtmlDocument> crawlerContext = CrawlerContext.builder(HtmlDocument.class)
                .batchInterval(5_000)
                .batchSize(5)
                .crawlLimit(scrapConfig.getCrawlLimit())
                .crawlUrlTest(crawlUrlTest)
                .linksExtractor(new LinkExtractor())
                .maxFailsAllowed(scrapConfig.getMaxFailsAllowed())
                .pageIsNoIndexTest((doc) -> doc.isRobotsMetaTagContentContaining("noindex"))
                .pageIsNoFollowTest((doc) -> doc.isRobotsMetaTagContentContaining("nofollow"))
                .parseLimit(scrapConfig.getParseLimit())
                .parseUrlTest((link) -> true)
                .preferredLinkTest(capturerContext.getScrappUrlFilter())
                .resumeHandler(resumeHandler)
                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000L))
                .timeoutMillis(timeoutPerSiteMillis)
                .urlFormatter(capturerContext.getUrlFormatter())
                .urlParser(urlParser)
                .build();

        return crawlerContext;
    }
    
    public FeedHandler createFeedHandler(JpaContext jpaContext, ExtractionContext capturerContext) {
        return new InsertFeedToDatabase(jpaContext);
    }
    
    private long getTimeoutPerSiteMillis(ScrapConfig scrapConfig, long outputIfNone) {
        return scrapConfig.getTimeoutPerSite() < 1 || scrapConfig.getTimeUnit() == null ? 
                outputIfNone : scrapConfig.getTimeUnit().toMillis(scrapConfig.getTimeoutPerSite());
    }
    
    private String getStartUrl(JsonConfig jsonConfig) {
        String startUrl = jsonConfig.getString(ConfigName.url, "start");
        if(startUrl == null || startUrl.trim().isEmpty()) {
            startUrl = jsonConfig.getString(ConfigName.url, "value");
        }
        Objects.requireNonNull(startUrl);
        return startUrl;
    }

    public JpaContext getJpaContext() {
        return jpaContext;
    }

    public ExtractionContextFactory getExtractionContextFactory() {
        return extractionContextFactory;
    }
 }
