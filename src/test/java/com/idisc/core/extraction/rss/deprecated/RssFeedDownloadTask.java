package com.idisc.core.extraction.rss.deprecated;

import com.bc.jpa.context.JpaContext;
import com.bc.task.AbstractStoppableTask;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.FeedHandler;
import com.idisc.core.extraction.FeedCreationConfigImpl;
import com.idisc.core.extraction.FeedCreationContext;
import com.idisc.core.extraction.FeedCreatorFromContext;
import com.idisc.core.extraction.rss.RssFeedCreator;
import com.idisc.core.extraction.rss.RssMgr;
import com.idisc.pu.FeedDao;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.context.ExtractionContext;
import com.bc.webdatex.context.ExtractionConfig;

/**
 * @author poshjosh
 */
public class RssFeedDownloadTask extends AbstractStoppableTask<Integer> {
    
  private transient static final Logger LOG = Logger.getLogger(RssFeedDownloadTask.class.getName());
    
  private final boolean acceptDuplicates;
    
  private final String url;
    
  private final long timeoutMillis;
    
  private final int maxFailsAllowed;

  private final FeedCreatorFromContext<SyndEntry> feedCreator;
  
  private final FeedHandler feedHandler;
    
  private final FeedDao feedDao;
  
  private int succeeded = 0;
        
  private int failedCount;
    
  public RssFeedDownloadTask(String feedName, String url,
      long timeout, TimeUnit timeoutUnit, 
      boolean acceptDuplicates, FeedHandler feedHandler) { 
    this.url = url;
    this.timeoutMillis = timeoutUnit.toMillis(timeout);
    this.maxFailsAllowed = 0;
    this.acceptDuplicates = acceptDuplicates;
    final IdiscApp app = IdiscApp.getInstance();
    final JpaContext jpaContext = app.getJpaContext();
    this.feedDao = new FeedDao(jpaContext);
    this.feedHandler = feedHandler;
    final Site site = new SiteDao(jpaContext).from(feedName, "rss", true);
    Objects.requireNonNull(site);
    final String baseUrl = com.bc.util.Util.getBaseURL(url);
    final NodeFilter imagesFilter = baseUrl == null ? null : new ImageNodeFilter(baseUrl);
    final ExtractionContext capturerContext = app.getExtractionContextFactory().getContext(site.getSite());
    final ExtractionConfig nodeExtractorConfig = capturerContext.getExtractionConfig();
    final FeedCreationContext feedCreationContext = new FeedCreationContextImpl(
            jpaContext.getMetaData(),
            nodeExtractorConfig,
            new FeedCreationConfigImpl(site, nodeExtractorConfig.getDefaults()), 
            imagesFilter);
    this.feedCreator = new RssFeedCreator(capturerContext, feedCreationContext);
  }

  public long getTimeout() {
    return this.timeoutMillis;
  }
    
  public boolean shouldStop() {
    boolean shouldStop = ((this.isRunning() && this.isTimedout(this.timeoutMillis)) || this.isFailed());
    final Level level = shouldStop ? Level.FINE : Level.FINER;
    if(LOG.isLoggable(level)) {
      LOG.log(level, "Should stop: {0}, timeout: {1}, timespent: {2}, max fails: {3}, current fails: {4}", 
      new Object[]{shouldStop, this.getTimeout(), this.getTimeSpent(), this.maxFailsAllowed, this.getFailedCount()});
    }
    return shouldStop;
  }
    
  public boolean isFailed() {
    return (this.maxFailsAllowed > 0) && (this.getFailedCount() > this.maxFailsAllowed);
  }
  
  public int getFailedCount() {
    return this.failedCount;
  }
    
    @Override
    protected Integer doCall() {
        
      LOG.entering(this.getClass().getName(), "doRun()", url);
      
      try {
        
        LOG.log(Level.FINE, "Downloading RSS Feed for {0}", url);
        
        final SyndFeed syndFeed = new RssMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
          LOG.log(Level.WARNING, "Failed to create SyndFeed for: {0}", this.url);
          return null;
        }
        
        LOG.log(Level.FINER, "Successfully created SyndFeed for: {0}", this.url);
        
        final List entries = syndFeed.getEntries();
    
        succeeded = 0;
        
        for (Object obj : entries) {
            
          if (isStopRequested() || this.shouldStop()) {
            break;
          }
          
          SyndEntry entry = (SyndEntry)obj;

          final Feed feed = feedCreator.createFeed(entry);

            boolean add = this.acceptDuplicates || !feedDao.isExisting(feed);
            
            if (add) {

              synchronized (feedHandler) {

                if(LOG.isLoggable(Level.FINE)) {
                  LOG.log(Level.FINE, 
                  "Adding: {0} RSS Feed. author: {1}, title: {2}\nURL: {3}", 
                  new Object[]{add, feed.getAuthor(), feed.getTitle(), feed.getUrl()});
                }
                  
                final boolean created = feedHandler.process(feed);
                
                if(created) {
                  ++succeeded;  
                }else {
//                  this.getFailed().add(pageNodes.getURL());
                }
              }
            }
        }
      } catch (IOException|FeedException e) {
        LOG.warning(e.toString());
//        LOG.log(Level.WARNING, null, e);
      } catch (RuntimeException e) {
        LOG.log(Level.WARNING, "Error extracting RSS Feed from: "+this.url, e);
      }finally{
        
        LOG.log(Level.FINE, "Added {0} RSS records for: {1} = {2}", 
                new Object[]{succeeded, this.feedCreator.getContext().getConfig().getSite().getSite(), url});
      }
      
      return succeeded;
    }
    
    @Override
    public String getTaskName() {
      return getClass().getName();
    }

  public int getSucceeded() {
    return succeeded;
  }
}
