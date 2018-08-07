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

import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.converters.Converter;
import com.bc.webdatex.converters.DateTimeConverter;
import com.idisc.core.extraction.FeedCreationContext;
import com.idisc.core.extraction.FeedCreatorFromContext;
import com.idisc.core.extraction.web.WebFeedCreator;
import com.idisc.core.functions.FindFirstImageUrl;
import com.idisc.pu.entities.Feed;
import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.dom.HtmlDocument;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 8:44:55 PM
 */
public class RssFeedCreator implements FeedCreatorFromContext<SyndEntry> {
    
    private transient static final Logger LOG = Logger.getLogger(RssFeedCreator.class.getName());

    private final Date NOW = new Date();
    
    private final FeedCreationContext feedCreationContext;
    
    private final StringBuilder contentStrBuilder;
    
    private final StringBuilder catStrBuilder;
    
    private final Parser parser;
    
    private final Converter<Date, Date> dateConverter;
    
    private final BiFunction<List<Node>, Predicate<Node>, String> findFirstImageUrl;
    
    private final FeedCreatorFromContext<HtmlDocument> webFeedCreator;

    public RssFeedCreator(ExtractionContext extractionContext, FeedCreationContext feedCreationContext) {
        this(
                extractionContext,
                feedCreationContext, 
                new DateTimeConverter(
                        TimeZone.getDefault(), feedCreationContext.getConfig().getOutputTimeZone()
                )
        );
    }
    
    public RssFeedCreator(
            ExtractionContext extractionContext,
            FeedCreationContext feedCreationContext,
            Converter<Date, Date> dateConverter) {
        this.feedCreationContext = Objects.requireNonNull(feedCreationContext);
        this.parser = new Parser();
        this.contentStrBuilder = new StringBuilder();
        this.catStrBuilder = new StringBuilder();
        this.dateConverter = Objects.requireNonNull(dateConverter);
        this.findFirstImageUrl = new FindFirstImageUrl();
        this.webFeedCreator = new WebFeedCreator(extractionContext, feedCreationContext, 0.0f);
    }

    @Override
    public void updateFeed(Feed feed, SyndEntry entry) {
        
        final List<SyndContent> syndContentList = entry.getContents();

        contentStrBuilder.setLength(0);

        if ((syndContentList != null) && (!syndContentList.isEmpty())) {

            for (SyndContent syndContent : syndContentList) {

                final String contentValue = syndContent.getValue();

                if ((contentValue != null) && (!contentValue.isEmpty())) {

                    contentStrBuilder.append(contentValue);
                    contentStrBuilder.append("<br/><br/>");
                }
            }
        }

        final SyndContent syndDescription = entry.getDescription();

        final String description = syndDescription == null ? null : syndDescription.getValue();

        if (contentStrBuilder.length() == 0) {

            if ((description != null) && (!description.isEmpty())) {

                contentStrBuilder.append(description);

            } else if(entry.getTitle() != null && !entry.getTitle().isEmpty()) {

                contentStrBuilder.append(entry.getTitle());
            }
        }
        
        final List<SyndCategory> syndCategoryList = entry.getCategories();

        catStrBuilder.setLength(0);

        if (syndCategoryList != null) {

            final Iterator<SyndCategory> iter = syndCategoryList.iterator();
          
            while (iter.hasNext()) {

                final SyndCategory syndCat = iter.next();

                final String name = syndCat.getName();
                
                if ((name != null) && (!name.isEmpty())) {
                    
                    catStrBuilder.append(name);
                    
                    if (iter.hasNext()) {
                    
                        catStrBuilder.append(',');
                    }
                }
            }
        }

        feed.setAuthor(feedCreationContext.format("author", entry.getAuthor(), true));

        final String cat = catStrBuilder.length() == 0 ? null : catStrBuilder.toString();
        if(cat != null) {
            feed.setCategories(feedCreationContext.format("categories", cat, true));
        }

        final String content = contentStrBuilder.length() == 0 ? null : contentStrBuilder.toString();
        if(content != null) {
            feed.setContent(feedCreationContext.format("content", content, false));
        }

        feed.setDescription(feedCreationContext.format("description", description, true));

        Date dateCreated = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();
        if(dateCreated != null && dateCreated.after(NOW)) {
            dateCreated = NOW;
        }
        if(dateCreated != null) {
            feed.setFeeddate(dateConverter.convert(dateCreated));
        }
        
        final Date lastModified = entry.getUpdatedDate();
        if(lastModified != null) {
            feed.setTimemodified(dateConverter.convert(lastModified));
        }

        final HtmlDocument contentDom = this.getNodesForHtml(content, null);

        if (contentDom != null && !contentDom.isEmpty()) {

            final String imageurl = this.findFirstImageUrl.apply(
                    contentDom, feedCreationContext.getImagesFilter());
            
            if (imageurl != null) {
                feed.setImageurl(imageurl);
            }
        }

        feed.setSiteid(feedCreationContext.getConfig().getSite());

        feed.setTitle(feedCreationContext.format("title", entry.getTitle(), false));

        feed.setUrl(entry.getLink());
        
        if(!feedCreationContext.hasEnoughData(feed) && entry.getLink() != null) {
            
            final HtmlDocument dom = this.getNodesForLink(entry.getLink(), null);
            
            if(dom != null) {
                
                this.webFeedCreator.updateFeed(feed, dom);
            }
        }
    }

    private HtmlDocument getNodesForLink(String link, HtmlDocument outputIfNone) {
        HtmlDocument output;
        try {
            parser.reset();
            parser.setURL(link);
            output = parser.parse(null);
        } catch (Exception e) {
            output = null;
            if(LOG.isLoggable(Level.WARNING)){
               LOG.log(Level.WARNING, "{0}", e.toString());
            } 
        }
        return output == null ? outputIfNone : output;
    }
    
    private HtmlDocument getNodesForHtml(String html, HtmlDocument outputIfNone) {
        HtmlDocument output;
        try {
            parser.reset();
            parser.setInputHTML(html);
            output = parser.parse(null);
        } catch (Exception e) {
            output = null;
            if(LOG.isLoggable(Level.WARNING)){
               LOG.log(Level.WARNING, "{0}", e.toString());
            } 
        }
        return output == null ? outputIfNone : output;
    }

    @Override
    public FeedCreationContext getContext() {
        return this.feedCreationContext;
    }
}
