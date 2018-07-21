package com.deprecated.twitter;

import com.bc.jpa.context.JpaContext;
import com.bc.jpa.dao.Criteria;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import com.bc.task.AbstractStoppableTask;
import java.util.logging.Logger;
import com.bc.webdatex.converters.DateTimeConverter;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.extraction.web.WebFeedCreator;
import com.idisc.pu.References;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Feed_;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.ParserException;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.TwitterException;
import twitter4j.URLEntity;
import twitter4j.User;
import java.util.TimeZone;
import org.htmlparser.dom.HtmlDocument;
import com.bc.webcrawler.ResumeHandler;
import com.bc.webcrawler.UrlParser;
import com.idisc.core.extraction.UrlParserImpl;
import com.bc.webdatex.context.CapturerContextFactory;

public class TwitterFeedTask extends AbstractStoppableTask<Collection<Feed>> implements Serializable {
    private transient static final Logger LOG = Logger.getLogger(TwitterFeedTask.class.getName());
    
  private final class ResumableUrlParserImpl implements ResumeHandler, AutoCloseable {
    private EntityManager entityManager;
    public ResumableUrlParserImpl() { }
    @Override
    public boolean isExisting(String name) {
        return this.isInDatabase(name);
    }
    @Override
    public boolean saveIfNotExists(String name) {
        if(true) throw new UnsupportedOperationException("Why not save????");
        return false;
    }
    @Override
    public void close() {
      if(this.entityManager != null && this.entityManager.isOpen()) {
        this.entityManager.close();
      }
    }
    public boolean isInDatabase(String link) {
        if(entityManager == null) {
          entityManager = getJpaContext().getEntityManager(Feed.class);
        }
        boolean found;
        try{
          TypedQuery<String> query = entityManager.createQuery("SELECT f.url FROM Feed f WHERE f.url = :url", String.class);
          query.setParameter("url", link);
          query.setFirstResult(0);
          query.setMaxResults(1);
          found = query.getSingleResult() != null;
        }catch(NoResultException ignored) {
            found = false;
        }
        if (found) {
          if(LOG.isLoggable(Level.FINER)){
               LOG.log(Level.FINER, "Link is already in database: {0}", link);
          }
        }
        return found;
    }
  }
    
  private final Collection<Feed> result;
  private final Sitetype timeline;
  private final Sitetype trending;
  private final EnumReferences refs;
  private final float tolerance;

  public TwitterFeedTask() {
    this.result = Collections.synchronizedCollection(new ArrayList());
    this.refs = getJpaContext().getEnumReferences();
    this.timeline = ((Sitetype)this.refs.getEntity(References.sitetype.timeline));
    this.trending = ((Sitetype)this.refs.getEntity(References.sitetype.trend));
    Configuration config = IdiscApp.getInstance().getConfiguration();
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
  }

    @Override
  public String getTaskName() {
    return this.getClass().getName();
  }

  @Override
  protected Collection<Feed> doCall() throws TwitterException {
      
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Running {0} :: {1}",new Object[]{ getClass().getSimpleName(),  this});
      }
    
      TwitterClient twr = new TwitterClient();
      
      final long sinceId = getNewestTweetId();
      
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Newest tweet id: {0}", sinceId);
      }
      
      ResponseList<Status> timeLine;
      
      if (sinceId == -1L) {
        timeLine = twr.getTwitter().getHomeTimeline();
      } else {
        Paging paging = new Paging();
        paging.setSinceId(sinceId);
        timeLine = twr.getTwitter().getHomeTimeline(paging);
      }

      if(LOG.isLoggable(Level.FINE)){
         LOG.log(Level.FINE, "Twitter timeline count: {0}", timeLine == null ? null : Integer.valueOf(timeLine.size()));
      }
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Twitter timeline: {0}", timeLine);
      }
      
      if ((timeLine != null) && (!timeLine.isEmpty())) {
        addTimeline(timeLine);
      }
      
      return this.result;
  }
  
  private long getNewestTweetId() {
      
    Feed newest;
    try{
      newest = this.getJpaContext().getDaoForSelect(Feed.class)
          .where(Feed.class, Feed_.categories.getName(), Criteria.EQ, "statuses")
          .descOrder(Feed_.feeddate.getName()).getSingleResultAndClose();
    }catch(NoResultException ignored) {
      newest = null;  
    }
    
    long newestTweetId;
    
    if (newest == null) {
      newestTweetId = -1L;
    }else{
      String rawId = newest.getRawid();
      newestTweetId = rawId == null ? -1L : Long.parseLong(rawId);
    }
    
    return newestTweetId;
  }
  
  private void addTimeline(List<Status> statuses) {
      
    if (statuses == null || statuses.isEmpty()) {
      return;
    }
    
    final JpaContext jpaContext = this.getJpaContext();    
    
    Site site = new SiteDao(jpaContext).from("twitter", this.timeline, true);
    
    NodeFilter imagesFilter = new ImageNodeFilter((String)null);
    
    WebFeedCreator webFeedCreator = new WebFeedCreator(site, imagesFilter, this.tolerance);
    
    DateTimeConverter dateTimeZoneConverter =
            new DateTimeConverter(TimeZone.getDefault(), webFeedCreator.getOutputTimeZone());
    
    final Date NOW = new Date();
    
    final UrlParserImpl urlParser = new UrlParserImpl();
    
    if(true) throw new UnsupportedOperationException("This is a faulty implementation. The UrlParser should have a ResumeHandler. Also we should be using TwitterFeedTaskProvider as is the case with WebFeedTaskProvider");
    
    try{
        
        for (Status status : statuses) {
            
          Feed feed = new Feed();

          feed.setSiteid(site);

          boolean updatedWithDirectContents = false;

          String link = null;

          URLEntity[] entities = status.getURLEntities();

          if ((entities != null) && (entities.length != 0)) {

            for (URLEntity entity : entities) {

              String expandedUrl = entity.getExpandedURL();

              if(LOG.isLoggable(Level.INFO)){
                     LOG.log(Level.INFO, "URL: {0}\nExpanded URL: {1}",new Object[]{ entity.getURL(),  expandedUrl});
              }

              link = expandedUrl != null ? expandedUrl : entity.getURL();

              if ((expandedUrl != null) && (!link.toLowerCase().contains("hootsuite"))) {

                updatedWithDirectContents = updateFeedWithDirectContent(
                        feed, expandedUrl, urlParser, webFeedCreator);
              }

              if (updatedWithDirectContents) {
                break;
              }
            }
          }

          String imageUrl = null;

          MediaEntity[] mediaEntities = status.getMediaEntities();

          if (mediaEntities != null) {

            for (MediaEntity media : mediaEntities) {

              if(LOG.isLoggable(Level.INFO)){
                     LOG.log(Level.INFO, "Media URL: {0}\nExpanded URL: {1}\nURL: {2}",new Object[]{ media.getMediaURL(),  media.getExpandedURL(),  media.getURL()});
              }

              imageUrl = media.getMediaURL() == null ? media.getMediaURLHttps() : media.getMediaURL();

              if (imageUrl != null) {
                break;
              }
            }
          }

          User user = status.getUser();

          if ((imageUrl == null) && (user != null)) {
            imageUrl = user.getMiniProfileImageURL();
            if (imageUrl == null) {
              imageUrl = user.getProfileImageURL();
            }
          }
          feed.setImageurl(imageUrl);

          if (link == null) {
            link = getHrefFromHtmlLink(status.getSource());
          }

          if (feed.getUrl() == null){
            feed.setUrl(link);
          }

          if (feed.getAuthor() == null)  {
            String userName = user == null ? null : user.getName();
            feed.setAuthor(webFeedCreator.format("author", userName, false));
          }

          if (!updatedWithDirectContents) {
            feed.setCategories("statuses");
          }

          if (feed.getFeeddate() == null) {
            Date createdAt = status.getCreatedAt();
            if(createdAt != null && createdAt.after(NOW)) {
              createdAt = NOW;
            }
            feed.setFeeddate(createdAt == null ? NOW : 
                    dateTimeZoneConverter.convert(createdAt));
          }

          feed.setRawid("" + status.getId());

          if (!updatedWithDirectContents) {
            feed.setContent(webFeedCreator.format("content", status.getText(), false));
            feed.setKeywords(null);
          }

          synchronized (this.result){
            if(LOG.isLoggable(Level.INFO)){
                  LOG.log(Level.INFO, "Adding Twitter Status Feed. Site: {0}, author: {1}, title: {2}\\nURL: {3}",  
                          new Object[]{feed.getSiteid() == null ? null : feed.getSiteid().getSite(),  feed.getAuthor(),  feed.getTitle(),  feed.getUrl()});
            }
            this.result.add(feed);
          }
        }
    }finally{
        
    }
  }
  
  private boolean updateFeedWithDirectContent(
      Feed feed, String link, UrlParser<HtmlDocument> parser, 
      WebFeedCreator webFeedCreator) {
      
    try {
        
      final CapturerContextFactory contextFactory = IdiscApp.getInstance().getScrapperContextFactory();
      
      final List<String> sites = contextFactory.getConfigService().getConfigNames();
      
      String selectedSite = null;
      
      for (String site : sites) { 
          
        if (!site.equalsIgnoreCase("default")) {

          JsonConfig cfg = contextFactory.getContext(site).getConfig();
          String baseUrl = cfg.getString(new Object[] { "url", "value" });
          
          if (link.startsWith(baseUrl)) {
            selectedSite = site;
            break;
          }
        }
      }
      
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Selected site: {0} for link: {1}",new Object[]{ selectedSite,  link});
      }
      
      if (selectedSite == null) {
        return false;
      }
      
      Sitetype webtype = (Sitetype)this.refs.getEntity(References.sitetype.web);
      
      JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
      
      Site site = new SiteDao(jpaContext).from(selectedSite, webtype, false);
      
      Objects.requireNonNull(site);
      
      final HtmlDocument pageNodes = parser.parse(link);
      
      webFeedCreator.updateFeed(feed, pageNodes);
      
      return true;
      
    }catch (Exception e) {
      if(LOG.isLoggable(Level.WARNING)){
         LOG.log(Level.WARNING, "Exception parsing: "+link, e);
      }
    }
    
    return false;
  }
  
  private void addTrends(List<Trend> trends) {
      
    if ((trends == null) || (trends.isEmpty())) {
      return;
    }
    
    final JpaContext jpaContext = this.getJpaContext();
    
    final Site site = new SiteDao(jpaContext).from("twitter", this.trending, true);
    
    for (Trend trend : trends) {
        
      Feed feed = new Feed();
      feed.setAuthor("twitter trends");
      

      feed.setCategories("trending");
      feed.setContent(trend.getName());
      

      feed.setFeeddate(new Date());
      feed.setSiteid(site);
      feed.setTitle(trend.getName());
      feed.setUrl(trend.getURL());
      
      synchronized (this.result)  {
        if(LOG.isLoggable(Level.INFO)){
            LOG.log(Level.INFO, "Adding Twitter Trend Feed. Site: {0}, author: {1}, title: {2}\\nURL: {3}",  
                    new Object[]{ feed.getSiteid() == null ? null : feed.getSiteid().getSite(),  feed.getAuthor(),  feed.getTitle(),  feed.getUrl()});
        }
        this.result.add(feed);
      }
    }
  }
  
  private String getHrefFromHtmlLink(String htmlLinkTag) {
    String output = null;
    if ((htmlLinkTag != null) && (!htmlLinkTag.isEmpty())) {
      Parser parser = new Parser();
      try {
        parser.setInputHTML(htmlLinkTag);
        HtmlDocument nodes = parser.parse(null);
        if ((nodes != null) && (!nodes.isEmpty()))
        {
          Node first = (Node)nodes.get(0);
          if ((first instanceof LinkTag)) {
            output = ((LinkTag)first).getLink();
          }
        }
      } catch (ParserException e) {
        if(LOG.isLoggable(Level.WARNING)){
            LOG.log(Level.WARNING, "Failed to extract href attribute from: " + htmlLinkTag, e);
        }
      }
    }
    
    return output;
  }
  
  private JpaContext getJpaContext() {
    return IdiscApp.getInstance().getJpaContext();
  }
}
