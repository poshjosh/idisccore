package com.idisc.core.twitter;

import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.web.WebFeedCreator;
import com.idisc.core.IdiscApp;
import com.idisc.core.TaskHasResult;
import com.idisc.core.util.Util;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.ResumableUrlParser;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.tag.Link;
import com.scrapper.util.PageNodes;
import com.scrapper.util.PageNodesImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
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
import com.bc.jpa.JpaContext;

public class TwitterFeedTask implements Serializable, TaskHasResult<Collection<Feed>>{
    
  private final Collection<Feed> result;
  private final Sitetype timeline;
  private final Sitetype trending;
  private final EnumReferences refs;
  private final float tolerance;
  private WebFeedCreator _fc;
  private ResumableUrlParser _parser_no_direct_access;
  
  public TwitterFeedTask() {
    this.result = Collections.synchronizedCollection(new ArrayList());
    this.refs = getControllerFactory().getEnumReferences();
    this.timeline = ((Sitetype)this.refs.getEntity(References.sitetype.timeline));
    this.trending = ((Sitetype)this.refs.getEntity(References.sitetype.trend));
    Configuration config = IdiscApp.getInstance().getConfiguration();
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
  }

  @Override
  public Collection<Feed> call() throws TwitterException {
    this.doRun();
    return this.getResult();
  }
  
  @Override
  public void run() {
      
    try {
        
      this.doRun();
        
    }catch (TwitterException e) {
      XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
    }catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
    }
  }
  
  protected void doRun() throws TwitterException {
      
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
  }
  
  private long getNewestTweetId() {
      
    EntityController<Feed, Integer> ec = getControllerFactory().getEntityController(Feed.class, Integer.class);
    
    Map params = Collections.singletonMap("categories", "statuses");
    
    Map<String, String> orderBy = Collections.singletonMap("feeddate", "DESC");
    
    List<Feed> feeds = ec.select(params, orderBy, 0, 1);
    
    if ((feeds == null) || (feeds.isEmpty())) {
      return -1L;
    }
    String rawId = ((Feed)feeds.get(0)).getRawid();
    if (rawId == null) {
      return -1L;
    }
    return Long.parseLong(rawId);
  }
  
  private void addTimeline(List<Status> statuses) {
      
    if ((statuses == null) || (statuses.isEmpty())) {
      return;
    }
    
    NodeFilter filter0 = new NodeClassFilter(ImageTag.class);
    NodeFilter filter1 = new TagNameFilter("IMG");
    NodeFilter imagesFilter = new OrFilter(new NodeFilter[] { filter0, filter1 });
    
    Date datecreated = new Date();
    
    for (Status status : statuses)
    {
      Feed feed = new Feed();
      
      Site site = Util.findSite("twitter", this.timeline, true);
      
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
              
            updatedWithDirectContents = updateFeedWithDirectContent(feed, expandedUrl, imagesFilter, datecreated);
          }
          
          if (updatedWithDirectContents) {
            break;
          }
        }
      }
      
      String imageUrl = null;
      MediaEntity[] mediaEntities = status.getMediaEntities();
      if (mediaEntities != null) {
        for (MediaEntity media : mediaEntities)
        {
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
      




      if (feed.getUrl() == null)
      {
        feed.setUrl(link);
      }
      
      if (feed.getAuthor() == null)
      {
        String userName = user == null ? null : user.getName();
        
        feed.setAuthor(Util.truncate(Feed.class, "author", userName));
      }
      


      if (!updatedWithDirectContents) {
        feed.setCategories("statuses");
      }
      




      if (feed.getFeeddate() == null) {
        feed.setFeeddate(status.getCreatedAt() == null ? new Date() : status.getCreatedAt());
      }
      
      feed.setRawid("" + status.getId());
      
      if (!updatedWithDirectContents) {
        feed.setContent(Util.truncate(Feed.class, "content", status.getText()));
        feed.setKeywords(null);
      }
      
      synchronized (this.result)
      {
        XLogger.getInstance().log(Level.INFO, "Adding Twitter Status Feed. Site: {0}, author: {1}, title: {2}\nURL: {3}", getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
        

        this.result.add(feed);
      }
    }
  }
  
  private boolean updateFeedWithDirectContent(Feed feed, String link, NodeFilter imagesFilter, Date datecreated)
  {
    try
    {
      ScrapperConfigFactory cf = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
      
      Set<String> sites = cf.getSitenames();
      
      String selectedSite = null;
      for (String site : sites)
        if (!site.equalsIgnoreCase("default"))
        {

          JsonConfig cfg = cf.getConfig(site);
          String baseUrl = cfg.getString(new Object[] { "url", "value" });
          
          if (link.startsWith(baseUrl)) {
            selectedSite = site;
            break;
          }
        }
      XLogger.getInstance().log(Level.FINER, "Selected site: {0} for link: {1}", getClass(), selectedSite, link);
      
      if (selectedSite == null)
      {
        return false;
      }
      
      Sitetype webtype = (Sitetype)this.refs.getEntity(References.sitetype.web);
      
      Site site = Util.findSite(selectedSite, webtype, false);
      
      if (site == null) {
        throw new NullPointerException();
      }
      
      ResumableUrlParser parser = getParser();
      
      parser.setSitename(site.getSite());
      
      NodeList nodeList = parser.parse(link);
      
      PageNodes pageNodes = new PageNodesImpl(link, nodeList);
      
      WebFeedCreator fc = getFeedCreator();
      fc.setSite(site);
      fc.setImagesFilter(imagesFilter);
      fc.setTolerance(this.tolerance);
      
      fc.updateFeed(feed, pageNodes, datecreated);
      
      return true;
    }
    catch (ParserException e) {}
    
    return false;
  }
  

  private void updateFeedWith(Site site, Feed feed, PageNodes pageNodes)
  {
    feed.setSiteid(site);
    
    String sitename = site.getSite();
    





    feed.setAuthor(sitename);
    
    feed.setCategories("status news");
    
    String content = pageNodes.getBody().getChildren().toHtml();
    


    feed.setContent(Util.truncate(Feed.class, "content", content));
    


    if (pageNodes.getDescription() != null) {
      MetaTag meta = pageNodes.getDescription();
      feed.setDescription(Util.truncate(Feed.class, "description", meta.getAttribute("content")));
    }
    


    Link link = pageNodes.getIcon();
    if (link == null) {
      link = pageNodes.getIco();
    }
    if (link != null) {
      feed.setImageurl(link.getLink());
    }
    
    if (pageNodes.getKeywords() != null) {
      MetaTag meta = pageNodes.getKeywords();
      feed.setKeywords(Util.truncate(Feed.class, "keywords", meta.getAttribute("content")));
    }
    
    if (pageNodes.getTitle() != null) {
      TitleTag title = pageNodes.getTitle();
      feed.setTitle(title.toPlainTextString());
    }
    



    feed.setUrl(pageNodes.getURL());
  }
  
  private void addTrends(List<Trend> trends)
  {
    if ((trends == null) || (trends.isEmpty())) {
      return;
    }
    
    for (Trend trend : trends)
    {
      Feed feed = new Feed();
      feed.setAuthor("twitter trends");
      

      feed.setCategories("trending");
      feed.setContent(trend.getName());
      

      feed.setFeeddate(new Date());
      Site site = Util.findSite("twitter", this.trending, true);
      feed.setSiteid(site);
      feed.setTitle(trend.getName());
      feed.setUrl(trend.getURL());
      
      synchronized (this.result)
      {
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
  
  private WebFeedCreator getFeedCreator()
  {
    if (this._fc == null) {
      this._fc = new WebFeedCreator();
    }
    return this._fc;
  }
  
  private ResumableUrlParser getParser()
  {
    if (this._parser_no_direct_access == null) {
      this._parser_no_direct_access = new ResumableUrlParser() {
        private transient Feed feedToFind;
        private EntityController<Feed, Integer> ec_accessViaGetter;
        
        public boolean isResumable() { return false; }
        
        public boolean isResume()
        {
          return true;
        }
        
        public boolean isInDatabase(String link) {
          if (this.feedToFind == null) {
            this.feedToFind = new Feed();
          }
          this.feedToFind.setUrl(link);
          EntityController<Feed, Integer> ec = getFeedController();
          Map map = ec.toMap(this.feedToFind, false);
          return ec.selectFirst(map) != null;
        }
        
        private EntityController<Feed, Integer> getFeedController() {
          if (this.ec_accessViaGetter == null) {
            this.ec_accessViaGetter = TwitterFeedTask.this.getControllerFactory().getEntityController(Feed.class, Integer.class);
          }
          return this.ec_accessViaGetter;
        }
      };
    }
    return this._parser_no_direct_access;
  }
  
  private JpaContext getControllerFactory() {
    return IdiscApp.getInstance().getJpaContext();
  }
  
  public Collection<Feed> getResult()
  {
    return this.result;
  }
}
