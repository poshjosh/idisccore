package com.idisc.core.rss;

import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.task.AbstractStoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.sun.syndication.feed.synd.SyndCategoryImpl;
import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;

/**
 * @author poshjosh
 */
public class RSSFeedDownloadTask extends AbstractStoppableTask {
    
  private transient static final Class cls = RSSFeedDownloadTask.class;
    
  private transient static final XLogger logger = XLogger.getInstance();
    
  private final boolean acceptDuplicates;
    
  private final String feedName;
    
  private final String url;
    
  private final Sitetype sitetype;
    
  private final long timeoutMillis;
    
  private final int maxFailsAllowed;
    
  private final Collection<Feed> resultBuffer;
    
  private int failedCount;
    
  public RSSFeedDownloadTask(String feedName, String url,
      long timeout, TimeUnit timeoutUnit, 
      boolean acceptDuplicates, Collection<Feed> resultBuffer) { 
    this.feedName = feedName;
    this.url = url;
    this.timeoutMillis = timeoutUnit.toMillis(timeout);
    this.maxFailsAllowed = 0;
    this.acceptDuplicates = acceptDuplicates;
    EnumReferences refs = IdiscApp.getInstance().getJpaContext().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.rss));
    this.resultBuffer = resultBuffer;
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
    return this.failedCount;
  }
    
    @Override
    protected void doRun() {
        
      logger.entering(this.getClass(), "doRun()", url);
      
      try {
        
        logger.log(Level.FINER, "Downloading RSS Feed for {0}", cls, url);
        
        SyndFeed syndFeed = new RSSMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
          logger.log(Level.WARNING, "Failed to create SyndFeed for: {0}", cls, this.url);
          return;
        }
        logger.log(Level.FINER, "Successfully created SyndFeed for: {0}", cls, this.url);
        
        String baseUrl = com.bc.util.Util.getBaseURL(url);
        NodeFilter imagesFilter = baseUrl == null ? null : com.idisc.core.util.Util.createImagesFilter(baseUrl);
        
        Parser parser = new Parser();
        
        Feed reusedFeedParams = new Feed();
        
        List entries = syndFeed.getEntries();
    
        StringBuilder catStrBuilder = new StringBuilder();
        
        StringBuilder contentStrBuilder = new StringBuilder();
        
        int added = 0;
        
        EntityController<Feed, Integer> feedCtrl = 
                IdiscApp.getInstance().getJpaContext().getEntityController(
                        Feed.class, Integer.class);

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

            feed.setAuthor(com.idisc.core.util.Util.truncate(Feed.class, "author", entry.getAuthor()));

            String cat = catStrBuilder.length() == 0 ? null : catStrBuilder.toString();
            feed.setCategories(com.idisc.core.util.Util.truncate(Feed.class, "categories", cat));

            String content = contentStrBuilder.length() == 0 ? null : contentStrBuilder.toString();
            feed.setContent(com.idisc.core.util.Util.truncate(Feed.class, "content", content));

            feed.setDescription(com.idisc.core.util.Util.truncate(Feed.class, "description", description));

            Date date = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();

            feed.setFeeddate(date == null ? new Date() : date);

            NodeList nodes = this.getNodes(parser, content);
            if ((imagesFilter != null) && (nodes != null) && (!nodes.isEmpty())) {
              String imageUrl = com.idisc.core.util.Util.getFirstImageUrl(nodes, imagesFilter);
              if (imageUrl != null) {
                feed.setImageurl(imageUrl);
              }
            }

            Site site = com.idisc.core.util.Util.findSite(this.feedName, this.sitetype, true);
            if (site == null) {
              throw new NullPointerException();
            }
            feed.setSiteid(site);
            feed.setTitle(com.idisc.core.util.Util.truncate(Feed.class, "title", entry.getTitle()));
            feed.setUrl(entry.getLink());

            boolean add = (this.acceptDuplicates) || (!com.idisc.core.util.Util.isInDatabase(feedCtrl, reusedFeedParams, feed));
            if (add) {

              synchronized (resultBuffer) {

                resultBuffer.add(feed);

                ++added;
              }
            }
            logger.log(Level.FINER, 
            "Added: {0} RSS Feed. author: {1}, title: {2}\nURL: {3}", 
            cls, add, feed.getAuthor(), feed.getTitle(), feed.getUrl());
        }
        logger.log(Level.FINER, "Added {0} Feed records for: {1} = {2}", cls, added, this.feedName, url);

      } catch (IOException|FeedException e) {
        logger.logSimple(Level.WARNING, cls, e);
//        logger.log(Level.WARNING, "", cls, e);
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Error extracting RSS Feed from: "+this.url, cls, e);
      }finally{
        logger.log(Level.FINE, "RSS: {0}, added {1} feeds", cls, url, this.resultBuffer == null ? null : this.resultBuffer.size());
      }
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
}
