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

import com.bc.jpa.context.JpaContext;
import org.htmlparser.dom.HtmlDocument;
import com.bc.json.config.JsonConfig;
import com.bc.webcrawler.Crawler;
import com.idisc.core.FeedHandler;
import com.idisc.pu.entities.Feed;
import com.bc.webdatex.context.CapturerContext;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.core.extraction.ImageNodeFilterImpl;
import com.idisc.pu.References;
import com.idisc.pu.entities.Sitetype;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 7:23:36 PM
 */
public class WebFeedTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(WebFeedTask.class.getName());

    private final CapturerContext context;
    
    private final com.bc.webcrawler.Crawler<HtmlDocument> crawler;
    
    private final Predicate<HtmlDocument> scrappDocumentTest;
    
    private final WebFeedCreator feedCreator;
    
    private final FeedHandler feedHandler;

    private int scrappAttempted;
    
    private int scrapped;
    
    private int saved;
    
    public WebFeedTask(CapturerContext context, 
            Crawler<HtmlDocument> crawler, 
            Predicate<HtmlDocument> scrappDocumentTest, 
            JpaContext jpaContext,
            float tolerance) {
        
        this(context, crawler, scrappDocumentTest, 
                new WebFeedCreator(
                        context.getConfig().getName(), 
                        (Sitetype)jpaContext.getEnumReferences().getEntity(References.sitetype.web), 
                        new ImageNodeFilterImpl(context.getConfig()), 
                        tolerance), 
                new InsertFeedToDatabase(jpaContext));
    }
    
    public WebFeedTask(CapturerContext context, 
            Crawler<HtmlDocument> crawler, 
            Predicate<HtmlDocument> scrappDocumentTest, 
            WebFeedCreator feedCreator,
            FeedHandler feedHandler) {
        this.context = Objects.requireNonNull(context);
        this.crawler = Objects.requireNonNull(crawler);
        this.scrappDocumentTest = Objects.requireNonNull(scrappDocumentTest);
        this.feedCreator = Objects.requireNonNull(feedCreator);
        this.feedHandler = Objects.requireNonNull(feedHandler);
        LOG.log(Level.FINE, "Done creating: {0}", WebFeedTask.this);
    }
    
    @Override
    public void run() {
        
        final JsonConfig config = context.getConfig();

        final String siteName = config.getName();
        
        LOG.fine(() -> "Running "+this.getClass().getSimpleName()+" for: " + siteName);
        try{
            
            this.scrappAttempted = 0;
            this.scrapped = 0;
            this.saved = 0;

            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -2);
            
            final Date daysAgo = cal.getTime();

            while(crawler.hasNext()) {

                final HtmlDocument doc = crawler.next();
                
                if(doc == null) {
                    continue;
                }
                
                ++scrappAttempted;

                LOG.log(Level.FINER, "PageNodes: {0}", doc);
                
                final boolean scrapp = scrappDocumentTest.test(doc);
                
                LOG.finest(() -> "Scrapp: " + scrapp + ", doc: " + doc);

                if(!scrapp) {
                    LOG.log(Level.FINER, "Not qualified for scrapp, doc: {0}", doc);
                    continue;
                }
                
                final Feed feed = feedCreator.createFeed(doc);
                
                if(feed == null || !feedCreator.hasEnoughData(feed)) {
                    LOG.fine(() -> "Not enough data from doc: " + doc + 
                            "\nFeed: " + feedCreator.toString(feed));
                    continue;
                }

                final Date date = feedCreator.getDate(feed);
                
                if(date == null) {
                    LOG.fine(() -> "No date for doc: {0}" + doc + 
                            "\nFeed: " + feedCreator.toString(feed));
                    continue;
                }

                ++scrapped;
                
                if(date.before(daysAgo)) {
                    LOG.fine(() -> "Contains expired data, doc: {0}" + doc + 
                            "\nFeed: " + feedCreator.toString(feed));
                    continue;
                }

                synchronized (feedHandler) {

                    LOG.log(Level.FINER, () -> "Inserting into database: " + feedCreator.toString(feed));

                    final boolean insertedIntoDatabase = feedHandler.process(feed);

                    if(insertedIntoDatabase) {
                        ++saved;
                    }else{
                        LOG.fine(() -> "Failed to add Web Feed. " + feedCreator.toString(feed));
                    }
                }
            }
        }catch(RuntimeException e) {
            LOG.log(Level.WARNING, "Unexpected Runtime Exception", e);
        }finally{
            LOG.fine(() -> this.toString());
        }
    }

    public int getScrappAttempted() {
        return scrappAttempted;
    }

    public int getScrapped() {
        return scrapped;
    }

    public int getSaved() {
        return saved;
    }

    @Override
    public String toString() {
        return "WebFeedTask@" + this.hashCode() + 
                "{context=" + context.getConfig().getName() + "\ncrawler=" + crawler + 
                "\nScrapp attempted=" + scrappAttempted + ", scrapped=" + scrapped+ ", inserted into database=" + saved + '}';
    }
}
