package com.idisc.core.extraction.web;

import com.bc.jpa.context.JpaContext;
import com.bc.json.config.JsonConfig;
import com.bc.meta.Metadata;
import java.util.logging.Logger;
import com.bc.webdatex.extractors.date.DateExtractor;
import com.bc.webdatex.extractors.date.DateFromUrlExtractor;
import com.bc.webdatex.extractors.date.DateStringFromUrlExtractor;
import com.bc.webdatex.filters.Filter;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.util.FeedCreator;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.context.CapturerContext;
import java.util.Arrays;
import java.util.Set;
import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import com.bc.webdatex.extractors.TextParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.htmlparser.dom.HtmlDocument;
import com.idisc.pu.SiteDao;
import java.util.Collection;
import com.bc.meta.ArticleMetaNames;
import com.bc.meta.impl.MetadataImpl;
import com.bc.meta.selector.Selector;
import com.bc.nodelocator.ConfigName;
import com.idisc.core.CreateMetaTagSelector;
import com.idisc.pu.entities.Feed_;
import java.text.MessageFormat;
import java.util.LinkedHashMap;

public class WebFeedCreator extends FeedCreator {
  
  private transient static final Logger LOG = Logger.getLogger(WebFeedCreator.class.getName());
    
  private final Date NOW = new Date();
    
  private final String [] dateNames = new String[]{ArticleMetaNames.DATE_CREATED, 
      ArticleMetaNames.DATE_PUBLISHED, ArticleMetaNames.DATE_MODIFIED};
  
  private final WebFeedDataExtractor nodeExtractor;
  
  private final Comparator<String> imageSizeComparator;
  
  private final com.bc.webdatex.extractors.Extractor<String, Date> dateExtractor;
  
  private final com.bc.webdatex.extractors.Extractor<String, Date> dateFromUrlExtractor;

  private final Map defaults;
  
  private final Selector<Node> metaTagSelector;
  
  private final Map<String, Object> reusedMap = new LinkedHashMap();
  
  private final com.bc.meta.selector.impl.Collectors.CollectIntoMap reusedCollector =
          new com.bc.meta.selector.impl.Collectors.CollectIntoMap(reusedMap);
  
  public WebFeedCreator(String sitename, Sitetype sitetype, 
          NodeFilter imagesFilter, float dataComparisonTolerance){
    this(new SiteDao(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), imagesFilter, dataComparisonTolerance);
  }
  
  public WebFeedCreator(Site site, NodeFilter imagesFilter, float dataComparisonTolerance){
      this(IdiscApp.getInstance().getScrapperContextFactory().getContext(site.getSite()), 
              IdiscApp.getInstance().getJpaContext(),
              site, 
              imagesFilter, 
              dataComparisonTolerance);
  }

  public WebFeedCreator(
          CapturerContext context, 
          JpaContext jpa,
          Site site, 
          NodeFilter imagesFilter, 
          float dataComparisonTolerance){
    super(jpa, site, "news", imagesFilter, dataComparisonTolerance);
    final String [] datePatterns = context.getNodeExtractorConfig().getUrlDatePatterns();
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
    if(LOG.isLoggable(Level.FINE)){
      LOG.log(Level.FINE, "Recommended content length: {0}", recommendedSize);
    }
    this.nodeExtractor = new WebFeedDataExtractor(dataComparisonTolerance, context, recommendedSize);
    
    this.imageSizeComparator = new DefaultImageSizeComparator();
    
    this.dateExtractor = new DateExtractor(
                Arrays.asList("yyyy-MM-dd'T'HH:mm:ss"), 
                this.getInputTimeZone(), this.getOutputTimeZone());

    final Map m = context.getNodeExtractorConfig().getDefaults();
    this.defaults = m == null ? Collections.EMPTY_MAP : context.getNodeExtractorConfig().getDefaults();

    this.metaTagSelector = new CreateMetaTagSelector().get();
  }
  
  public Feed createFeed(HtmlDocument doc){

//    Feed feed = this.parseMetadata(pageNodes, this.collectIntoFeed, null);
//    if(feed == null) {
//        feed = new Feed();
//    }
//    this.updateFeed(feed, pageNodes, dateCreatedIfNone, ArticleMetaNames.EMPTY);
    Feed feed = new Feed();
    this.updateFeed(feed, doc);
    return feed;
  }
  
    public Metadata parseMetadata(HtmlDocument dom) {
        final Map map = this.parseMetatags(dom);
        final Metadata metaData = map.isEmpty() ? Metadata.EMPTY : new MetadataImpl(map);
        return metaData;
    }

    public Map parseMetatags(HtmlDocument dom) {
        final List metaTags = dom.getMetaTags();
        reusedMap.clear();
        if(!metaTags.isEmpty()) {
            metaTagSelector.select(metaTags.iterator(), ArticleMetaNames.values(), reusedCollector);
        }
        return reusedMap;
    }
    
    public <D> D parseMetadata(
            HtmlDocument dom, 
            Selector.Collector<D> collector, 
            D outputIfNone) {
        final List metaTags = dom.getMetaTags();
        final D result;
        if(metaTags.isEmpty()) {
            result = null;
        }else{
            result = (D)metaTagSelector.select(metaTags.iterator(), ArticleMetaNames.values(), collector);
        }
        return result == null ? outputIfNone : result;
    }

    public void updateFeed(Feed feed, HtmlDocument dom) {
        final Metadata metaData = this.parseMetadata(dom);
        this.updateFeed(feed, dom, metaData);
    }
    
    public void updateFeed(Feed feed, HtmlDocument dom, Metadata metaData) {
        
        this.updateFeedWithMetaData(feed, dom, metaData);
        
        if(!this.hasEnoughData(feed)) {

            final Map<String, String> extract = nodeExtractor.extract(dom);
            
            this.updateFeedWithExtract(feed, dom, extract);
            
        }else{
            
            if(this.isNullOrEmpty(feed.getTitle())) {
                
                this.updateTitle(dom, Feed_.title.getName(), feed, 
                        metaData, Collections.EMPTY_MAP, 
                        feed.getDescription(), feed.getContent());
            }
        }
    }

    public boolean hasEnoughData(Feed feed) {
        return (feed.getFeeddate() != null || feed.getDatecreated() != null || feed.getTimemodified() != null) &&
                this.isAnyNotNullOrEmpty(feed.getDescription(), feed.getContent(), feed.getTitle());
    }
    
    public boolean hasEnoughData(Metadata metadata) {
        return !this.isNullOrEmpty(metadata.getValue(dateNames, null)) && 
                (!this.isNullOrEmpty(metadata.getValue(ArticleMetaNames.DESCRIPTION, null)) || 
                !this.isNullOrEmpty(metadata.getValue(ArticleMetaNames.CONTENT, null)) ||
                !this.isNullOrEmpty(metadata.getValue(ArticleMetaNames.TITLE, null))); 
    }
    
    public boolean isAnyNotNullOrEmpty(Object... values) {
        for(Object value : values) {
            if(!this.isNullOrEmpty((String)value)) {
                return true;
            }
        }
        return false;
    }

    public int updateFeedWithMetaData(Feed feed, HtmlDocument dom, Metadata metaData) {
        return this.updateFeed(feed, dom, metaData, Collections.EMPTY_MAP);
    }
    
    public int updateFeedWithExtract(Feed feed, HtmlDocument dom, Map<String, String> extract) {
        return this.updateFeed(feed, dom, Metadata.EMPTY, extract);
    }
    
    public int updateFeed(Feed feed, HtmlDocument dom, 
            Metadata metaData, Map<String, String> extract) {
        
        int updateCount = 0;
        
        final Level level = Level.FINER;

        final Site site = this.getSite();
        feed.setSiteid(site);
        
        if(this.updateAuthor(dom, Feed_.author.getName(), feed, metaData, extract)) {
            ++updateCount;
        }
        
////////////// Categories 
        String categories = getValue(Feed_.categories.getName(), true, 
                metaData.combineValues(ArticleMetaNames.CATEGORY_SET, null),
                extract.get(Feed_.categories.getName()));
        if(this.isNullOrEmpty(categories)) {
            categories = this.getDefaultCategories();
        }
        if(!this.isNullOrEmpty(categories)){
            ++updateCount;
            feed.setCategories(categories);
        }
        
////////////// Content        
        final String content = getValue(Feed_.content.getName(), false,
                metaData.getValue(ArticleMetaNames.CONTENT, null),
                extract.get(Feed_.content.getName()));
        
        if(!this.isNullOrEmpty(content)) {
            ++updateCount;
            feed.setContent(content);
        }
        
        final int contentLength = content == null ? 0 : content.length();
        final int recommendedSize = this.getRecommendedSize("content");
        if(contentLength > recommendedSize) {
            LOG.log(Level.WARNING, "For site: {0}, found content length: {1} > recommended: {2}", 
            new Object[]{this.getSite()==null?null:this.getSite().getSite(), contentLength, recommendedSize});
        }
        
////////////// Datecreated
        final String datecreatedStr = extract.get("datecreated");
        Date datecreated = this.isNullOrEmpty(datecreatedStr) ? null : this.getDate(datecreatedStr, null);
        if(datecreated == null) {
            final String dateStr = metaData.getValue(ArticleMetaNames.DATE_CREATED, null);
            if(!this.isNullOrEmpty(dateStr)) {
                datecreated = this.dateExtractor.extract(dateStr, null);
            }
        }
        if(datecreated != null) {
            ++updateCount;
        }
        feed.setDatecreated(datecreated);

////////////// Description        
        final String description = getValue(Feed_.description.getName(), true,
                metaData.getValue(ArticleMetaNames.DESCRIPTION, null),
                extract.get(Feed_.description.getName()));
        
        if(!this.isNullOrEmpty(description)) {
            ++updateCount;
        }
        feed.setDescription(description);
        
        if(LOG.isLoggable(level)) {       
        LOG.log(level, "Description. {0} = {1}", new Object[]{extract.get("description"), feed.getDescription()});
        }        
        if(this.isNullOrEmpty(feed.getContent()) && !this.isNullOrEmpty(feed.getDescription())) {
            feed.setContent(feed.getDescription());
            feed.setDescription(null);
        }
        
////////////// Feeddate
        final String feeddateStr = extract.get("feeddate");
        Date feeddate = this.isNullOrEmpty(feeddateStr) ? null : this.getDate(feeddateStr, null);
        if(feeddate == null) {
            final String dateStr = metaData.getValue(dateNames, null);
            if(!this.isNullOrEmpty(dateStr)) {
                feeddate = this.dateExtractor.extract(dateStr, null);
            }
            if(feeddate == null) {
                feeddate = this.dateFromUrlExtractor.extract(dom.getURL(), null);
            }
        }
        if(feeddate == null) {
            if(LOG.isLoggable(level)) {
                LOG.log(level, "Feeddate could not be extracted for feed:: site: {0}, title: {1}, author: {2}", 
                        new Object[]{this.getSite().getSite(), feed.getTitle(), feed.getAuthor()});
            }
        }else{
            if(feeddate.after(NOW)) {
                feeddate = NOW;
            }
        }    
        if(feeddate != null) {
            ++updateCount;
        }
        feed.setFeeddate(feeddate);

        if(LOG.isLoggable(Level.FINER)) {        
            LOG.log(Level.FINER, "Feeddate. {0} = {1}", new Object[]{feeddateStr, feeddate});
        }

////////////// Imageurl
        String imageurl = extract.get("imageurl");
        LOG.log(Level.FINER, "Extracted image url: {0}", imageurl);        

        if(!this.isNullOrEmpty(imageurl)) {
            if(!imageurl.startsWith("http://")) {
                final String baseUrl = this.getConfig().getString(ConfigName.url, "value");
                if(baseUrl.endsWith("/")) {
                    if(imageurl.startsWith("/")) {
                        imageurl = baseUrl + imageurl.substring(1);
                    }else{
                        imageurl = baseUrl + imageurl;
                    }
                }else{
                    if(imageurl.startsWith("/")) {
                        imageurl = baseUrl + imageurl;
                    }else{
                        imageurl = baseUrl + '/' + imageurl;
                    }
                }
            }
            Filter<String> filter = ((ImageNodeFilter)this.getImagesFilter()).getImageSrcFilter();
            boolean accepted = filter.test(imageurl);
            LOG.log(Level.FINER, "Extracted image url accepted: {0}", accepted);        
            
            if(!accepted) {
                imageurl = null;
            }
        }

        if(this.isNullOrEmpty(imageurl)) {
            final Set<String> imageUrls = metaData.getValues(ArticleMetaNames.IMAGELINK_SET);
            if(imageUrls != null && !imageUrls.isEmpty()) {
                imageurl = this.getImageUrlOfLargestImage(imageUrls);
            }
            if(this.isNullOrEmpty(imageurl)) {
                final NodeList nodeList = dom.getElements();
//                imageurl = this.getFirstImageUrl(nodeList);                
                NodeFilter filter = this.getImagesFilter() != null ? this.getImagesFilter() : null;
                final NodeList imageNodes = filter == null ? null : nodeList.extractAllNodesThatMatch(filter, true);
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
            LOG.log(Level.FINER, "Body content extracted image url: ", imageurl);        
        }
        if(!this.isNullOrEmpty(imageurl)) {
            ++updateCount;
            feed.setImageurl(imageurl);
        }

////////////// Keywords
        String keywords = getValue(Feed_.keywords.getName(), true, 
                metaData.getValue(ArticleMetaNames.KEYWORDS, null), 
                metaData.getAsSingle(ArticleMetaNames.TAG_SET, null),
                this.getKeywords(dom),
                extract.get(Feed_.keywords.getName()));
        if(!this.isNullOrEmpty(keywords)) {
            ++updateCount;
            feed.setKeywords(keywords);
        }
        
////////////// Title
        if(this.updateTitle(dom, Feed_.title.getName(), feed, 
                metaData, extract, description, content)) {
            ++updateCount;
        }
        
        if(this.isNullOrEmpty(feed.getContent()) && !this.isNullOrEmpty(feed.getTitle())) {
            feed.setContent(feed.getTitle());
//            feed.setTitle(null);
        }
        
        // We use this not the formatted value so 
        // that we can check for duplicates in the future
        // using methods like isInDatabase(String link)
        feed.setUrl(dom.getURL()); 
        ++updateCount;
        
//if(true) {
//System.out.println("@"+this.getClass().getName());
//System.out.println("URL: "+feed.getUrl());
//System.out.println("Feed date: "+feed.getFeeddate());
//System.out.println("Title: "+feed.getTitle());
//System.out.println("Extract: "+extract);
//}     
        return updateCount;
    }
    
    public boolean updateAuthor(HtmlDocument dom, String col, Feed feed, 
            Metadata metaData, Map<String, String> extract) {
        
////////////// Author        
        String author = getValue(col, true, 
                metaData.getValue(ArticleMetaNames.AUTHOR, null), 
                metaData.getValue(ArticleMetaNames.PUBLISHER, null),
                extract.get(col));
        
        if (!this.isNullOrEmpty(author) && (author.startsWith("a target=")) && (author.contains("punch"))) {
            author = "Punch Newspaper";
        }
        if (!this.isNullOrEmpty(author) && (author.startsWith("a href=")) && (author.contains("leadership"))) {
            author = "Leaderhship Newspaper";
        }
        if(this.isNullOrEmpty(author)) {
            author = this.getSite().getSite();
        }
        LOG.log(Level.FINER, "Author. {0} = {1}", new Object[]{extract.get("author"), author});
        if(!this.isNullOrEmpty(author)) {
            feed.setAuthor(author);
        }
        
        return !this.isNullOrEmpty(author);
    }
    
    public boolean updateTitle(HtmlDocument dom, String col, Feed feed, 
            Metadata metadata, Map<String, String> extract, String description, String content) {
        
        return this.updateTitle(dom, col, feed, 
                new String[]{
                    metadata.getValue(ArticleMetaNames.TITLE, null),
                    this.getTitle(dom),
                    extract.get(col),
                    description, content});
    }
    
    public boolean updateTitle(HtmlDocument dom, String col, Feed feed, String... values) {
        
        String title = getValue(col, true, values);
        
        final JsonConfig config = this.getConfig();
        
        if(this.isNullOrEmpty(title)) {
            Boolean titleIsInUrl = getBoolean(config, ConfigName.isTitleInUrl);
            if(titleIsInUrl != null && titleIsInUrl) {
                final TextParser<String> textParser = this.getTitleFromUrlExtractor();
                title = textParser == null ? null : textParser.extract(dom.getURL(), null);
            }
        }
        if(this.isNullOrEmpty(title)) {
            title = (String)defaults.get(col);
        }
        if(!this.isNullOrEmpty(title)) {
            feed.setTitle(title);
            return true;
        }else{
            return false;
        }
    }
    
  public Date getDate(String dateStr, Date defaultIfNone) {

    String [] datePatterns = nodeExtractor.getContext().getNodeExtractorConfig().getDatePatterns();
    
    return this.getDate(datePatterns, dateStr, defaultIfNone);
  }
    
    private String getValue(String col, boolean plainTextOnly, String... values) {
        
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
            
            val = (String)defaults.get(col);
        }
        
        return val;
    }
    
    private final List<String> sort = new ArrayList();
    private String getImageUrlOfLargestImage(Collection<String> imageUrls) {
      sort.clear();
      sort.addAll(imageUrls);
      Collections.sort(sort, imageSizeComparator);
      final String output = sort.get(sort.size() - 1);
      sort.clear();
      return output;
    }  

    public JsonConfig getConfig() {
        final CapturerContext context = nodeExtractor.getContext();
        final JsonConfig config = context.getConfig();
        return config;
    }

    public boolean isNullOrEmpty(Object oval) {
        return oval == null || oval.toString().isEmpty();
    }

    public boolean isNullOrEmpty(String sval) {
        return sval == null || sval.isEmpty();
    }

    public final com.bc.webdatex.extractors.Extractor<String, Date> getDateFromUrlExtractor() {
        return dateFromUrlExtractor;
    }

    public final WebFeedDataExtractor getNodeExtractor() {
        return nodeExtractor;
    }
  
    public Date getDate(Feed feed) {
        return feed.getFeeddate() != null ? feed.getFeeddate() :
                feed.getTimemodified() != null ? feed.getTimemodified() :
                feed.getDatecreated() != null ? feed.getDatecreated() : null;
                
    }

    public String toString(Feed feed) {
        return MessageFormat.format(
            "Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: {4}", 
            feed.getSiteid() == null ? null : feed.getSiteid().getSite(), 
            feed.getAuthor(), feed.getTitle(), feed.getUrl(), feed.getImageurl());
    }
}
