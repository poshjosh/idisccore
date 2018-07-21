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

package com.idisc.core.util;

import com.bc.htmlparser.ParseJob;
import com.bc.jpa.context.JpaContext;
import com.bc.json.config.JsonConfig;
import java.util.logging.Logger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import com.bc.webdatex.extractors.date.DateExtractor;
import com.bc.webdatex.extractors.TitleFromUrlExtractor;
import com.bc.jpa.metadata.JpaMetaData;
import com.bc.nodelocator.ConfigName;
import com.bc.webdatex.extractors.TextParser;
import com.idisc.core.IdiscApp;
import com.idisc.pu.SiteDao;
import org.htmlparser.dom.HtmlDocument;
import org.htmlparser.tags.MetaTag;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 11:05:58 AM
 */
public class FeedCreator extends BaseFeedCreator {

    private transient static final Logger LOG = Logger.getLogger(FeedCreator.class.getName());

    private final boolean allowOpenEnded = false;
    private final int defaultSpaces = 1;

    private final float dataComparisonTolerance;

    private final NodeFilter imagesFilter;

    private final ParseJob parseJob;

    private final TextParser<String> titleFromUrlExtractor;

    private final int [] columnDisplaySizes;

    private final float allowForMultiByteChars = 0.01f;

    private final JpaMetaData metaData;

    public FeedCreator(
            Integer siteId, String defaultCategories, 
            NodeFilter imagesFilter, float dataComparisonTolerance){
        this(null, IdiscApp.getInstance().getJpaContext().getEntityController(Site.class, Integer.class).find(siteId), 
            defaultCategories, imagesFilter, dataComparisonTolerance);
    }

    public FeedCreator(
            String sitename, Sitetype sitetype, String defaultCategories, 
            NodeFilter imagesFilter, float dataComparisonTolerance){
        this(null, new SiteDao(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), 
            defaultCategories, imagesFilter, dataComparisonTolerance);
    }

    public FeedCreator(JpaContext jpa, Site site, String defaultCategories, NodeFilter imagesFilter, float dataComparisonTolerance){
        super(site, defaultCategories);
        this.parseJob = new ParseJob();
        this.imagesFilter = imagesFilter;
        this.dataComparisonTolerance = dataComparisonTolerance;
        this.titleFromUrlExtractor = new TitleFromUrlExtractor();
        this.metaData = jpa.getMetaData();
        this.columnDisplaySizes = metaData.getColumnDisplaySizes(Feed.class);
    }

    public Date getDate(String [] datePatterns, String dateStr, Date outputIfNone) {

        final Level level = Level.FINER;

        if(dateStr != null) {
            dateStr = getPlainText(dateStr.trim());
        }

        Date feeddate = outputIfNone;

        if(dateStr != null && !dateStr.isEmpty()) {

            LOG.log(level, "Date patterns: {0}", datePatterns == null ? null : Arrays.toString(datePatterns));

            if(datePatterns != null && datePatterns.length != 0) {

                TextParser<Date> dateExtractor = 
                        new DateExtractor(Arrays.asList(datePatterns),
                        this.getInputTimeZone(), this.getOutputTimeZone());

                feeddate = dateExtractor.extract(dateStr, outputIfNone);
            }
        }
        return feeddate;
    }

    public String getTitle(HtmlDocument dom) {
        String output = null;
        final TitleTag tag = dom.getTitle();
        if(tag != null) {
            output = tag.getTitle();
            output = output == null ? null : this.truncate(output, this.getRecommendedSize("title"));
        }
        return output;
    }

    public String getDescription(HtmlDocument dom) {
        final MetaTag tag = dom.getDescription().orElse(null);
        return this.getMetaTagContent(tag, "description");
    }

    public String getKeywords(HtmlDocument dom) {
        final MetaTag tag = dom.getKeywords().orElse(null);
        return this.getMetaTagContent(tag, "keywords");
    }

    public String getMetaTagContent(MetaTag tag, String name) {
        String output = null;
        if(tag != null) {
            output = tag.getAttributeValue("content");
            output = output == null ? null : this.truncate(output, this.getRecommendedSize(name));
        }
        return output;
    }

    public int getRecommendedSize(String columnName) {
        final int displaySize = getColumnDisplaySize(columnName);  
        return (int)(displaySize - (displaySize * this.allowForMultiByteChars));
    }

    public int getColumnDisplaySize(String columnName) {
        final int displaySize = this.columnDisplaySizes[metaData.getColumnIndex(Feed.class, columnName)];
        return displaySize;
    }  
  
    public String format(String col, String val, Map defaultValues, boolean plainTextOnly) {
        
        int maxLen = this.getRecommendedSize(col);
        
        return format(val, (String)defaultValues.get(col), maxLen, plainTextOnly);
    }

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
            val = getPlainText(val);
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
  
    public String getPlainText(String s) {
        return getPlainText(s, this.defaultSpaces);
    }
    
    public String getPlainText(String s, int spaces) {
        String output;
        try{
            parseJob.comments(false).separator(" ").maxSeparators(spaces).plainText(true);
            StringBuilder sb = parseJob.parse(s);
            output = sb == null || sb.length() == 0 ? null : sb.toString();
        }catch(IOException e) {
            output = null;
        }
        return output;
    }
    
    public String getFirstImageUrl(NodeList nodeList) {
    
        try{
            
            if(imagesFilter != null) {

                ImageTag imageTag = (ImageTag)getFirst(nodeList);

                if (imageTag != null) {
                    
                    String imageUrl = imageTag.getImageURL();
                    
                    if(LOG.isLoggable(Level.FINE)){
                              LOG.log(Level.FINE, "Image URL: {0}", imageUrl);
                    }

                    return imageUrl;
                }
            }
        }catch(Exception e) {
            
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, "Error extracting image url", e);
            }
        }
        
        return null;
    }
    
    private Node getFirst(NodeList nodeList) {
//        NodeList nodes = nodeList.extractAllNodesThatMatch(imagesFilter, true);        
        Node output = null;
        for (Node node:nodeList) {
            if (imagesFilter.accept (node)) {
                output = node;
                break;
            }
            NodeList children = node.getChildren();
            if (null != children) {
                output = getFirst(children);
                if(output != null) {
                    break;
                }
            }    
        }
        return output;
    }
    
    public Boolean getBoolean(JsonConfig config, ConfigName key) {
        Boolean bval = config.getBoolean(key);
        return bval == null ? Boolean.FALSE : bval;
    }

    public final TextParser<String> getTitleFromUrlExtractor() {
        return titleFromUrlExtractor;
    }

    public final boolean isAllowOpenEnded() {
        return this.allowOpenEnded;
    }

    public final int getDefaultSpaces() {
        return this.defaultSpaces;
    }

    public final float getDataComparisonTolerance() {
        return this.dataComparisonTolerance;
    }

    public final NodeFilter getImagesFilter() {
        return this.imagesFilter;
    }
}
