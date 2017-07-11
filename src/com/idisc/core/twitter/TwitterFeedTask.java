package com.idisc.core.twitter;

import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.Criteria;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.idisc.pu.entities.Feed;
import com.scrapper.ResumableUrlParser;
import java.io.Serializable;
import java.util.Collection;
import java.util.logging.Level;
import com.bc.task.AbstractStoppableTask;
import com.bc.util.XLogger;
import com.bc.webdatex.converter.DateTimeConverter;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.filter.ImageNodeFilter;
import com.idisc.core.web.WebFeedCreator;
import com.idisc.pu.References;
import com.idisc.pu.SiteService;
import com.idisc.pu.entities.Feed_;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.config.ScrapperConfigFactory;
import com.bc.dom.HtmlPageDomImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
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
import com.bc.dom.HtmlPageDom;

public class TwitterFeedTask extends AbstractStoppableTask<Collection<Feed>> implements Serializable {
    
  private final class ResumableUrlParserImpl extends ResumableUrlParser implements AutoCloseable {
    private EntityManager entityManager;
    public ResumableUrlParserImpl() {
      super(new ArrayList(), false, true);
    }
    @Override
    protected void post() {
      super.post();
      this.close();
    }
    @Override
    public void close() {
      if(this.entityManager != null && this.entityManager.isOpen()) {
        this.entityManager.close();
      }
    }
    @Override
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
          XLogger.getInstance().log(Level.FINER, "Link is already in database: {0}", this.getClass(), link);
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
      
      XLogger.getInstance().log(Level.FINER, "Running {0} :: {1}", getClass(), getClass().getSimpleName(), this);
    
      TwitterClient twr = new TwitterClient();
      
      final long sinceId = getNewestTweetId();
      
      XLogger.getInstance().log(Level.FINER, "Newest tweet id: {0}", getClass(), sinceId);
      
      ResponseList<Status> timeLine;
      
      if (sinceId == -1L) {
        timeLine = twr.getTwitter().getHomeTimeline();
      } else {
        Paging paging = new Paging();
        paging.setSinceId(sinceId);
        timeLine = twr.getTwitter().getHomeTimeline(paging);
      }

      XLogger.getInstance().log(Level.FINE, "Twitter timeline count: {0}", getClass(), timeLine == null ? null : Integer.valueOf(timeLine.size()));
      XLogger.getInstance().log(Level.FINER, "Twitter timeline: {0}", getClass(), timeLine);
      
      if ((timeLine != null) && (!timeLine.isEmpty())) {
        addTimeline(timeLine);
      }
      
      return this.result;
  }
  
  private long getNewestTweetId() {
      
    Feed newest;
    try{
      newest = this.getJpaContext().getBuilderForSelect(Feed.class)
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
    
    Site site = new SiteService(jpaContext).from("twitter", this.timeline, true);
    
    NodeFilter imagesFilter = new ImageNodeFilter((String)null);
    
    WebFeedCreator webFeedCreator = new WebFeedCreator(site, imagesFilter, this.tolerance);
    
    DateTimeConverter dateTimeZoneConverter =
            new DateTimeConverter(TimeZone.getDefault(), webFeedCreator.getOutputTimeZone());
    
    final Date NOW = new Date();
    
    try(ResumableUrlParserImpl urlParser = new ResumableUrlParserImpl()){
        
        for (Status status : statuses) {
            
          Feed feed = new Feed();

          feed.setSiteid(site);

          boolean updatedWithDirectContents = false;

          String link = null;

          URLEntity[] entities = status.getURLEntities();

          if ((entities != null) && (entities.length != 0)) {

            for (URLEntity entity : entities) {

              String expandedUrl = entity.getExpandedURL();

              XLogger.getInstance().log(Level.INFO, "URL: {0}\nExpanded URL: {1}", getClass(), entity.getURL(), expandedUrl);

              link = expandedUrl != null ? expandedUrl : entity.getURL();

              if ((expandedUrl != null) && (!link.toLowerCase().contains("hootsuite"))) {

                updatedWithDirectContents = updateFeedWithDirectContent(
                        feed, expandedUrl, urlParser, webFeedCreator, null);
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

              XLogger.getInstance().log(Level.INFO, "Media URL: {0}\nExpanded URL: {1}\nURL: {2}", getClass(), media.getMediaURL(), media.getExpandedURL(), media.getURL());

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
            XLogger.getInstance().log(Level.INFO, "Adding Twitter Status Feed. Site: {0}, author: {1}, title: {2}\nURL: {3}", getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
            this.result.add(feed);
          }
        }
        
    }
  }
  
  private boolean updateFeedWithDirectContent(
      Feed feed, String link, ResumableUrlParser parser, 
      WebFeedCreator webFeedCreator, Date datecreated) {
      
    try {
        
      ScrapperConfigFactory cf = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
      
      Set<String> sites = cf.getSitenames();
      
      String selectedSite = null;
      
      for (String site : sites) { 
          
        if (!site.equalsIgnoreCase("default")) {

          JsonConfig cfg = cf.getContext(site).getConfig();
          String baseUrl = cfg.getString(new Object[] { "url", "value" });
          
          if (link.startsWith(baseUrl)) {
            selectedSite = site;
            break;
          }
        }
      }
      
      XLogger.getInstance().log(Level.FINER, "Selected site: {0} for link: {1}", getClass(), selectedSite, link);
      
      if (selectedSite == null) {
        return false;
      }
      
      Sitetype webtype = (Sitetype)this.refs.getEntity(References.sitetype.web);
      
      JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
      
      Site site = new SiteService(jpaContext).from(selectedSite, webtype, false);
      
      Objects.requireNonNull(site);
      
      NodeList nodeList = parser.parse(link);
      
      HtmlPageDom pageNodes = new HtmlPageDomImpl(link, nodeList);
      
      webFeedCreator.updateFeed(feed, pageNodes, datecreated);
      
      return true;
      
    }catch (ParserException e) {
      XLogger.getInstance().log(Level.WARNING, "Exception parsing: "+link, this.getClass(), e);
    }
    
    return false;
  }
  
  private void addTrends(List<Trend> trends) {
      
    if ((trends == null) || (trends.isEmpty())) {
      return;
    }
    
    final JpaContext jpaContext = this.getJpaContext();
    
    final Site site = new SiteService(jpaContext).from("twitter", this.trending, true);
    
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
        XLogger.getInstance().log(Level.INFO, "Adding Twitter Trend Feed. Site: {0}, author: {1}, title: {2}\nURL: {3}", getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
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
        NodeList nodes = parser.parse(null);
        if ((nodes != null) && (!nodes.isEmpty()))
        {
          Node first = (Node)nodes.get(0);
          if ((first instanceof LinkTag)) {
            output = ((LinkTag)first).getLink();
          }
        }
      } catch (ParserException e) {
        XLogger.getInstance().log(Level.WARNING, "Failed to extract href attribute from: " + htmlLinkTag, getClass(), e);
      }
    }
    
    return output;
  }
  
  private JpaContext getJpaContext() {
    return IdiscApp.getInstance().getJpaContext();
  }
}
