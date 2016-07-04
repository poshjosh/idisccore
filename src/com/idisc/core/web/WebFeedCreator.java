package com.idisc.core.web;

import com.bc.htmlparser.ParseJob;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.util.Util;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.config.Config;
import com.scrapper.tag.Link;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;

public class WebFeedCreator {
    
  private boolean allowOpenEnded;
  private int defaultSpaces = 1;
  
  private float tolerance;
  
  private String defaultCategories = "news";
  private Site site;
  private NodeFilter imagesFilter;
  private StringBuilder _sb;
  private ParseJob _pj;
  private final SimpleDateFormat simpleDateFormat;

  private final NodeExtractor extractor;
  
  public WebFeedCreator() {
    simpleDateFormat = new SimpleDateFormat();  
    extractor = new NodeExtractor();
  }
    
  public WebFeedCreator(String sitename, Sitetype sitetype){
    this(Util.findSite(sitename, sitetype, true));
  }
  
  public WebFeedCreator(Site site){
    if (site == null) {
      throw new NullPointerException();
    }
    this.site = site;
    this.simpleDateFormat = new SimpleDateFormat();
    this.extractor = new NodeExtractor();
  }
  
  public Feed createFeed(PageNodes pageNodes, Date datecreated){
      
    Feed feed = new Feed();
    
    updateFeed(feed, pageNodes, datecreated);
    
    return feed;
  }
  
    public void updateFeed(Feed feed, PageNodes pageNodes, Date datecreated) {
        
        XLogger xlog = XLogger.getInstance();
        Class cls = this.getClass();

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        this.extractor.setTolerance(tolerance);
        this.extractor.setSitename(sitename);
        
        Map<String, String> extract = this.extractor.extract(pageNodes);
        
//if(true) {
//System.out.println("URL: "+pageNodes.getFormattedURL());
//System.out.println("Title: "+(pageNodes.getTitle()==null?null:pageNodes.getTitle().toPlainTextString()));
//System.out.println("Extract: "+extract);
//    return feed;
//}        
        JsonConfig config = this.extractor.getContext().getConfig();
        Map defaultValues = config.getMap(Config.Formatter.defaultValues);
        
////////////// Author        
        String author = getValue(extract, "author", defaultValues, true);
        if ((author != null) && (author.startsWith("a target=")) && (author.contains("punch"))) {
            author = "Punch Newspaper";
        }
        if ((author != null) && (author.startsWith("a href=")) && (author.contains("leadership"))) {
            author = "Leaderhship Newspaper";
        }
        if(author == null || author.trim().isEmpty()) {
            author = sitename;
        }
        xlog.log(Level.FINER, "Author. {0} = {1}", cls, extract.get("author"), author);
        feed.setAuthor(author);
        
////////////// Categories        
        String categories = getValue(extract, "categories", defaultValues, true);
        if(categories == null) {
            categories = defaultCategories;
        }
        feed.setCategories(categories);
        
////////////// Content        
        final String $_s = getValue(extract, "content", defaultValues, false);
        final String content = Util.removeNonBasicMultilingualPlaneChars($_s);
        feed.setContent(content);
        
////////////// Datecreated
        feed.setDatecreated(datecreated);

////////////// Description        
        String description = getValue(extract, "description", defaultValues, true);
        if(description == null) {
            Boolean descriptionIsGeneric = getBoolean(config, Config.Extractor.isDescriptionGeneric);
            if(descriptionIsGeneric) {
                description = format(content, (String)defaultValues.get("description"), 
                        Util.getColumnDisplaySize(Feed.class, "description"), true);
            }else{
                if(pageNodes.getDescription() != null) {
                    MetaTag meta = pageNodes.getDescription();
                    description = Util.truncate(Feed.class, "description", meta.getAttribute("content"));
                }
            }
        }
        feed.setDescription(description);
        xlog.log(Level.FINER, "Description. {0} = {1}", 
        cls, extract.get("description"), feed.getDescription());
        
////////////// Feeddate
        String dateStr = extract.get("feeddate");
        if(dateStr != null) {
            dateStr = getPlainText(dateStr.trim());
        }

        Date feeddate = null;
        if(dateStr != null && !dateStr.isEmpty()) {
            
            String [] datePatterns = extractor.getDatePatterns();

        xlog.log(Level.FINER, "Date patterns: {0}", cls, 
        datePatterns == null ? null : Arrays.toString(datePatterns));
            
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
        xlog.log(Level.FINER, "Feeddate. {0} = {1}", cls, extract.get("feeddate"), feeddate);
        feed.setFeeddate(feeddate); 

////////////// Imageurl
        String imageurl = extract.get("imageurl");
        if(imageurl != null) {
            feed.setImageurl(imageurl);
        }else{
            if(imagesFilter != null) {
                String imageUrl = Util.getFirstImageUrl(pageNodes.getNodeList(), imagesFilter);
                if(imageUrl != null) {
                    feed.setImageurl(imageUrl);
                }
            }
        }
        if(feed.getImageurl() == null) {
            Link link = pageNodes.getIcon();
            if(link == null) {
                link = pageNodes.getIco();
            }
            if(link != null) {
                feed.setImageurl(link.getLink());
            }
        }
        xlog.log(Level.FINER, "Imageurl. {0} = {1}", 
        cls, extract.get("imageurl"), feed.getImageurl());

////////////// Title        
        String keywords = getValue(extract, "keywords", defaultValues, true);
        if(keywords == null) {
            if(pageNodes.getKeywords() != null) {
                MetaTag meta = pageNodes.getKeywords();
                keywords = Util.truncate(Feed.class, "keywords", meta.getAttribute("content"));
            }
        }
        feed.setKeywords(keywords);
        
////////////// Title
        String title = getValue(extract, "title", defaultValues, true);
        if(title == null) {
            Boolean titleIsInUrl = getBoolean(config, Config.Extractor.isTitleInUrl);
            if(titleIsInUrl) {
                String url = pageNodes.getURL();
                title = extractTitleFromUrl(url);
            }
            if(title == null) {
                Boolean titleIsGeneric = getBoolean(config, Config.Extractor.isTitleGeneric);
                if(titleIsGeneric) {
                    title = format(content, null, Util.getColumnDisplaySize(Feed.class, "title"), true);
                }else{
                    if(pageNodes.getTitle() != null) {
                        TitleTag titleTag = pageNodes.getTitle();
                        title = titleTag.toPlainTextString();
                    }
                }
            }
        }
        feed.setTitle(title);
        
        // We use this not the formatted value so 
        // that we can check for duplicates in the future
        // using methods like isInDatabase(String link)
        feed.setUrl(pageNodes.getURL());
    }
    
    private String getValue(Map<String, String> extract, String col, Map defaultValues, boolean plainTextOnly) {
        
        String val = extract.get(col);
        
        if(val != null) {
            
            val = format(col, val, defaultValues, plainTextOnly);
        }
        
        return val;
    }
    
    public String format(String col, String val, Map defaultValues, boolean plainTextOnly) {
        
        int maxLen = Util.getColumnDisplaySize(Feed.class, col);
        
        return format(val, (String)defaultValues.get(col), maxLen, plainTextOnly);
    }
    
    public String format(
            String s, String defaultValue, int maxLen, boolean plainTextOnly) {
        if(s == null) {
            s = defaultValue;
        }
        s = getPlainText(s);
        if(s == null) {
            s = defaultValue;
        }
        return Util.truncate(s, maxLen-3);
    }

    private String getPlainText(String s) {
        return getPlainText(s, this.defaultSpaces);
    }
    
    private String getPlainText(String s, int spaces) {
        String output;
        ParseJob pj = this.getParseJob().resetToDefaults();
        try{
            pj.comments(false).separator(" ").maxSeparators(spaces).plainText(true);
            StringBuilder sb = pj.parse(s);
            output = sb == null || sb.length() == 0 ? null : sb.toString();
        }catch(IOException e) {
            output = null;
        }
        return output;
    }
    
    private String extractTitleFromUrl(String url) {
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
    
    private Boolean getBoolean(JsonConfig config, Config.Extractor key) {
        Boolean bval = config.getBoolean(key);
        if(bval == null) {
            bval = Boolean.FALSE;
        }
        return bval;
    }
    
  private ParseJob getParseJob()
  {
    if (this._pj == null) {
      this._pj = new ParseJob();
    } else {
      this._pj.resetToDefaults();
    }
    return this._pj;
  }
  
  public boolean isAllowOpenEnded() {
    return this.allowOpenEnded;
  }
  
  public void setAllowOpenEnded(boolean allowOpenEnded) {
    this.allowOpenEnded = allowOpenEnded;
  }
  
  public int getDefaultSpaces() {
    return this.defaultSpaces;
  }
  
  public void setDefaultSpaces(int defaultSpaces) {
    this.defaultSpaces = defaultSpaces;
  }
  
  public float getTolerance() {
    return this.tolerance;
  }
  
  public void setTolerance(float tolerance) {
    this.tolerance = tolerance;
  }
  
  public String getDefaultCategories() {
    return this.defaultCategories;
  }
  
  public void setDefaultCategories(String defaultCategories) {
    this.defaultCategories = defaultCategories;
  }
  
  public NodeFilter getImagesFilter() {
    return this.imagesFilter;
  }
  
  public void setImagesFilter(NodeFilter imagesFilter) {
    this.imagesFilter = imagesFilter;
  }
  
  public Site getSite() {
    return this.site;
  }
  
  public void setSite(Site site) {
    this.site = site;
  }
}
