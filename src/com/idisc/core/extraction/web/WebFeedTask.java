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

import com.bc.dom.HtmlDocument;
import com.bc.jpa.context.JpaContext;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.webcrawler.Crawler;
import com.bc.webdatex.filter.ImageNodeFilter;
import com.idisc.core.FeedHandler;
import com.idisc.core.InsertFeedToDatabase;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.context.CapturerContext;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.NodeFilter;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 7:23:36 PM
 */
public class WebFeedTask implements Runnable {

    private static final Logger logger = Logger.getLogger(WebFeedTask.class.getName());

    private final CapturerContext context;
    
    private final com.bc.webcrawler.Crawler<HtmlDocument> crawler;
    
    private final JpaContext jpaContext;
    
    private final Sitetype sitetype;
    
    private final Predicate<HtmlDocument> scrappDocumentTest;
    
    private final float tolerance;

    private int attempted;
    
    private int scrapped;
    
    private int saved;

    public WebFeedTask(CapturerContext context, Crawler<HtmlDocument> crawler, 
            JpaContext jpaContext, Predicate<HtmlDocument> scrappDocumentTest, float tolerance) {
        this.context = Objects.requireNonNull(context);
        this.crawler = Objects.requireNonNull(crawler);
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.scrappDocumentTest = Objects.requireNonNull(scrappDocumentTest);
        this.tolerance = tolerance;
        final EnumReferences refs = jpaContext.getEnumReferences();
        this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
        logger.log(Level.FINE, "Done creating: {0}", WebFeedTask.this);
    }
    
    @Override
    public void run() {
        
        final JsonConfig config = context.getConfig();

        final String siteName = config.getName();
        
        logger.log(Level.FINER, () -> "Running task " + "WebFeedCrawler for " + siteName);

        try{
            
            this.attempted = 0;
            this.scrapped = 0;
            this.saved = 0;

            final NodeFilter imagesFilter = new ImageNodeFilter(
    //                config.getString(new Object[] { "url", "value" }), // Images may not start with baseUrl
                    null,
                    config.getString("imageUrl_requiredRegex"),
                    config.getString("imageUrl_unwantedRegex"));

            final WebFeedCreator feedCreator = new WebFeedCreator(
                    siteName, this.sitetype, imagesFilter, this.tolerance);

            final Date datecreated = new Date();

            final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);

            while(crawler.hasNext()) {

                final HtmlDocument doc = crawler.next();
                
                if(doc == null) {
                    continue;
                }
                
                ++attempted;

                logger.log(Level.FINER, "PageNodes: {0}", doc);
                
                if (scrappDocumentTest.test(doc)){

                    final Feed feed = feedCreator.createFeed(doc, datecreated);

                    if(feed != null) {
                        
                        ++scrapped;
                        
                        synchronized (feedHandler) {

                            logger.log(Level.FINER, () -> "Add Web Feed. " + this.printFeed(feed));

                            final boolean insertedIntoDatabase = feedHandler.process(feed);

                            if(insertedIntoDatabase) {
                                ++saved;
                            }else{
                                logger.fine(() -> "Failed to add Web Feed. " + this.printFeed(feed));
                            }
                        }
                    }
                }
            }
        }catch(RuntimeException e) {
            logger.log(Level.WARNING, "Unexpected Runtime Exception", e);
        }finally{
            logger.fine(() -> MessageFormat.format("Site: {0}, saved: {1} feeds", siteName, this.saved));
        }
    }

    private String printFeed(Feed feed) {
        return MessageFormat.format(
            "Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: {4}", 
            feed.getSiteid() == null ? null : feed.getSiteid().getSite(), 
            feed.getAuthor(), feed.getTitle(), feed.getUrl(), feed.getImageurl());
    }

    public int getAttempted() {
        return attempted;
    }

    public int getScrapped() {
        return scrapped;
    }

    public int getSaved() {
        return saved;
    }

    @Override
    public String toString() {
        return "WebFeedTask@" + Integer.toHexString(hashCode()) + 
                "{context=" + context.getConfig().getName() + "\ncrawler=" + crawler + 
                "\nAttempted=" + attempted + ", scrapped=" + scrapped+ ", inserted into database=" + saved + '}';
    }
}
