package com.idisc.core.web;

import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.TaskHasResult;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.Crawler;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;
import com.bc.jpa.JpaContext;
import com.idisc.core.filters.CapturerContextImagesFilter;

public class NewsCrawler extends Crawler<Collection<Feed>>
  implements TaskHasResult<Collection<Feed>>, StoppableTask {
    
  private transient static final Class cls = NewsCrawler.class;
  private transient static final XLogger logger = XLogger.getInstance();
  
  private final float tolerance;
  private final Sitetype sitetype;
  private final Collection<Feed> result;
  
  private long timeoutMillis;
  private int maxFailsAllowed;
  
  private EntityManager entityManager;
  
  public NewsCrawler(
          JsonConfig config, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, Collection<Feed> resultsBuffer) {
      
    this(
            IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), 
            timeout, timeoutUnit, maxFailsAllowed, resultsBuffer
    );
  }

  public NewsCrawler(
          CapturerContext context, long timeout, TimeUnit timeoutUnit, 
          int maxFailsAllowed, Collection<Feed> resultsBuffer) {
      
    super(context);
    
    logger.log(Level.FINER, "Creating", cls);
    
    this.timeoutMillis = timeoutUnit.toMillis(timeout);
    
    this.maxFailsAllowed = maxFailsAllowed;
    
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
    
    logger.log(Level.FINER, "Tolerance: {0}", cls, this.tolerance);
    
    this.result = resultsBuffer;
    setParseLimit(getLimit(Config.Extractor.parseLimit));
    setCrawlLimit(getLimit(Config.Extractor.crawlLimit));
    
    logger.log(Level.FINER, "Updated parse limit: {0}, crawl limit: {1}", cls, getParseLimit(), getCrawlLimit());
    
    EnumReferences refs = getControllerFactory().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
    logger.log(Level.FINE, "Done creating: {0}", cls, NewsCrawler.this);
    
    setBatchInterval(0L);
  }

  @Override
  public void completePendingActions() {
    super.completePendingActions();
    if(this.entityManager != null) {
      this.entityManager.close();
    }
  }
  
  @Override
  public long getTimeout() {
    return this.timeoutMillis;
  }
    
  public boolean shouldStop() {
    boolean shouldStop = ((this.isRunning() && this.isTimedout()) || this.isFailed());
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
      this.entityManager = this.getControllerFactory().getEntityManager(Feed.class);
    }
    TypedQuery<String> query = this.entityManager.createQuery("SELECT f.url FROM Feed f WHERE f.url = :url", String.class);
    query.setParameter("url", link);
    query.setFirstResult(0);
    query.setMaxResults(1);
    final boolean found = query.getSingleResult() != null;
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

    @Override
  public Collection<Feed> doCall() {
      
    logger.log(Level.FINER, "Running task {0} for {1}", cls, cls.getName(), this.getSitename());

    try{

        int scrappLimit = getLimit(Config.Extractor.scrappLimit);

        int scrapped = 0;

        NodeFilter imagesFilter = new CapturerContextImagesFilter(this.getContext());

        WebFeedCreator feedCreator = new WebFeedCreator(getSitename(), this.sitetype, imagesFilter, this.tolerance);
        
        Date datecreated = new Date();
        
        while ((hasNext()) && ((scrappLimit <= 0) || (scrapped < scrappLimit))) {

          if (isStopRequested() || this.shouldStop()) {
            break;
          }

          PageNodes pageNodes = next();

          logger.log(Level.FINER, "PageNodes: {0}", cls, pageNodes);

          if (this.accept(pageNodes)){

            Feed feed = feedCreator.createFeed(pageNodes, datecreated);

            synchronized (this.result) {
              logger.log(Level.FINER, 
                      "Adding Web Feed. Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: " + feed.getImageurl(), getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
              this.result.add(feed);
            }

            scrapped++;
          }
        }
    }finally{
        logger.log(Level.FINE, "Site: {0}, added: {1} feeds", cls,
                this.getSitename(), this.result == null ? null : this.result.size());
    } 
    
    return this.getResult();
  }
  
  public boolean accept(PageNodes pageNodes) {
    return (pageNodes != null && //pageNodes.getBody() != null && 
        this.acceptTitle(pageNodes) && 
        !pageNodes.getURL().equals(getStartUrl()));
  }
  
  private boolean acceptTitle(PageNodes pageNodes) {
    return pageNodes.getTitle() == null || !pageNodes.getTitle().toPlainTextString().toLowerCase().contains("400 bad request");  
  }
  
  @Override
  public Collection<Feed> getResult() {
    return this.result;
  }
  
  private JpaContext getControllerFactory() {
    return IdiscApp.getInstance().getJpaContext();
  }
  
  @Override
  public boolean isResumable() {
    return false;
  }
  
  @Override
  public boolean isResume() {
    return true;
  }
}
