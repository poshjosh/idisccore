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
import com.idisc.core.ConfigNames;
import com.idisc.core.IdiscApp;
import com.idisc.core.extraction.FeedDownloadResumeHandler;
import com.idisc.core.extraction.LinkExtractor;
import com.idisc.core.extraction.ScrappDocumentTest;
import com.idisc.core.extraction.UrlParserImpl;
import com.idisc.pu.entities.Feed;
import com.idisc.core.FeedHandler;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.core.extraction.ConnectionProviderImpl;
import com.idisc.core.extraction.ImageNodeFilterImpl;
import com.idisc.pu.References;
import com.idisc.pu.entities.Sitetype;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.context.CapturerContextFactory;
import com.bc.webdatex.context.CapturerContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 6:26:07 PM
 */
public class WebFeedTaskProvider implements Function<String, Runnable> {

    private static final Logger LOG = Logger.getLogger(WebFeedTaskProvider.class.getName());

    private final boolean acceptDuplicateUrls;

    private final long timeout;
    
    private final float tolerance;

    private final TimeUnit timeunit;

    private final int crawlLimit;
    
    private final int parseLimit;

    private final int maxFailsAllowed;
    
    private final JpaContext jpaContext;

    private final CapturerContextFactory configFactory;
    
    public WebFeedTaskProvider(IdiscApp app) {
        this(
                app.getJpaContext(), 
                app.getScrapperContextFactory(),
                app.getConfiguration()
        );
    }

    public WebFeedTaskProvider(
        JpaContext jpaContext, 
        CapturerContextFactory configFactory,
        Configuration config) {
        this(jpaContext, configFactory,
                config.getFloat(ConfigNames.TOLERANCE, 0.0f),
                config.getLong(ConfigNames.WEB_TIMEOUT_PER_SITE_SECONDS, 180),
                TimeUnit.SECONDS,
                config.getInt(ConfigNames.CRAWL_LIMIT, 5000),
                config.getInt(ConfigNames.PARSE_LIMIT, 500),
                config.getInt(ConfigNames.MAX_FAILS_ALLOWED, 9),
                config.getBoolean(ConfigNames.WEB_ACCEPT_DUPLICATE_LINKS, false)
        );
    }
    
    public WebFeedTaskProvider(
            JpaContext jpaContext, CapturerContextFactory configFactory,
            float tolerance, long timeout, TimeUnit timeunit, 
            int crawlLimit, int parseLimit, int maxFailsAllowed, boolean acceptDuplicateUrls) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.configFactory = Objects.requireNonNull(configFactory);
        this.timeout = timeout;
        this.tolerance = tolerance;
        this.timeunit = Objects.requireNonNull(timeunit);
        this.crawlLimit = crawlLimit;
        this.parseLimit = parseLimit;
        this.maxFailsAllowed = maxFailsAllowed;
        this.acceptDuplicateUrls = acceptDuplicateUrls;
    }
  
    @Override
    public Runnable apply(final String siteName) {
    
        final CapturerContext capturerContext = this.configFactory.getContext(siteName);

        final JsonConfig jsonConfig = capturerContext.getConfig();

        Objects.requireNonNull(capturerContext, CapturerContext.class.getSimpleName()+" for site: "+siteName+" is null");

        final UrlParser<HtmlDocument> urlParser = new UrlParserImpl();
        
        final ConnectionProvider connProvider = new ConnectionProviderImpl(
                () -> urlParser.getCookieList()
        );

        final Predicate<String> crawlHtmlLinks = new HtmlLinkIsToBeCrawledTest(
                connProvider, urlParser, 5_000, 10_000, true);

        final Predicate<String> crawlUrlTest = capturerContext.getCaptureUrlFilter().and(crawlHtmlLinks);
        
        final EntityManager entityManager = this.jpaContext.getEntityManager(Feed.class);

        final ResumeHandler resumeHandler = this.acceptDuplicateUrls ? 
                ResumeHandler.NO_OP : new FeedDownloadResumeHandler(entityManager);

        final CrawlerContext<HtmlDocument> crawlerContext = CrawlerContext.builder(HtmlDocument.class)
                .batchInterval(5_000)
                .batchSize(5)
                .crawlLimit(crawlLimit)
                .crawlUrlTest(crawlUrlTest)
                .linksExtractor(new LinkExtractor())
                .maxFailsAllowed(maxFailsAllowed)
                .pageIsNoIndexTest((doc) -> doc.isRobotsMetaTagContentContaining("noindex"))
                .pageIsNoFollowTest((doc) -> doc.isRobotsMetaTagContentContaining("nofollow"))
                .parseLimit(parseLimit)
                .parseUrlTest((link) -> true)
                .preferredLinkTest(capturerContext.getScrappUrlFilter())
                .resumeHandler(resumeHandler)
                .retryOnExceptionTestSupplier(() -> new RetryConnectionFilter(2, 2_000L))
                .timeoutMillis(timeout < 1 || timeunit == null ? 0 : timeunit.toMillis(timeout))
                .urlFormatter(capturerContext.getUrlFormatter())
                .urlParser(urlParser)
                .build();

        final String startUrl = this.getStartUrl(jsonConfig);

        final com.bc.webcrawler.Crawler<HtmlDocument> crawler = 
                crawlerContext.newCrawler(Collections.singleton(startUrl));    

        LOG.fine(() -> MessageFormat.format("Created task {0} for {1}", 
            crawler.getClass().getName(), siteName));

        final Predicate<String> notStartUrl = (link) -> !link.equals(startUrl);
        
        final Predicate<HtmlDocument> documentTest = 
                new ScrappDocumentTest(notStartUrl.and(capturerContext.getScrappUrlFilter()));
        
        final NodeFilter imagesFilter = new ImageNodeFilterImpl(jsonConfig);
        
        final WebFeedCreator feedCreator = new WebFeedCreator(
                siteName, 
                (Sitetype)jpaContext.getEnumReferences().getEntity(References.sitetype.web), 
                imagesFilter, 
                this.tolerance);
        
        final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);
        
        return new WebFeedTask(capturerContext, crawler, documentTest, feedCreator, feedHandler){
            @Override
            public void run() {
                try{
                    super.run();
                }catch(RuntimeException e) {
                    LOG.log(Level.WARNING, "Unexpected Runtime Exception", e);
                }finally{
                    LOG.fine(() -> "Closing EntityManager used by: " + resumeHandler.getClass().getSimpleName());
                    if(entityManager.isOpen()) {
                        entityManager.close();
                    }
                }
            }
        };
    }
    
    private String getStartUrl(JsonConfig jsonConfig) {
        String startUrl = jsonConfig.getString(ConfigName.url, "start");
        if(startUrl == null || startUrl.trim().isEmpty()) {
            startUrl = jsonConfig.getString(ConfigName.url, "value");
        }
        Objects.requireNonNull(startUrl);
        return startUrl;
    }
}
