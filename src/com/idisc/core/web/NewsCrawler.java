package com.idisc.core.web;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.FeedCreator;
import com.idisc.core.FeedUpdateTask;
import com.idisc.core.IdiscApp;
import com.idisc.core.TaskHasResult;
import com.idisc.core.Util;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Sitetype;
import com.scrapper.Crawler;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.util.PageNodes;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;

public class NewsCrawler
  extends Crawler
  implements TaskHasResult<Collection<Feed>>, StoppableTask
{
  private final float tolerance;
  private final Sitetype sitetype;
  private final Collection<Feed> result;
  private transient Feed feedToFind;
  private EntityController<Feed, Integer> ec_accessViaGetter;
  
  public NewsCrawler(JsonConfig config, Collection<Feed> resultsBuffer)
  {
    this(IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), resultsBuffer);
  }

  public NewsCrawler(CapturerContext context, Collection<Feed> resultsBuffer)
  {
    super(context);
    
    XLogger.getInstance().log(Level.FINER, "Creating", getClass());
    
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
    
    XLogger.getInstance().log(Level.FINER, "Tolerance: {0}", getClass(), Float.valueOf(this.tolerance));
    
    this.result = resultsBuffer;
    setParseLimit(getLimit(Config.Extractor.parseLimit));
    setCrawlLimit(getLimit(Config.Extractor.crawlLimit));
    
    XLogger.getInstance().log(Level.FINER, "Updated parse limit: {0}, crawl limit: {1}", getClass(), Integer.valueOf(getParseLimit()), Integer.valueOf(getCrawlLimit()));
    

    EnumReferences refs = getControllerFactory().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
    XLogger.getInstance().log(Level.FINE, "Done creating: {0}", getClass(), this);
    
    setBatchInterval(0L);
  }
  
  @Override
  public boolean isInDatabase(String link)
  {
    if (this.feedToFind == null) {
      this.feedToFind = new Feed();
    }
    this.feedToFind.setUrl(link);
    EntityController<Feed, Integer> ec = getFeedController();
    Map map = ec.toMap(this.feedToFind, false);
    boolean found = ec.selectFirst(map) != null;
    if (found) {
      XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, "Link is already in database: {0}", getClass(), link);
    }
    return found;
  }
  
  private int getLimit(Config.Extractor name) {
    Integer limit = getContext().getConfig().getInt(new Object[] { name });
    if (limit == null) {
      throw new NullPointerException("Required config value: " + name + " == null");
    }
    return limit.intValue();
  }
  

  @Override
  protected void doRun()
  {
    int scrappLimit = getLimit(Config.Extractor.scrappLimit);
    
    int scrapped = 0;
    
    String baseUrl = getContext().getConfig().getString(new Object[] { "url", "value" });
    NodeFilter imagesFilter = Util.createImagesFilter(baseUrl);
    
    FeedCreator feedCreator = new FeedCreator(getSitename(), this.sitetype);
    feedCreator.setImagesFilter(imagesFilter);
    feedCreator.setTolerance(this.tolerance);
    
    while ((hasNext()) && ((scrappLimit <= 0) || (scrapped < scrappLimit)))
    {
      if (isStopInitiated()) {
        break;
      }
      
      PageNodes pageNodes = next();
      
      XLogger.getInstance().log(Level.FINER, "PageNodes: {0}", getClass(), pageNodes);
      
      if ((pageNodes != null) && (pageNodes.getBody() != null) && 
      
        ((pageNodes.getTitle() == null) || (!pageNodes.getTitle().toPlainTextString().toLowerCase().contains("400 bad request"))) && 

        (!pageNodes.getURL().equals(getStartUrl())))
      {


        Feed feed = feedCreator.createFeed(pageNodes);
        
        synchronized (this.result) {
          XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, "Adding Web Feed. Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: " + feed.getImageurl(), getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
          this.result.add(feed);
        }
        
        scrapped++;
      }
    }
  }
  
  @Override
  public Collection<Feed> getResult() {
    return this.result;
  }
  
  private EntityController<Feed, Integer> getFeedController()
  {
    if (this.ec_accessViaGetter == null) {
      this.ec_accessViaGetter = getControllerFactory().getEntityController(Feed.class, Integer.class);
    }
    return this.ec_accessViaGetter;
  }
  
  private ControllerFactory getControllerFactory() {
    return IdiscApp.getInstance().getControllerFactory();
  }
  

  @Override
  public boolean isResumable()
  {
    return false;
  }
  
  @Override
  public boolean isResume()
  {
    return true;
  }
}
