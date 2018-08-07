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

package com.idisc.core.extraction;

import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import com.bc.webdatex.extractors.Extractor;
import com.bc.webdatex.extractors.TextParser;
import com.bc.webdatex.extractors.TitleFromUrlExtractor;
import com.bc.webdatex.extractors.date.DateExtractor;
import com.bc.webdatex.extractors.date.DateFromUrlExtractor;
import com.bc.webdatex.extractors.date.DateStringFromUrlExtractor;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.extraction.web.DefaultImageSizeComparator;
import com.idisc.core.functions.GetPlainText;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.context.ExtractionConfig;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Sitetype;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 12:38:37 AM
 */
public class FeedCreationContextBuilderImpl 
        implements FeedCreationContextBuilder, FeedCreationContext, Serializable {

    private transient static final Logger LOG = Logger.getLogger(FeedCreationContextBuilderImpl.class.getName());

    private final float allowForMultiByteChars = 0.01f;

    private NodeFilter imagesFilter;
    
    private FeedCreationConfig feedCreationConfig;

    private BiFunction<String, String, String> plainTextExtractor;

    private TextParser<String> titleFromUrlExtractor;

    private String [] columnNames;

    private int [] columnDisplaySizes;

    private Comparator<String> imageSizeComparator;
  
    private Extractor<String, Date> dateExtractor;
  
    private Extractor<String, Date> dateFromUrlExtractor;
    
    private String [] datePatterns;
    
    private String [] urlDatePatterns;
    
    ////////////////////////////////////////////////
    //
    //  FeedCreationContextBuilder interface methods
    //
    ////////////////////////////////////////////////
    
    private final AtomicBoolean buildAttempted = new AtomicBoolean(false);

    @Override
    public FeedCreationContext build() {
        
        if(this.buildAttempted.getAndSet(true)) {
            throw new IllegalStateException("build() method may only be called once!");
        }
        
        if(this.plainTextExtractor == null) {
            this.plainTextExtractor(new GetPlainText());
        }
        if(this.imageSizeComparator == null) {
            this.imageSizeComparator(new DefaultImageSizeComparator());
        }
        if(this.titleFromUrlExtractor == null) {
            this.titleFromUrlExtractor(new TitleFromUrlExtractor());
        }
        if(this.dateExtractor == null) {
            this.dateExtractor = this.createDateExtractor(feedCreationConfig, datePatterns);
        }
        if(this.dateFromUrlExtractor == null) {
            this.dateFromUrlExtractor = this.createDateFromUrlExtractor(feedCreationConfig, urlDatePatterns);
        }
        Objects.requireNonNull(feedCreationConfig);
        Objects.requireNonNull(plainTextExtractor);
        Objects.requireNonNull(imagesFilter);
        Objects.requireNonNull(titleFromUrlExtractor);
        Objects.requireNonNull(columnNames);
        Objects.requireNonNull(columnDisplaySizes);
        Objects.requireNonNull(dateExtractor);
        Objects.requireNonNull(dateFromUrlExtractor);
        Objects.requireNonNull(imageSizeComparator);
        return this;
    }
    
    @Override
    public FeedCreationContextBuilder with(
            PersistenceUnitContext puContext, Sitetype siteType, ExtractionConfig config) {
        final String siteName = config.getConfig().getName();
        final Site site = new SiteDao(puContext).from(siteName, siteType, true);
        this.with(site, config);
        if(this.columnNames == null && this.columnDisplaySizes == null) {
            this.persistenceMetaData(puContext.getMetaData());
        }
        return this;
    }

    @Override
    public FeedCreationContextBuilder with(
            PersistenceUnitContext puContext, String siteType, ExtractionConfig config) {
        final String siteName = config.getConfig().getName();
        final Site site = new SiteDao(puContext).from(siteName, siteType, true);
        this.with(site, config);
        if(this.columnNames == null && this.columnDisplaySizes == null) {
            this.persistenceMetaData(puContext.getMetaData());
        }
        return this;
    }

    @Override
    public FeedCreationContextBuilder with(Site site, ExtractionConfig config) {
        if(this.imagesFilter == null) {
            this.imagesFilter(config);
        }
        if(this.feedCreationConfig == null) {
            this.feedCreationConfig(site, config);
        }
        if(this.datePatterns == null) {
            this.datePatterns = config.getDatePatterns();
        }
        if(this.urlDatePatterns == null) {
            this.urlDatePatterns = config.getUrlDatePatterns();
        }
        return this;
    }

    @Override
    public FeedCreationContextBuilder persistenceMetaData(PersistenceUnitMetaData metaData) {
        this.columnNames(metaData.getColumnNames(Feed.class));
        this.columnDisplaySizes(metaData.getColumnDisplaySizes(Feed.class));
        return this;
    }

    @Override
    public FeedCreationContextBuilder feedCreationConfig(Site site, ExtractionConfig config) {
        this.feedCreationConfig(new FeedCreationConfigImpl(site, config.getDefaults()));
        return this;
    }

    @Override
    public FeedCreationContextBuilder feedCreationConfig(FeedCreationConfig feedCreationConfig) {
        this.feedCreationConfig = feedCreationConfig;
        return this;
    }

    @Override
    public FeedCreationContextBuilder imagesFilter(String url) {
        final String baseUrl = com.bc.util.Util.getBaseURL(url);
        this.imagesFilter(baseUrl == null || baseUrl.isEmpty() ?
                (node) -> false : new ImageNodeFilter(baseUrl));
        return this;
    }

    @Override
    public FeedCreationContextBuilder imagesFilter(ExtractionConfig config) {
        this.imagesFilter(new ImageNodeFilterImpl(config.getConfig()));
        return this;
    }

    @Override
    public FeedCreationContextBuilder imagesFilter(NodeFilter imagesFilter) {
        this.imagesFilter = imagesFilter;
        return this;
    }

    @Override
    public FeedCreationContextBuilder plainTextExtractor(BiFunction<String, String, String> extractor) {
        this.plainTextExtractor = extractor;
        return this;
    }

    @Override
    public FeedCreationContextBuilder titleFromUrlExtractor(TextParser<String> titleFromUrlExtractor) {
        this.titleFromUrlExtractor = titleFromUrlExtractor;
        return this;
    }

    @Override
    public FeedCreationContextBuilder columnNames(String[] columnNames) {
        this.columnNames = columnNames;
        return this;
    }

    @Override
    public FeedCreationContextBuilder columnDisplaySizes(int[] columnDisplaySizes) {
        this.columnDisplaySizes = columnDisplaySizes;
        return this;
    }

    @Override
    public FeedCreationContextBuilder imageSizeComparator(Comparator<String> imageSizeComparator) {
        this.imageSizeComparator = imageSizeComparator;
        return this;
    }

    @Override
    public FeedCreationContextBuilder dateExtractor(Extractor<String, Date> dateExtractor) {
        this.dateExtractor = dateExtractor;
        return this;
    }

    @Override
    public FeedCreationContextBuilder dateFromUrlExtractor(Extractor<String, Date> dateFromUrlExtractor) {
        this.dateFromUrlExtractor = dateFromUrlExtractor;
        return this;
    }

    @Override
    public FeedCreationContextBuilder datePatterns(String[] datePatterns) {
        this.datePatterns = datePatterns;
        return this;
    }

    @Override
    public FeedCreationContextBuilder urlDatePatterns(String[] urlDatePatterns) {
        this.urlDatePatterns = urlDatePatterns;
        return this;
    }

    /////////////////////////////////////////
    //
    //  FeedCreationContext interface methods
    //
    /////////////////////////////////////////

    @Override
    public boolean hasEnoughData(Feed feed) {
        final boolean hasEnough = (feed.getFeeddate() != null || feed.getDatecreated() != null || feed.getTimemodified() != null) &&
                this.isAnyNotNullOrEmpty(feed.getDescription(), feed.getContent(), feed.getTitle());
        final Level LEVEL = hasEnough ? Level.FINEST : Level.FINE;
        LOG.log(LEVEL, () -> "Feed has enough data: " + hasEnough + ", feed: " + this.toString(feed));
        return hasEnough;
    }
    
    @Override
    public String getValue(String col, boolean plainTextOnly, String... values) {
        
        String val = null;
        
        if(values != null) {
            for(String option : values) {
                if(!this.isNullOrEmpty(option)) {
                    val = option;
                    break;
                }
            }
        }
        
        if(!this.isNullOrEmpty(val)) {
            
            val = format(col, val, plainTextOnly);
            
        }else{

            val = (String)this.feedCreationConfig.getDefaultValue(col);
        }
        
        return val;
    }

    @Override
    public Date getDate(String dateStr, Date outputIfNone) {
        
        final String input = dateStr;

        if(dateStr != null) {
            dateStr = dateStr.trim();
            dateStr = this.plainTextExtractor.apply(dateStr, dateStr);
        }

        Date feeddate = null;

        if(dateStr != null && !dateStr.isEmpty()) {
            feeddate = dateExtractor.extract(dateStr, outputIfNone);
        }
        
        final Date output = feeddate == null ? outputIfNone : feeddate;
        
        LOG.fine(() -> "Input: " + input + ", output: " + output);
        
        return output;
    }

    @Override
    public int getRecommendedSize(String columnName) {
        final int displaySize = getColumnDisplaySize(columnName);  
        return (int)(displaySize - (displaySize * this.allowForMultiByteChars));
    }

    public int getColumnDisplaySize(String columnName) {
        final int displaySize = this.columnDisplaySizes[getColumnIndex(columnName)];
        return displaySize;
    }  
    
    public int getColumnIndex(String columnName) {
        int columnIndex = -1;
        for(int i=0; i<this.columnNames.length; i++) {
            if(Objects.equals(this.columnNames[i], columnName)) {
                columnIndex = i;
                break;
            }
        }
        return columnIndex;
    }
  
    @Override
    public String format(String col, String val, boolean plainTextOnly) {
        
        int maxLen = this.getRecommendedSize(col);
        
        return format(val, null, maxLen, plainTextOnly);
    }
    
    
    public String format(String col, String val, String defaultValue, boolean plainTextOnly) {
        
        int maxLen = this.getRecommendedSize(col);
        
        return format(val, defaultValue, maxLen, plainTextOnly);
    }

    public String format(String val, int maxLen, boolean plainTextOnly) {
        
        return this.format(val, null, maxLen, plainTextOnly);
    }
    
    public String format(
            String val, String defaultValue, int maxLen, boolean plainTextOnly) {
        if(val == null) {
            val = defaultValue;
        }
        if(val != null && val.length() > 0 && plainTextOnly) {
            val = this.plainTextExtractor.apply(val, val);
        }
        
        if(val != null && val.length() > 0) {
            val = com.bc.util.Util.removeNonBasicMultilingualPlaneChars(val);
        }
        
        if(val != null && val.length() > 0) {
            val = com.bc.util.StringEscapeUtils.unescapeHtml(val);
        }
        
        return truncate(val, maxLen);
    }

    public String truncate(String s, int maxLen) {
        if ((s != null) && (maxLen > -1) && (s.length() > maxLen)) {
            s = s.substring(0, maxLen);
        }
        return s;
    }
  
    public boolean isAnyNotNullOrEmpty(Object... values) {
        for(Object value : values) {
            if(!this.isNullOrEmpty((String)value)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNullOrEmpty(Object oval) {
        return oval == null || oval.toString().isEmpty();
    }

    public boolean isNullOrEmpty(String sval) {
        return sval == null || sval.isEmpty();
    }

    @Override
    public Date getDate(Feed feed) {
        return feed.getFeeddate() != null ? feed.getFeeddate() :
                feed.getTimemodified() != null ? feed.getTimemodified() :
                feed.getDatecreated() != null ? feed.getDatecreated() : null;
                
    }

    @Override
    public FeedCreationConfig getConfig() {
        return feedCreationConfig;
    }

    @Override
    public final TextParser<String> getTitleFromUrlExtractor() {
        return titleFromUrlExtractor;
    }

    @Override
    public final NodeFilter getImagesFilter() {
        return this.imagesFilter;
    }

    @Override
    public Comparator<String> getImageSizeComparator() {
        return imageSizeComparator;
    }

    @Override
    public Extractor<String, Date> getDateExtractor() {
        return dateExtractor;
    }

    @Override
    public Extractor<String, Date> getDateFromUrlExtractor() {
        return dateFromUrlExtractor;
    }

    @Override
    public String toString(Feed feed) {
        return MessageFormat.format(
            "Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: {4}", 
            feed.getSiteid() == null ? null : feed.getSiteid().getSite(), 
            feed.getAuthor(), feed.getTitle(), feed.getUrl(), feed.getImageurl());
    }

    public Extractor<String, Date> createDateExtractor(
            FeedCreationConfig config, String[] datePatterns) {
        final Extractor<String, Date> output;
        if(datePatterns == null || datePatterns.length == 0) {
            output = DateExtractor.NO_INSTANCE;
        }else{
            output = new DateExtractor(
                    Arrays.asList(datePatterns), 
                    config.getInputTimeZone(), 
                    config.getOutputTimeZone()
            );
        }
        return output;
    }

    public Extractor<String, Date> createDateFromUrlExtractor(
            FeedCreationConfig config, String[] urlDatePatterns) {
        final Extractor<String, Date> output;
        if(urlDatePatterns == null || urlDatePatterns.length == 0) {
            output = DateFromUrlExtractor.NO_INSTANCE;
        }else{
            final Extractor<String, Date> dateFromTextExtractor = new DateExtractor(
                    Arrays.asList(urlDatePatterns), 
                    config.getInputTimeZone(), 
                    config.getOutputTimeZone()
            );
            output = new DateFromUrlExtractor(
                    new DateStringFromUrlExtractor(), dateFromTextExtractor
            );
        }
        return output;
    }
}
