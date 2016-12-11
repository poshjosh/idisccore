package com.idisc.core.web;

import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.Crawler;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;
import com.bc.jpa.JpaContext;
import com.bc.webdatex.filter.ImageNodeFilter;
import javax.persistence.NoResultException;
import com.idisc.pu.entities.Feed_;
import com.bc.dom.HtmlPageDom;
import com.idisc.core.FeedHandler;

public class NewsCrawler extends Crawler<Integer> {
    
  private transient static final Class cls = NewsCrawler.class;
  private transient static final XLogger logger = XLogger.getInstance();
  
  private final float tolerance;
  private final Sitetype sitetype;
  
  private final long timeoutMillis;
  private final int maxFailsAllowed;
  
  private int scrapped = 0;
  
  private final int scrappLimit;
  
  private final FeedHandler feedHandler;

  private EntityManager entityManager;

  public NewsCrawler(
          JsonConfig config, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, FeedHandler feedHandler) {
      
    this(
            IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), 
            timeout, timeoutUnit, maxFailsAllowed, feedHandler, false, true
    );
  }
  
  public NewsCrawler(
          JsonConfig config, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, FeedHandler feedHandler,
          boolean resumable, boolean toResume) {
      
    this(
            IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), 
            timeout, timeoutUnit, maxFailsAllowed, feedHandler, resumable, toResume
    );
  }

  public NewsCrawler(
          CapturerContext context, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, FeedHandler feedHandler) {
   
    this(context, timeout, timeoutUnit, maxFailsAllowed, feedHandler, false, true);
  }
  
  public NewsCrawler(
          CapturerContext context, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, FeedHandler feedHandler,
          boolean resumable, boolean toResume) {
      
    super(context, resumable, toResume);
    
    logger.log(Level.FINER, "Creating", cls);
    
    this.feedHandler = feedHandler;
    
    this.timeoutMillis = timeout < 1 || timeoutUnit == null ? 0 : timeoutUnit.toMillis(timeout);
    
    this.maxFailsAllowed = maxFailsAllowed;
    
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
    
    logger.log(Level.FINER, "Tolerance: {0}", cls, this.tolerance);
    
    setParseLimit(getLimit(Config.Extractor.parseLimit));
    setCrawlLimit(getLimit(Config.Extractor.crawlLimit));
    
    scrappLimit = getLimit(Config.Extractor.scrappLimit);
    
    logger.log(Level.FINER, "Updated parse limit: {0}, crawl limit: {1}", cls, getParseLimit(), getCrawlLimit());
    
    EnumReferences refs = getJpaContext().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
    logger.log(Level.FINE, "Done creating: {0}", cls, NewsCrawler.this);
    
    setBatchInterval(0L);
  }

  @Override
  protected void post() {
    super.post();
    if(this.entityManager != null && this.entityManager.isOpen()) {
      this.entityManager.close();
    }
  }
  
  public long getTimeout() {
    return this.timeoutMillis;
  }
    
  public boolean shouldStop() {
    boolean shouldStop = ((this.isRunning() && this.isTimedout(this.timeoutMillis)) || this.isFailed());
    final Level level = shouldStop ? Level.FINE : Level.FINER;
    if(logger.isLoggable(level, cls)) {
      logger.log(level, "Should stop: {0}, timeout: {1}, timespent: {2}, max fails: {3}, current fails: "+this.getFailedCount(), 
      cls, shouldStop, this.getTimeout(), this.getTimeSpent(), this.maxFailsAllowed);
    }
    return shouldStop;
  }
    
  public boolean isFailed() {
    return (this.maxFailsAllowed > 0) && (this.getFailedCount() > this.maxFailsAllowed);
  }
  
  public int getFailedCount() {
    return this.getFailed() == null ? 0 : this.getFailed().size();
  }
  
  @Override
  public boolean isInDatabase(String link) {
    if(this.entityManager == null) {
      this.entityManager = this.getJpaContext().getEntityManager(Feed.class);
    }
    boolean found;
    try{
      final String col = Feed_.url.getName()   ;
      TypedQuery<String> query = this.entityManager.createQuery("SELECT f."+col+" FROM "+Feed.class.getSimpleName()+" f WHERE f."+col+" = :"+col, String.class);
      query.setParameter(col, link);
      query.setFirstResult(0).setMaxResults(1);
      found = query.getSingleResult() != null;
    }catch(NoResultException ignored) {
        found = false;
    }
    if (found) {
      logger.log(Level.FINER, "Link is already in database: {0}", cls, link);
    }
    return found;
  }

  private Integer getLimit(Config.Extractor name) {
    Integer limit = getContext().getConfig().getInt(new Object[] { name });
    if (limit == null) {
      throw new NullPointerException("Required config value: " + name + " == null");
    }
    return limit;
  }

  public boolean isWithinScrappLimit() {
    boolean withinLimit = isWithLimit(this.scrapped, this.scrappLimit);
    logger.log(Level.FINEST, "URLs: {0}, scrapp limit: {1}, within scrapp limit: {2}", 
            cls, getPageLinks().size(), (this.scrappLimit), (withinLimit));
    return withinLimit;
  }
  
  @Override
  protected Integer doCall() {
      
    logger.log(Level.FINER, "Running task {0}", cls, this.getTaskName());
    
    final JsonConfig config = this.getContext().getConfig();

    final String siteName = config.getName();
        
    try{
        
        scrapped = 0;

        NodeFilter imagesFilter = new ImageNodeFilter(
//                config.getString(new Object[] { "url", "value" }), // Images may not start with baseUrl
                null,
                config.getString("imageUrl_requiredRegex"),
                config.getString("imageUrl_unwantedRegex"));

        WebFeedCreator feedCreator = new WebFeedCreator(
                siteName, this.sitetype, imagesFilter, this.tolerance);
        
        Date datecreated = new Date();
        
        while (hasNext() && this.isWithinScrappLimit()) {

          if (isStopRequested() || this.shouldStop()) {
            break;
          }

          HtmlPageDom pageDom = next();

          logger.log(Level.FINER, "PageNodes: {0}", cls, pageDom);

          if (this.accept(pageDom)){

            Feed feed = feedCreator.createFeed(pageDom, datecreated);

            if(feed != null) {
                
              synchronized (feedHandler) {
                
                logger.log(Level.FINER, 
                    "Adding Web Feed. Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: " + feed.getImageurl(), 
                    cls, feed.getSiteid() == null ? null : feed.getSiteid().getSite(), 
                    feed.getAuthor(), feed.getTitle(), feed.getUrl());
              
                final boolean created = feedHandler.process(feed);
                
                if(created) {
                  ++scrapped;  
                }else {
                  this.getFailed().add(pageDom.getURL());
                }
              }
            }
          }
        }
    }finally{
      logger.log(Level.FINE, "Site: {0}, added: {1} feeds", cls, siteName, this.scrapped);
    } 

    return this.scrapped;
  }
  
  public boolean accept(HtmlPageDom pageNodes) {
    return pageNodes != null && this.acceptBody(pageNodes) && 
        this.acceptTitle(pageNodes) && this.acceptUrl(pageNodes);
  }
  
  private boolean acceptBody(HtmlPageDom pageNodes) {
    return true;//pageNodes.getBody() != null;
  }
  
  private boolean acceptTitle(HtmlPageDom pageNodes) {
    return pageNodes.getTitle() == null || !pageNodes.getTitle().toPlainTextString().toLowerCase().contains("400 bad request");  
  }
  
  private boolean acceptUrl(HtmlPageDom pageNodes) {
    return !pageNodes.getURL().equals(getStartUrl());  
  }
  
  private JpaContext getJpaContext() {
    return IdiscApp.getInstance().getJpaContext();
  }
  
  public int getScrapped() {
    return scrapped;
  }

  public int getMaxFailsAllowed() {
    return maxFailsAllowed;
  }
}
