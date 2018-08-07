/*
 * Copyright 2016 NUROX Ltd.
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

package com.idisc.core.extraction.rss.deprecated;

import com.bc.jpa.context.PersistenceUnitContext;
import java.util.logging.Logger;
import com.idisc.pu.entities.Feed;
import java.util.Arrays;
import java.util.Date;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.extractors.date.DateExtractor;
import com.bc.webdatex.extractors.TitleFromUrlExtractor;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import com.bc.webdatex.extractors.Extractor;
import com.bc.webdatex.extractors.TextParser;
import com.bc.webdatex.extractors.date.DateFromUrlExtractor;
import com.bc.webdatex.extractors.date.DateStringFromUrlExtractor;
import com.idisc.core.extraction.FeedCreationConfig;
import com.idisc.core.extraction.FeedCreationConfigImpl;
import com.idisc.core.extraction.FeedCreationContext;
import com.idisc.core.extraction.ImageNodeFilterImpl;
import com.idisc.core.extraction.web.DefaultImageSizeComparator;
import com.idisc.core.functions.GetPlainText;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Sitetype;
import java.text.MessageFormat;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import com.bc.webdatex.context.ExtractionConfig;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 11:05:58 AM
 */
public class FeedCreationContextImpl implements FeedCreationContext {

    private transient static final Logger LOG = Logger.getLogger(FeedCreationContextImpl.class.getName());
    
    private static Extractor<String, Date> createDateExtractor(
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

    private static Extractor<String, Date> createDateFromUrlExtractor(
            FeedCreationConfig config, String[] urlDatePatterns) {
        final Extractor<String, Date> output;
        if(urlDatePatterns == null || urlDatePatterns.length == 0) {
            output = DateStringFromUrlExtractor.NO_INSTANCE;
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

    private final NodeFilter imagesFilter;
    
    private final FeedCreationConfig feedCreationConfig;

    private final BiFunction<String, String, String> plainTextExtractor;

    private final TextParser<String> titleFromUrlExtractor;

    private final float allowForMultiByteChars = 0.01f;

    private final String [] columnNames;

    private final int [] columnDisplaySizes;

    private final Comparator<String> imageSizeComparator;
  
    private final Extractor<String, Date> dateExtractor;
  
    private final Extractor<String, Date> dateFromUrlExtractor;

    public FeedCreationContextImpl(
            PersistenceUnitContext puContext, 
            ExtractionConfig extractionConfig, 
            String siteName, Sitetype siteType){
        this(puContext.getMetaData(),
                extractionConfig,
                new FeedCreationConfigImpl(
                        new SiteDao(puContext).from(siteName, siteType, true), 
                        extractionConfig.getDefaults()
                )
        );
    }
    
    public FeedCreationContextImpl(
            PersistenceUnitMetaData metaData, 
            ExtractionConfig extractionConfig, 
            FeedCreationConfig creationConfig){
        this(metaData, creationConfig, 
                new ImageNodeFilterImpl(extractionConfig.getConfig()),
                createDateExtractor(creationConfig, extractionConfig.getDatePatterns()),
                new TitleFromUrlExtractor(),
                createDateFromUrlExtractor(creationConfig, extractionConfig.getUrlDatePatterns()),
                new DefaultImageSizeComparator());
    }
    
    public FeedCreationContextImpl(
            PersistenceUnitMetaData metaData, 
            ExtractionConfig extractionConfig, 
            FeedCreationConfig creationConfig,
            NodeFilter imagesFilter){
        this(metaData, creationConfig, 
                imagesFilter,
                createDateExtractor(creationConfig, extractionConfig.getDatePatterns()),
                new TitleFromUrlExtractor(),
                createDateFromUrlExtractor(creationConfig, extractionConfig.getUrlDatePatterns()),
                new DefaultImageSizeComparator());
    }

    public FeedCreationContextImpl(
            PersistenceUnitMetaData metaData, 
            FeedCreationConfig feedCreationConfig,
            NodeFilter imagesFilter, 
            Extractor<String, Date> dateExtractor,
            TextParser<String> titleFromUrlExtractor,
            Extractor<String, Date> dateFromUrlExtractor,
            Comparator<String> imageSizeComparator){
        this.feedCreationConfig = Objects.requireNonNull(feedCreationConfig);
        this.plainTextExtractor = new GetPlainText();
        this.imagesFilter = imagesFilter;
        this.titleFromUrlExtractor = Objects.requireNonNull(titleFromUrlExtractor);
        this.columnNames = metaData.getColumnNames(Feed.class);
        this.columnDisplaySizes = metaData.getColumnDisplaySizes(Feed.class);
        this.dateExtractor = Objects.requireNonNull(dateExtractor);
        this.dateFromUrlExtractor = Objects.requireNonNull(dateFromUrlExtractor);
        this.imageSizeComparator = Objects.requireNonNull(imageSizeComparator);
    }

    public FeedCreationContextImpl(
            FeedCreationConfig feedCreationConfig,
            BiFunction<String, String, String> plainTextExtractor,
            NodeFilter imagesFilter, 
            Extractor<String, Date> dateExtractor,
            TextParser<String> titleFromUrlExtractor,
            Extractor<String, Date> dateFromUrlExtractor,
            Comparator<String> imageSizeComparator,
            String [] columnNames,
            int [] columnDisplaySizes){
        this.feedCreationConfig = Objects.requireNonNull(feedCreationConfig);
        this.plainTextExtractor = Objects.requireNonNull(plainTextExtractor);
        this.imagesFilter = Objects.requireNonNull(imagesFilter);
        this.titleFromUrlExtractor = Objects.requireNonNull(titleFromUrlExtractor);
        this.columnNames = Objects.requireNonNull(columnNames);
        this.columnDisplaySizes = Objects.requireNonNull(columnDisplaySizes);
        this.dateExtractor = Objects.requireNonNull(dateExtractor);
        this.dateFromUrlExtractor = Objects.requireNonNull(dateFromUrlExtractor);
        this.imageSizeComparator = Objects.requireNonNull(imageSizeComparator);
    }

    @Override
    public boolean hasEnoughData(Feed feed) {
        return (feed.getFeeddate() != null || feed.getDatecreated() != null || feed.getTimemodified() != null) &&
                this.isAnyNotNullOrEmpty(feed.getDescription(), feed.getContent(), feed.getTitle());
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

        if(dateStr != null) {
            dateStr = dateStr.trim();
            dateStr = this.plainTextExtractor.apply(dateStr, dateStr);
        }

        Date feeddate = null;

        if(dateStr != null && !dateStr.isEmpty()) {
            feeddate = dateExtractor.extract(dateStr, outputIfNone);
        }
        
        return feeddate == null ? outputIfNone : feeddate;
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
    public String toString(Feed feed) {
        return MessageFormat.format(
            "Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: {4}", 
            feed.getSiteid() == null ? null : feed.getSiteid().getSite(), 
            feed.getAuthor(), feed.getTitle(), feed.getUrl(), feed.getImageurl());
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
}
/**
 * 
    public FeedCreationContextImpl(
            Integer siteId, Map defaults, 
            NodeFilter imagesFilter, String [] datePatterns, String [] urlDatePatterns){
        this(null, IdiscApp.getInstance().getJpaContext().getDao().findAndClose(Site.class, siteId), 
            defaults, imagesFilter, datePatterns, urlDatePatterns);
    }

    public FeedCreationContextImpl(
            String sitename, Sitetype sitetype, Map defaults, 
            NodeFilter imagesFilter, String [] datePatterns, String [] urlDatePatterns){
        this(null, new SiteDao(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), 
            defaults, imagesFilter, datePatterns, urlDatePatterns);
    }
    
    public FeedCreationContextImpl(
            PersistenceUnitMetaData metaData, 
            FeedCreationConfig feedCreationConfig, 
            NodeFilter imagesFilter, 
            String [] datePatterns, 
            String [] urlDatePatterns){
        this.columnNames = metaData.getColumnNames(Feed.class);
        this.columnDisplaySizes = metaData.getColumnDisplaySizes(Feed.class);
        this.parseJob = new ParseJob();
        this.feedCreationConfig = Objects.requireNonNull(feedCreationConfig);
        this.imagesFilter = imagesFilter;
        this.titleFromUrlExtractor = new TitleFromUrlExtractor();

        if(datePatterns == null || datePatterns.length == 0) {
            this.dateExtractor = DateExtractor.NO_INSTANCE;
        }else{
            this.dateExtractor = new DateExtractor(
                    Arrays.asList(datePatterns), 
                    feedCreationConfig.getInputTimeZone(), 
                    feedCreationConfig.getOutputTimeZone()
            );
        }
        
        if(urlDatePatterns == null || urlDatePatterns.length == 0) {
            this.dateFromUrlExtractor = DateStringFromUrlExtractor.NO_INSTANCE;
        }else{
            final Extractor<String, Date> dateFromTextExtractor = new DateExtractor(
                    Arrays.asList(urlDatePatterns), 
                    feedCreationConfig.getInputTimeZone(), 
                    feedCreationConfig.getOutputTimeZone()
            );
            this.dateFromUrlExtractor = new DateFromUrlExtractor(
                    new DateStringFromUrlExtractor(), dateFromTextExtractor
            );
        }
    
        this.imageSizeComparator = new DefaultImageSizeComparator();
    }

 * 
 */