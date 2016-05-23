package com.idisc.core.web;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
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
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;

public class NewsCrawler extends Crawler
  implements TaskHasResult<Collection<Feed>>, StoppableTask
{
  private final float tolerance;
  private final Class cls;
  private final XLogger logger;
  private final Sitetype sitetype;
  private final Collection<Feed> result;
  private EntityController<Feed, Integer> ec_accessViaGetter;
  
  public NewsCrawler(JsonConfig config, Collection<Feed> resultsBuffer)
  {
    this(IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), resultsBuffer);
  }

  public NewsCrawler(CapturerContext context, Collection<Feed> resultsBuffer)
  {
    super(context);
    
    cls = this.getClass();
    
    logger = XLogger.getInstance();
    
    logger.log(Level.FINER, "Creating", cls);
    
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    this.tolerance = config.getFloat("dataComparisonTolerance", 0.0F);
    
    logger.log(Level.FINER, "Tolerance: {0}", cls, Float.valueOf(this.tolerance));
    
    this.result = resultsBuffer;
    setParseLimit(getLimit(Config.Extractor.parseLimit));
    setCrawlLimit(getLimit(Config.Extractor.crawlLimit));
    
    logger.log(Level.FINER, "Updated parse limit: {0}, crawl limit: {1}", cls, Integer.valueOf(getParseLimit()), Integer.valueOf(getCrawlLimit()));
    

    EnumReferences refs = getControllerFactory().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.web));
    logger.log(Level.FINE, "Done creating: {0}", cls, NewsCrawler.this);
    
    setBatchInterval(0L);
  }
  
  @Override
  public boolean isInDatabase(String link)
  {
    EntityController<Feed, Integer> ec = getFeedController();
    boolean found = ec.selectFirst("url", link) != null;
    if (found) {
      logger.log(Level.FINER, "Link is already in database: {0}", cls, link);
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
    logger.log(Level.FINER, "Running task {0} for {1}", cls, cls.getName(), this.getSitename());

    try{
        
        int scrappLimit = getLimit(Config.Extractor.scrappLimit);

        int scrapped = 0;

        String baseUrl = getContext().getConfig().getString(new Object[] { "url", "value" });
        NodeFilter imagesFilter = Util.createImagesFilter(baseUrl);

        WebFeedCreator feedCreator = new WebFeedCreator(getSitename(), this.sitetype);
        feedCreator.setImagesFilter(imagesFilter);
        feedCreator.setTolerance(this.tolerance);
        
        while ((hasNext()) && ((scrappLimit <= 0) || (scrapped < scrappLimit)))
        {

          if (isStopRequested()) {
            break;
          }

          PageNodes pageNodes = next();

          logger.log(Level.FINER, "PageNodes: {0}", cls, pageNodes);

          if ((pageNodes != null) && (pageNodes.getBody() != null) && 
            ((pageNodes.getTitle() == null) || (!pageNodes.getTitle().toPlainTextString().toLowerCase().contains("400 bad request"))) && 
            (!pageNodes.getURL().equals(getStartUrl())))
          {

            Feed feed = feedCreator.createFeed(pageNodes);

            synchronized (this.result) {
              logger.log(Level.FINER, 
                      "Adding Web Feed. Site {0}, author: {1}, title: {2}\nURL: {3}\nImage url: " + feed.getImageurl(), getClass(), feed.getSiteid() == null ? null : feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
              this.result.add(feed);
            }

            scrapped++;
          }
        }
    }finally{
        logger.log(Level.FINE, "Site: {0}, number of feeds added: {1}", cls,
                this.getSitename(), this.result == null ? null : this.result.size());
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
