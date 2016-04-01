package com.idisc.core.web;

import com.bc.htmlparser.ParseJob;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.Util;
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

public class WebFeedCreator
{
  private boolean allowOpenEnded;
  private int defaultSpaces = 1;
  
  private float tolerance;
  
  private String defaultCategories = "news";
  private Site site;
  private NodeFilter imagesFilter;
  private StringBuilder _sb;
  private ParseJob _pj;
  
  public WebFeedCreator() {}
  
  public WebFeedCreator(String sitename, Sitetype sitetype)
  {
    this(Util.findSite(sitename, sitetype, true));
  }
  
  public WebFeedCreator(Site site)
  {
    if (site == null) {
      throw new NullPointerException();
    }
    
    this.site = site;
  }
  
  public Feed createFeed(PageNodes pageNodes)
  {
    Feed feed = new Feed();
    
    updateFeed(feed, pageNodes);
    
    return feed;
  }
  
    public void updateFeed(Feed feed, PageNodes pageNodes) {

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        NodeExtractor extractor = new NodeExtractor(tolerance, sitename);
        
        Map<String, String> extract = extractor.extract(pageNodes);
        
//if(true) {
//System.out.println("URL: "+pageNodes.getFormattedURL());
//System.out.println("Title: "+(pageNodes.getTitle()==null?null:pageNodes.getTitle().toPlainTextString()));
//System.out.println("Extract: "+extract);
//    return feed;
//}        
        JsonConfig config = extractor.getContext().getConfig();
        Map defaultValues = config.getMap(Config.Formatter.defaultValues);
        
        ParseJob parseJob = this.getParseJob();

////////////// Author        
        String author = getValue(extract, "author", defaultValues, parseJob);
        if ((author != null) && (author.startsWith("a target=")) && (author.contains("punch"))) {
            author = "Punch Newspaper";
        }
        if ((author != null) && (author.startsWith("a href=")) && (author.contains("leadership"))) {
            author = "Leaderhship Newspaper";
        }
        if(author == null || author.trim().isEmpty()) {
            author = sitename;
        }
XLogger.getInstance().log(Level.FINER, "Author. {0} = {1}", 
        NewsCrawler.class, extract.get("author"), author);
        feed.setAuthor(author);
        
////////////// Categories        
        String categories = getValue(extract, "categories", defaultValues, parseJob);
        if(categories == null) {
            categories = defaultCategories;
        }
        feed.setCategories(categories);
        
////////////// Content        
        String content = extract.get("content");
        feed.setContent(Util.truncate(Feed.class, "content", content));
        
//        feed.setDatecreated(new Date()); // set before creation

////////////// Description        
        String description = getValue(extract, "description", defaultValues, parseJob);
        if(description == null) {
            Boolean descriptionIsGeneric = getBoolean(config, Config.Extractor.isDescriptionGeneric);
            if(descriptionIsGeneric) {
                description = format(content, (String)defaultValues.get("description"), 300, parseJob);
            }else{
                if(pageNodes.getDescription() != null) {
                    MetaTag meta = pageNodes.getDescription();
                    description = Util.truncate(Feed.class, "description", meta.getAttribute("content"));
                }
            }
        }
        feed.setDescription(description);
XLogger.getInstance().log(Level.FINER, "Description. {0} = {1}", 
        NewsCrawler.class, extract.get("description"), feed.getDescription());
        
////////////// Feeddate
        String dateStr = extract.get("feeddate");
        if(dateStr != null) {
            dateStr = getPlainText(dateStr.trim(), parseJob);
        }

        Date feeddate = null;
        if(dateStr != null && !dateStr.isEmpty()) {
            
            String [] datePatterns = extractor.getDatePatterns();

XLogger.getInstance().log(Level.FINER, "Date patterns: {0}", NewsCrawler.class, 
        datePatterns == null ? null : Arrays.toString(datePatterns));
            
            if(datePatterns != null && datePatterns.length != 0) {
                SimpleDateFormat df = new SimpleDateFormat();
                for(String pattern:datePatterns) {
                    df.applyPattern(pattern);
                    try{
                        
                        feeddate = df.parse(dateStr);
                        
XLogger.getInstance().log(Level.FINER, "Parsed date: {0}", NewsCrawler.class, feeddate);

                        break;
                    }catch(ParseException ignored) { }
                }
            }
        }
        if(feeddate == null){
            feeddate = new Date(); // We need this for sorting
        }
XLogger.getInstance().log(Level.FINER, "Feeddate. {0} = {1}", 
        NewsCrawler.class, extract.get("feeddate"), feeddate);
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
XLogger.getInstance().log(Level.FINER, "Imageurl. {0} = {1}", 
        NewsCrawler.class, extract.get("imageurl"), feed.getImageurl());

////////////// Title        
        String keywords = getValue(extract, "keywords", defaultValues, parseJob);
        if(keywords == null) {
            if(pageNodes.getKeywords() != null) {
                MetaTag meta = pageNodes.getKeywords();
                keywords = Util.truncate(Feed.class, "keywords", meta.getAttribute("content"));
            }
        }
        feed.setKeywords(keywords);
        
////////////// Title
        String title = getValue(extract, "title", defaultValues, parseJob);
        if(title == null) {
            Boolean titleIsInUrl = getBoolean(config, Config.Extractor.isTitleInUrl);
            if(titleIsInUrl) {
                String url = pageNodes.getURL();
                title = extractTitleFromUrl(url);
            }
            if(title == null) {
                Boolean titleIsGeneric = getBoolean(config, Config.Extractor.isTitleGeneric);
                if(titleIsGeneric) {
                    title = format(content, null, Util.getColumnDisplaySize(Feed.class, "title"), parseJob);
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
    
    private String getValue(Map<String, String> extract, String col, Map defaultValues, ParseJob parseJob) {
        
        String val = extract.get(col);
        
        if(val != null) {
            
            val = format(col, val, defaultValues, parseJob);
        }
        
        return val;
    }
    
    private String format(String col, String val, Map defaultValues, ParseJob parseJob) {
        
        int maxLen = Util.getColumnDisplaySize(Feed.class, col) - 3;
        
        return format(val, (String)defaultValues.get(col), maxLen, parseJob);
    }
    
    public String format(
            String s, String defaultValue, int maxLen, ParseJob pj) {
        if(s == null) {
            s = defaultValue;
        }
        if(pj != null) {
            s = getPlainText(s, pj);
            if(s == null) {
                s = defaultValue;
            }
        }
        return Util.truncate(s, maxLen);
    }

    private String getPlainText(String s, ParseJob pj) {
        return getPlainText(s, 1, pj);
    }
    
    private String getPlainText(String s, int spaces, ParseJob pj) {
        String output;
        pj.reset();
        try{
            pj.comments(false).formatter(null).maxSeparators(spaces).plainText(true).tagFilter(null);
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
      this._pj = this._pj.comments(false).maxSeparators(this.defaultSpaces).plainText(true).separator(" ");
    } else {
      this._pj.reset();
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
