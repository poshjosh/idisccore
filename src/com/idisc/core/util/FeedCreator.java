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
import com.bc.jpa.PersistenceMetaData;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.web.NewsCrawler;
import com.idisc.core.web.NodeExtractor;
import com.idisc.pu.Sites;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.config.Config;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 11:05:58 AM
 */
public class FeedCreator extends BaseFeedCreator {

  private final boolean allowOpenEnded = false;
  private final int defaultSpaces = 1;
  
  private final float dataComparisonTolerance;
  
  private final NodeFilter imagesFilter;
  private final ParseJob parseJob;
  private final SimpleDateFormat simpleDateFormat;

  private final NodeExtractor nodeExtractor;

  public FeedCreator(
          Integer siteId, String defaultCategories, 
          NodeFilter imagesFilter, float dataComparisonTolerance){
    this(IdiscApp.getInstance().getJpaContext().getEntityController(Site.class, Integer.class).find(siteId), 
            defaultCategories, imagesFilter, dataComparisonTolerance);
  }
  
  public FeedCreator(
          String sitename, Sitetype sitetype, String defaultCategories, 
          NodeFilter imagesFilter, float dataComparisonTolerance){
    this(new Sites(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), 
            defaultCategories, imagesFilter, dataComparisonTolerance);
  }
  
  public FeedCreator(Site site, String defaultCategories, NodeFilter imagesFilter, float dataComparisonTolerance){
    super(site, defaultCategories);
    this.simpleDateFormat = new SimpleDateFormat();
    this.parseJob = new ParseJob();
    this.nodeExtractor = new NodeExtractor(dataComparisonTolerance, site.getSite());
    this.imagesFilter = imagesFilter;
    this.dataComparisonTolerance = dataComparisonTolerance;
  }
  
  public Date getFeeddate(String dateStr) {
XLogger xlog = XLogger.getInstance();
Level level = Level.FINER;
Class cls = this.getClass();

    if(dateStr != null) {
        dateStr = getPlainText(dateStr.trim());
    }

    Date feeddate = null;
    if(dateStr != null && !dateStr.isEmpty()) {

        String [] datePatterns = nodeExtractor.getDatePatterns();

if(xlog.isLoggable(level, cls))
xlog.log(level, "Date patterns: {0}", cls, datePatterns == null ? null : Arrays.toString(datePatterns));

        if(datePatterns != null && datePatterns.length != 0) {
            for(String pattern:datePatterns) {
                simpleDateFormat.applyPattern(pattern);
                try{

                    feeddate = simpleDateFormat.parse(dateStr);

                    xlog.log(Level.FINER, "Parsed date: {0}", cls, feeddate);

                    break;
                }catch(ParseException ignored) { }
            }
        }
    }
    if(feeddate == null){
        feeddate = new Date(); // We need this for sorting
    }
    return feeddate;
  }

  public String getTitle(PageNodes pageNodes) {
    String title = null;
    if(pageNodes.getTitle() != null) {
        TitleTag titleTag = pageNodes.getTitle();
        if(titleTag != null) {
            title = titleTag.toPlainTextString();
            title = title == null ? null : this.truncate(title, this.getColumnDisplaySize("title"));
        }
    }
    return title;
  }
    
  public String getKeywords(PageNodes pageNodes) {
    Objects.requireNonNull(pageNodes);
    return this.getMetaContent(pageNodes.getKeywords(), "keywords");
  }
  
  public String getDescription(PageNodes pageNodes) {
    Objects.requireNonNull(pageNodes);
    return this.getMetaContent(pageNodes.getDescription(), "description");
  }
    
  public String getMetaContent(MetaTag meta, String columnName) {
    return meta == null ? null :
            this.truncate(meta.getAttribute("content"), this.getColumnDisplaySize(columnName));
  }
  
  public String getImageUrl(PageNodes pageNodes) {
    Objects.requireNonNull(pageNodes);
    String imageurl = this.getFirstImageUrl(pageNodes.getNodeList());
//    if(imageurl == null) {
//        imageurl = site.getIconurl();
//        if(imageurl == null) {
//            Link link = pageNodes.getIcon();
//            if(link == null) {
//                link = pageNodes.getIco();
//            }
//            imageurl = link == null ? null : link.getLink();
//        }
//    }
    return imageurl;
  }
    
  private int [] _fcds;
  public int getColumnDisplaySize(String columnName) {
    PersistenceMetaData metaData = IdiscApp.getInstance().getJpaContext().getMetaData();  
    if(_fcds == null) {
      // Round trips to the database  
      _fcds = metaData.getColumnDisplaySizes(Feed.class);
    }
    int displaySize = _fcds[metaData.getColumnIndex(Feed.class, columnName)];
    return displaySize;
  }  
  
    public String format(String col, String val, Map defaultValues, boolean plainTextOnly) {
        
        int maxLen = this.getColumnDisplaySize(col);
        
        return format(val, (String)defaultValues.get(col), maxLen, plainTextOnly);
    }

    public String format(String col, String val, boolean plainTextOnly) {
        
        int maxLen = this.getColumnDisplaySize(col);
        
        return format(val, null, maxLen, plainTextOnly);
    }
    
    
    public String format(String col, String val, String defaultValue, boolean plainTextOnly) {
        
        int maxLen = this.getColumnDisplaySize(col);
        
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
        if(val != null && plainTextOnly) {
            val = getPlainText(val);
        }
        return truncate(val, maxLen-3);
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
    
    public String extractTitleFromUrl(String url) {
//   .../the-government-has-done-it-again.html            
// we extract: the government has done it again            
        url = url.trim();
        if(url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String title;
        int a = url.lastIndexOf('/');
        if(a != -1) {
//            int b = url.lastIndexOf('.', a); // This counts backwards from a
            int b = url.lastIndexOf('.');
            if(b == -1 || b < a) {
                b = url.length();
            }
            title = url.substring(a+1, b);
            title = title.replaceAll("\\W", " "); //replace all non word chars
        }else{
            title = null;
        }
        return title;
    }
    
    public String getFirstImageUrl(NodeList nodeList) {
    
        try{
            
            if(imagesFilter != null) {

                String html = nodeList.toHtml();
                int start = html.indexOf("<img ");
                if(start == -1) {
                    start = html.indexOf("<IMG ");
                }
                if(start != -1) {
                    int end = start + 200 > html.length() ? html.length() : start + 200;
                    XLogger.getInstance().log(Level.FINE, "IMAGE part: {0}", NewsCrawler.class, html.substring(start, end));
                }

                ImageTag imageTag = (ImageTag)getFirst(nodeList);

                if (imageTag != null) {
                    
                    String imageUrl = imageTag.getImageURL();
                    
                    if(acceptImageUrl(imageUrl)) {
if(start != -1) {
XLogger.getInstance().log(Level.FINE, "IMAGE URL: {0}", NewsCrawler.class, imageUrl);
}                
                        return imageUrl;
                    }
                }
            }
        }catch(Exception e) {
            
            XLogger.getInstance().log(Level.WARNING, "Error extracting image url", Util.class, e);
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
    
    private boolean acceptImageUrl(String imageUrl) {
        
        if(imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
//@todo unwanted formats. Make this a property                
// https://d5nxst8fruw4z.cloudfront.net/atrk.gif?account=rrH8k1a0CM00UH                
        int n = imageUrl.indexOf(".cloudfront.net/");
        if(n != -1) {
            return false;
        }
  
        try{
            URL url = new URL(imageUrl);
            return true;
        }catch(MalformedURLException e) {
            return false;
        }
// Potentially time consuming        
//        try{
//            return ConnectionManager.exists(imageUrl);
//        }catch(Exception e) {
//            return false;
//        }    
    }
    
    
  public Boolean getBoolean(JsonConfig config, Config.Extractor key) {
    Boolean bval = config.getBoolean(key);
    return bval == null ? Boolean.FALSE : bval;
  }

  public final NodeExtractor getNodeExtractor() {
    return nodeExtractor;
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
