package com.idisc.core.web;

import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.bc.webdatex.extractor.date.DateExtractor;
import com.bc.webdatex.extractor.date.DateFromUrlExtractor;
import com.bc.webdatex.extractor.date.DateStringFromUrlExtractor;
import com.bc.webdatex.filter.Filter;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.filter.ImageNodeFilter;
import com.bc.dom.metatags.BasicMetadata;
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
import com.bc.dom.metatags.MetadataChain;
import com.bc.dom.metatags.OpenGraph;
import com.bc.dom.metatags.SchemaArticle;
import com.bc.dom.metatags.TwitterCard;
import com.scrapper.context.CapturerContext;
import java.util.Arrays;
import java.util.Set;
import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import com.bc.webdatex.extractor.TextParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.bc.dom.metatags.Metadata;
import com.bc.dom.HtmlPageDom;

public class WebFeedCreator extends FeedCreator {
    
  private final Date NO_DATE = new Date(0);
  
  private final Date NOW = new Date();
    
  private final NodeExtractor nodeExtractor;
  
  private final Comparator<String> imageSizeComparator;
  
  private final com.bc.webdatex.extractor.Extractor<String, Date> dateExtractor;
  
  private final com.bc.webdatex.extractor.Extractor<String, Date> dateFromUrlExtractor;

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
        TextParser<Date> dateFromDateStringExtractor = new DateExtractor(
                    Arrays.asList(datePatterns), this.getInputTimeZone(), this.getOutputTimeZone()
        );
        this.dateFromUrlExtractor = new DateFromUrlExtractor(
                new DateStringFromUrlExtractor(), dateFromDateStringExtractor 
        );
    }
    
    final int recommendedSize = this.getRecommendedSize("content"); 
    XLogger.getInstance().log(Level.FINE, "Recommended content length: {0}", this.getClass(), recommendedSize);
    this.nodeExtractor = new NodeExtractor(dataComparisonTolerance, context, recommendedSize);
    
    this.imageSizeComparator = new DefaultImageSizeComparator();
    
    this.dateExtractor = new DateExtractor(
                Arrays.asList("yyyy-MM-dd'T'HH:mm:ss"), 
                this.getInputTimeZone(), this.getOutputTimeZone());
  }
  
  public Feed createFeed(HtmlPageDom pageNodes, Date dateCreatedIfNone){
      
    Feed feed = new Feed();
    
    updateFeed(feed, pageNodes, dateCreatedIfNone);
    
    return feed;
  }
  
    public void updateFeed(Feed feed, HtmlPageDom dom, Date dateCreatedIfNone) {
        
XLogger xlog = XLogger.getInstance();
Level level = Level.FINER;
Class cls = this.getClass();

        Site site = this.getSite();

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        Map<String, String> extract = nodeExtractor.extract(dom);
        
        Metadata metaData = new MetadataChain(
                new BasicMetadata(dom), new SchemaArticle(dom),
                new OpenGraph(dom), new TwitterCard(dom));
        
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
        
        final int contentLength = content == null ? 0 : content.length();
        final int recommendedSize = this.getRecommendedSize("content");
        if(contentLength > recommendedSize) {
            xlog.log(Level.WARNING, "For site: {0}, found content length: {1} > recommended: {2}", 
            cls, this.getSite()==null?null:this.getSite().getSite(), contentLength, recommendedSize);
        }
        
////////////// Datecreated
        final String datecreatedStr = extract.get("datecreated");
        Date datecreated = datecreatedStr == null ? null : this.getDate(datecreatedStr, null);
        if(datecreated == null) {
            final String dateStr = metaData.getDateCreated();
            if(dateStr != null && !dateStr.isEmpty()) {
                datecreated = this.dateExtractor.extract(dateStr, null);
            }
        }
        feed.setDatecreated(datecreated==null?dateCreatedIfNone:datecreated);

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
            final String dateStr = metaData.getDate();
            if(dateStr != null && !dateStr.isEmpty()) {
                feeddate = this.dateExtractor.extract(dateStr, null);
            }
            if(feeddate == null) {
                feeddate = this.dateFromUrlExtractor.extract(dom.getURL(), NO_DATE);
            }
        }
        if(feeddate == NO_DATE) {
            xlog.log(level, "Feeddate could not be extracted for feed:: site: {0}, title: {1}, author: {2}", 
                    cls, sitename, feed.getTitle(), feed.getAuthor());
        }else{
            if(feeddate.after(NOW)) {
                feeddate = NOW;
            }
        }    
        feed.setFeeddate(feeddate);

if(xlog.isLoggable(level, cls))        
xlog.log(Level.FINER, "Feeddate. {0} = {1}", cls, feeddateStr, feeddate);

////////////// Imageurl
        String imageurl = extract.get("imageurl");
xlog.log(Level.FINER, "Extracted image url: {0}", cls, imageurl);        

        if(imageurl != null) {
            Filter<String> filter = ((ImageNodeFilter)this.getImagesFilter()).getImageSrcFilter();
            boolean accepted = filter.accept(imageurl);
xlog.log(Level.FINER, "Extracted image url accepted: {0}", cls, accepted);        
            
            if(!accepted) {
                imageurl = null;
            }
        }
        if(imageurl == null) {
            final Set<String> imageUrls = metaData.getImageUrls();
            if(imageUrls != null && !imageUrls.isEmpty()) {
                imageurl = this.getImageUrlOfLargestImage(new ArrayList(imageUrls));
            }
            if(imageurl == null) {
                final NodeList nodeList = dom.getElements();
//                imageurl = this.getFirstImageUrl(nodeList);                
                NodeFilter filter = this.getImagesFilter() != null ? this.getImagesFilter() :
                        new org.htmlparser.filters.NodeClassFilter(ImageTag.class);
                final NodeList imageNodes = nodeList.extractAllNodesThatMatch(filter, true);
                if(imageNodes != null && !imageNodes.isEmpty()) {
                    final List<String> imageSrcs = new ArrayList<>();
                    for(Node node : imageNodes) {
                        if(node instanceof ImageTag) {
                            imageSrcs.add(((ImageTag)node).getImageURL());
                        }
                    }
                    imageurl = this.getImageUrlOfLargestImage(imageSrcs);
                }
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
                title = this.getTitleFromUrlExtractor().extract(dom.getURL(), null);
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
    
  private String getImageUrlOfLargestImage(List<String> imageUrls) {
    Collections.sort(imageUrls, imageSizeComparator);
    return imageUrls.get(imageUrls.size() - 1);
  }  

  public final com.bc.webdatex.extractor.Extractor<String, Date> getDateFromUrlExtractor() {
    return dateFromUrlExtractor;
  }

  public final NodeExtractor getNodeExtractor() {
    return nodeExtractor;
  }
}
