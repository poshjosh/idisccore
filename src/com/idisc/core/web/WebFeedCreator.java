package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.util.FeedCreator;
import com.idisc.pu.Sites;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.config.Config;
import com.scrapper.util.PageNodes;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;

public class WebFeedCreator extends FeedCreator {
    
  public WebFeedCreator(String sitename, Sitetype sitetype, 
          NodeFilter imagesFilter, float dataComparisonTolerance){
    this(new Sites(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), imagesFilter, dataComparisonTolerance);
  }
  
  public WebFeedCreator(Site site, NodeFilter imagesFilter, float dataComparisonTolerance){
    super(site, "news", imagesFilter, dataComparisonTolerance);
  }
  
  public Feed createFeed(PageNodes pageNodes, Date datecreated){
      
    Feed feed = new Feed();
    
    updateFeed(feed, pageNodes, datecreated);
    
    return feed;
  }
  
    public void updateFeed(Feed feed, PageNodes pageNodes, Date datecreated) {
        
XLogger xlog = XLogger.getInstance();
Level level = Level.FINER;
Class cls = this.getClass();
        Site site = this.getSite();

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        NodeExtractor extractor = this.getNodeExtractor();
        
        Map<String, String> extract = extractor.extract(pageNodes);
        
//if(true) {
//System.out.println("URL: "+pageNodes.getFormattedURL());
//System.out.println("Title: "+(pageNodes.getTitle()==null?null:pageNodes.getTitle().toPlainTextString()));
//System.out.println("Extract: "+extract);
//    return feed;
//}        
        JsonConfig config = extractor.getContext().getConfig();
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
            categories = this.getDefaultCategories();
        }
        feed.setCategories(categories);
        
////////////// Content        
        final String $_s = getValue(extract, "content", defaultValues, false);
//System.out.println("" + new Date() + '@' + this.getClass().getName()+"\n----------------------\n"+$_s+"\n----------------------");
        final String content = $_s == null ? null : com.bc.util.Util.removeNonBasicMultilingualPlaneChars($_s);
        feed.setContent(content);
        
////////////// Datecreated
        feed.setDatecreated(datecreated);

////////////// Description        
        String description = getValue(extract, "description", defaultValues, true);
        if(description == null) {
            Boolean descriptionIsGeneric = getBoolean(config, Config.Extractor.isDescriptionGeneric);
            if(descriptionIsGeneric) {
                description = format(content, (String)defaultValues.get("description"), 
                        this.getColumnDisplaySize("description"), true);
            }else{
                description = this.getDescription(pageNodes);
            }
        }
        feed.setDescription(description);
        
if(xlog.isLoggable(level, cls))        
xlog.log(level, "Description. {0} = {1}", cls, extract.get("description"), feed.getDescription());
        
        if(feed.getContent() == null && feed.getDescription() != null) {
            feed.setContent(feed.getDescription());
            feed.setDescription(null);
        }
        
////////////// Feeddate
        String dateStr = extract.get("feeddate");
        Date feeddate = this.getFeeddate(dateStr);

if(xlog.isLoggable(level, cls))        
xlog.log(Level.FINER, "Feeddate. {0} = {1}", cls, extract.get("feeddate"), feeddate);

        feed.setFeeddate(feeddate); 

////////////// Imageurl
        String imageurl = extract.get("imageurl");
        if(imageurl == null) {
            imageurl = this.getImageUrl(pageNodes);
        }
        feed.setImageurl(imageurl);
        
        xlog.log(Level.FINER, "Imageurl. {0} = {1}", 
        cls, extract.get("imageurl"), feed.getImageurl());

////////////// Title        
        String keywords = getValue(extract, "keywords", defaultValues, true);
        if(keywords == null) {
            keywords = this.getKeywords(pageNodes);
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
                    title = format(content, this.getColumnDisplaySize("title"), true);
                }else{
                    title = this.getTitle(pageNodes);
                }
            }
        }
        feed.setTitle(title);
        
        if(feed.getContent() == null && feed.getTitle() != null) {
            feed.setContent(feed.getTitle());
//            feed.setTitle(null);
        }
        
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
}
