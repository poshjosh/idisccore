package com.idisc.core.rss;

import com.bc.jpa.JpaContext;
import com.bc.jpa.fk.EnumReferences;
import com.bc.task.AbstractStoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.filter.ImageNodeFilter;
import com.idisc.core.FeedHandler;
import com.idisc.core.util.FeedCreator;
import com.idisc.pu.FeedService;
import com.idisc.pu.References;
import com.idisc.pu.SiteService;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;

/**
 * @author poshjosh
 */
public class RSSFeedDownloadTask extends AbstractStoppableTask<Integer> {
    
  private transient static final Class cls = RSSFeedDownloadTask.class;
    
  private transient static final XLogger logger = XLogger.getInstance();
    
  private final boolean acceptDuplicates;
    
  private final String url;
    
  private final Site site;
    
  private final long timeoutMillis;
    
  private final int maxFailsAllowed;

  private int added = 0;
        
  private final FeedHandler feedHandler;
    
  private int failedCount;
    
  public RSSFeedDownloadTask(String feedName, String url,
      long timeout, TimeUnit timeoutUnit, 
      boolean acceptDuplicates, FeedHandler feedHandler) { 
    this.url = url;
    this.timeoutMillis = timeoutUnit.toMillis(timeout);
    this.maxFailsAllowed = 0;
    this.acceptDuplicates = acceptDuplicates;
    JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
    EnumReferences refs = jpaContext.getEnumReferences();
    Sitetype sitetype = ((Sitetype)refs.getEntity(References.sitetype.rss));
    this.feedHandler = feedHandler;
    this.site = new SiteService(jpaContext).from(feedName, sitetype, true);
    Objects.requireNonNull(this.site);
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
    return this.failedCount;
  }
    
    @Override
    protected Integer doCall() {
        
      logger.entering(this.getClass(), "doRun()", url);
      
      try {
        
        logger.log(Level.FINER, "Downloading RSS Feed for {0}", cls, url);
        
        SyndFeed syndFeed = new RSSMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
          logger.log(Level.WARNING, "Failed to create SyndFeed for: {0}", cls, this.url);
          return null;
        }
        logger.log(Level.FINER, "Successfully created SyndFeed for: {0}", cls, this.url);
        
        String baseUrl = com.bc.util.Util.getBaseURL(url);
        NodeFilter imagesFilter = baseUrl == null ? null : new ImageNodeFilter(baseUrl);
        
        Parser parser = new Parser();
        
        List entries = syndFeed.getEntries();
    
        StringBuilder catStrBuilder = new StringBuilder();
        
        StringBuilder contentStrBuilder = new StringBuilder();
        
        added = 0;
        
        final Date NOW = new Date();
        
        final FeedCreator feedCreator = new FeedCreator(site, "news", imagesFilter, 0);
        
        com.bc.webdatex.converter.DateTimeConverter dateConverter =
                new com.bc.webdatex.converter.DateTimeConverter(
                        TimeZone.getDefault(), feedCreator.getOutputTimeZone());
        
        final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
        
        final FeedService feeds = new FeedService(jpaContext);
        
        for (Object obj : entries) {
            
          if (isStopRequested() || this.shouldStop()) {
            break;
          }
          
          SyndEntry entry = (SyndEntry)obj;
          
          List contents = entry.getContents();
          
          contentStrBuilder.setLength(0);
          
          if ((contents != null) && (!contents.isEmpty())) {
              
            for (Object oval : contents) {
                
              SyndContent content = (SyndContent)oval;

              String contentValue = content.getValue();

              if ((contentValue != null) && (!contentValue.isEmpty())) {

                contentStrBuilder.append(contentValue);
                contentStrBuilder.append("<br/><br/>");
              }
            }
          }
          SyndContent syndDescription = entry.getDescription();
          
          String description = null;
          
          if (syndDescription != null) {
            description = syndDescription.getValue();
          }

          if (contentStrBuilder.length() == 0) {

            if ((description != null) && (!description.isEmpty())) {
                
              contentStrBuilder.append(description);
              
            } else if(entry.getTitle() != null && !entry.getTitle().isEmpty()) {
                
              contentStrBuilder.append(entry.getTitle());
            }
          }
          
          // Content cannot be null
          if(contentStrBuilder.length() == 0) {
              continue;
          }

            List categories = entry.getCategories();

            catStrBuilder.setLength(0);
            if (categories != null) {
              Iterator iter = categories.iterator();
              while (iter.hasNext()) {

                Object category = iter.next();
                SyndCategoryImpl syndCat = (SyndCategoryImpl)category;

                String name = syndCat.getName();
                if ((name != null) && (!name.isEmpty()))
                {
                  catStrBuilder.append(name);
                  if (iter.hasNext()) {
                    catStrBuilder.append(',');
                  }
                }
              }
            }

            Feed feed = new Feed();
            
            feed.setAuthor(feedCreator.format("author", entry.getAuthor(), true));

            String cat = catStrBuilder.length() == 0 ? null : catStrBuilder.toString();
            feed.setCategories(feedCreator.format("categories", cat, true));

            String content = contentStrBuilder.length() == 0 ? null : contentStrBuilder.toString();
            feed.setContent(feedCreator.format("content", content, false));

            feed.setDescription(feedCreator.format("description", description, true));

            Date dateCreated = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();
            feed.setFeeddate(dateCreated == null ? NOW : dateConverter.convert(dateCreated));
            
            Date lastModified = entry.getUpdatedDate();
            feed.setTimemodified(dateConverter.convert(lastModified == null ? new Date() : lastModified));
            
            NodeList nodes = this.getNodes(parser, content);

            if (nodes != null && !nodes.isEmpty()) {
              
              String imageurl = feedCreator.getFirstImageUrl(nodes);
              if (imageurl != null) {
                feed.setImageurl(imageurl);
              }
            }

            feed.setSiteid(site);
            
            feed.setTitle(feedCreator.format("title", entry.getTitle(), false));
            
            feed.setUrl(entry.getLink());

            boolean add = this.acceptDuplicates || !feeds.isExisting(feed);
            
            if (add) {

              synchronized (feedHandler) {

                logger.log(Level.FINER, 
                "Adding: {0} RSS Feed. author: {1}, title: {2}\nURL: {3}", 
                cls, add, feed.getAuthor(), feed.getTitle(), feed.getUrl());
                  
                final boolean created = feedHandler.process(feed);
                
                if(created) {
                  ++added;  
                }else {
//                  this.getFailed().add(pageNodes.getURL());
                }
              }
            }
        }
        
        logger.log(Level.FINE, "Added {0} Feed records for: {1} = {2}", cls, added, site.getSite(), url);

      } catch (IOException|FeedException e) {
        logger.logSimple(Level.WARNING, cls, e);
//        logger.log(Level.WARNING, "", cls, e);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Error extracting RSS Feed from: "+this.url, cls, e);
      }finally{
        logger.log(Level.FINE, "RSS: {0}, added {1} feeds", cls, url, added);
      }
      
      return added;
    }
    
    @Override
    public String getTaskName() {
      return getClass().getName();
    }

  private NodeList getNodes(Parser parser, String html) {
    try {
      parser.reset();
      parser.setInputHTML(html);
      return parser.parse(null);
    } catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString()); 
    }
    return null;
  }

  public int getAdded() {
    return added;
  }
}
