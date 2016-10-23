package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.bc.webdatex.extractor.Extractor;
import com.bc.webdatex.extractor.date.DateExtractor;
import com.bc.webdatex.extractor.date.DateFromUrlExtractor;
import com.bc.webdatex.extractor.date.DateStringFromUrlExtractor;
import com.bc.webdatex.filter.Filter;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.filter.ImagesFilter;
import com.bc.webdatex.nodedata.BasicMetatagsData;
import com.idisc.core.util.FeedCreator;
import com.idisc.pu.SiteService;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.config.Config;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.nodedata.Dom;
import com.bc.webdatex.nodedata.MetatagsData;
import com.bc.webdatex.nodedata.MetatagsDataBuilder;
import com.bc.webdatex.nodedata.OpenGraph;
import com.bc.webdatex.nodedata.SchemaArticle;
import com.bc.webdatex.nodedata.TwitterCard;
import com.scrapper.context.CapturerContext;
import java.text.ParseException;
import java.util.Arrays;

public class WebFeedCreator extends FeedCreator {
    
  private final Date NO_DATE = new Date(0);
    
  private final NodeExtractor nodeExtractor;
  
  private final Extractor<Date> dateFromUrlExtractor;

  public WebFeedCreator(String sitename, Sitetype sitetype, 
          NodeFilter imagesFilter, float dataComparisonTolerance){
    this(new SiteService(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), imagesFilter, dataComparisonTolerance);
  }
  
  public WebFeedCreator(Site site, NodeFilter imagesFilter, float dataComparisonTolerance){
    super(site, "news", imagesFilter, dataComparisonTolerance);
    final CapturerContext context = IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(site.getSite());
    final String [] datePatterns = context.getSettings().getUrlDatePatterns();
    if(datePatterns == null || datePatterns.length == 0) {
        this.dateFromUrlExtractor = DateStringFromUrlExtractor.NO_INSTANCE;
    }else{
        Extractor<Date> dateFromDateStringExtractor = new DateExtractor(
                    Arrays.asList(datePatterns), this.getInputTimeZone(), this.getOutputTimeZone()
        );
        this.dateFromUrlExtractor = new DateFromUrlExtractor(
                new DateStringFromUrlExtractor(), dateFromDateStringExtractor 
        );
    }
    this.nodeExtractor = new NodeExtractor(dataComparisonTolerance, context);
  }
  
  public Feed createFeed(Dom pageNodes, Date dateCreatedIfNone){
      
    Feed feed = new Feed();
    
    updateFeed(feed, pageNodes, dateCreatedIfNone);
    
    return feed;
  }
  
    public void updateFeed(Feed feed, Dom dom, Date dateCreatedIfNone) {
        
XLogger xlog = XLogger.getInstance();
Level level = Level.FINER;
Class cls = this.getClass();
        Site site = this.getSite();

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        Map<String, String> extract = nodeExtractor.extract(dom);
        
//        MetatagsData metaData = new MetatagsDataImpl(dom, 
//                Arrays.asList("yyyy-MM-dd'T'HH:mm:ss"), this.getInputTimeZone(), this.getOutputTimeZone());
        MetatagsData metaData = new MetatagsDataBuilder().dom(dom)
                .dateExtractor(Arrays.asList("yyyy-MM-dd'T'HH:mm:ss"), this.getInputTimeZone(), this.getOutputTimeZone())
                .buildComposite(BasicMetatagsData.class, SchemaArticle.class, OpenGraph.class, TwitterCard.class);
        
        final CapturerContext context = nodeExtractor.getContext();
        
        final JsonConfig config = context.getConfig();
        
        final Map defaults = context.getSettings().getDefaults();
        
////////////// Author        
        String author = getValue(extract, "author", defaults, true, metaData.getAuthor(), metaData.getPublisher());
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
        String categories = getValue(extract, "categories", defaults, true, metaData.getCategories());
        if(categories == null) {
            categories = this.getDefaultCategories();
        }
        feed.setCategories(categories);
        
////////////// Content        
        final String content = getValue(extract, "content", defaults, false);
        feed.setContent(content);
        
if(xlog.isLoggable(level, cls)) {
xlog.log(level, "Content length. initial: {0}, after format: {1}", cls,
extract.get("content")==null?null:extract.get("content").length(), 
content==null?null:content.length());
}
        
////////////// Datecreated
        final String datecreatedStr = extract.get("datecreated");
        Date datecreated = datecreatedStr == null ? null : this.getDate(datecreatedStr, null);
        if(datecreated == null) {
            try{
                datecreated = metaData.getDateCreated();
            }catch(ParseException e) {
                XLogger.getInstance().log(Level.WARNING, "Error parse date from metatag", this.getClass(), e);
            }
            if(datecreated == null) {
                datecreated = dateCreatedIfNone;
            }
        }
        feed.setDatecreated(datecreated);

////////////// Description        
        String description = getValue(extract, "description", defaults, true);
        if(description == null) {
            Boolean descriptionIsGeneric = getBoolean(config, Config.Extractor.isDescriptionGeneric);
            final int displaySize = this.getRecommendedSize("description");
            if(descriptionIsGeneric) {
                description = format(content, (String)defaults.get("description"), displaySize, true);
            }else{
                description = this.format(metaData.getDescription(), displaySize, true);
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
        final String feeddateStr = extract.get("feeddate");
        Date feeddate = feeddateStr == null ? null : this.getDate(feeddateStr, null);
        if(feeddate == null) {
            feeddate = metaData.getDate();
            if(feeddate == null) {
                feeddate = this.dateFromUrlExtractor.extract(dom.getFormattedURL(), NO_DATE);
            }
        }
        if(feeddate == NO_DATE) {
            xlog.log(level, "Feeddate could not be extracted for feed:: site: {0}, title: {1}, author: {2}", 
                    cls, sitename, feed.getTitle(), feed.getAuthor());
        }    
        feed.setFeeddate(feeddate);

if(xlog.isLoggable(level, cls))        
xlog.log(Level.FINER, "Feeddate. {0} = {1}", cls, feeddateStr, feeddate);

////////////// Imageurl
        String imageurl = extract.get("imageurl");
xlog.log(Level.FINER, "Extracted image url: {0}", cls, imageurl);        

        if(imageurl != null) {
            Filter<String> filter = ((ImagesFilter)this.getImagesFilter()).getImageSrcFilter();
            boolean accepted = filter.accept(imageurl);
xlog.log(Level.FINER, "Extracted image url accepted: {0}", cls, accepted);        
            
            if(!accepted) {
                imageurl = null;
            }
        }
        if(imageurl == null) {
            imageurl = metaData.getImageUrlOfLargestImage();
            if(imageurl == null) {
                imageurl = this.getFirstImageUrl(dom.getNodeList());
            }
xlog.log(Level.FINER, "Body content extracted image url: ", cls, imageurl);        
        }
        feed.setImageurl(imageurl);

////////////// Keywords
        String keywords = getValue(extract, "keywords", defaults, true, metaData.getKeywords(), metaData.getTags());
        feed.setKeywords(keywords);
        
////////////// Title
        String title = getValue(extract, "title", defaults, true);
        if(title == null) {
            Boolean titleIsInUrl = getBoolean(config, Config.Extractor.isTitleInUrl);
            if(titleIsInUrl) {
                title = this.getTitleFromUrlExtractor().extract(dom.getFormattedURL(), null);
            }
            if(title == null) {
                Boolean titleIsGeneric = getBoolean(config, Config.Extractor.isTitleGeneric);
                if(titleIsGeneric) {
                    title = format(content, this.getRecommendedSize("title"), true);
                }else{
                    title = this.getTitle(dom);
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
        feed.setUrl(dom.getURL()); 
        
//if(true) {
//System.out.println("@"+this.getClass().getName());
//System.out.println("URL: "+feed.getUrl());
//System.out.println("Feed date: "+feed.getFeeddate());
//System.out.println("Title: "+feed.getTitle());
//System.out.println("Extract: "+extract);
//}        
    }
    
  public Date getDate(String dateStr, Date defaultIfNone) {

    String [] datePatterns = nodeExtractor.getContext().getSettings().getDatePatterns();
    
    return this.getDate(datePatterns, dateStr, defaultIfNone);
  }
    
    private String getValue(Map<String, String> extract, String col, Map defaults, 
            boolean plainTextOnly, String... alternatives) {
        
        String val = extract.get(col);
        
        if(alternatives != null) {
            for(String alternative : alternatives) {
                if(alternative != null) {
                    val = alternative;
                    break;
                }
            }
        }
        
        if(val != null) {
            
            val = format(col, val, defaults, plainTextOnly);
            
        }else{
            
            val = defaults == null ? null : (String)defaults.get(col);
        }
        
        return val;
    }

  public final Extractor<Date> getDateFromUrlExtractor() {
    return dateFromUrlExtractor;
  }

  public final NodeExtractor getNodeExtractor() {
    return nodeExtractor;
  }
}
