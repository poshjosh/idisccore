package com.idisc.core.extraction.rss;

import com.bc.jpa.context.JpaContext;
import com.bc.jpa.fk.EnumReferences;
import com.bc.task.AbstractStoppableTask;
import com.idisc.core.IdiscApp;
import com.bc.webdatex.nodefilters.ImageNodeFilter;
import com.idisc.core.FeedHandler;
import com.idisc.core.util.FeedCreator;
import com.idisc.pu.FeedDao;
import com.idisc.pu.References;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.rometools.rome.feed.synd.SyndCategoryImpl;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.dom.HtmlDocument;

/**
 * @author poshjosh
 */
public class RssFeedDownloadTask extends AbstractStoppableTask<Integer> {
    
  private transient static final Logger LOG = Logger.getLogger(RssFeedDownloadTask.class.getName());
    
  private final boolean acceptDuplicates;
    
  private final String url;
    
  private final Site site;
    
  private final long timeoutMillis;
    
  private final int maxFailsAllowed;

  private int added = 0;
        
  private final FeedHandler feedHandler;
    
  private int failedCount;
    
  public RssFeedDownloadTask(String feedName, String url,
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
    this.site = new SiteDao(jpaContext).from(feedName, sitetype, true);
    Objects.requireNonNull(this.site);
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
        
        SyndFeed syndFeed = new RssMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
          LOG.log(Level.WARNING, "Failed to create SyndFeed for: {0}", this.url);
          return null;
        }
        LOG.log(Level.FINER, "Successfully created SyndFeed for: {0}", this.url);
        
        String baseUrl = com.bc.util.Util.getBaseURL(url);
        NodeFilter imagesFilter = baseUrl == null ? null : new ImageNodeFilter(baseUrl);
        
        Parser parser = new Parser();
        
        List entries = syndFeed.getEntries();
    
        StringBuilder catStrBuilder = new StringBuilder();
        
        StringBuilder contentStrBuilder = new StringBuilder();
        
        added = 0;
        
        final Date NOW = new Date();
        
        final FeedCreator feedCreator = new FeedCreator(
                IdiscApp.getInstance().getJpaContext(),
                site, "news", imagesFilter, 0);
        
        com.bc.webdatex.converters.DateTimeConverter dateConverter =
                new com.bc.webdatex.converters.DateTimeConverter(
                        TimeZone.getDefault(), feedCreator.getOutputTimeZone());
        
        final JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();
        
        final FeedDao feeds = new FeedDao(jpaContext);
        
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
            if(dateCreated != null && dateCreated.after(NOW)) {
                dateCreated = NOW;
            }
            feed.setFeeddate(dateCreated == null ? NOW : dateConverter.convert(dateCreated));
            
            Date lastModified = entry.getUpdatedDate();
            feed.setTimemodified(dateConverter.convert(lastModified == null ? new Date() : lastModified));
            
            HtmlDocument nodes = this.getNodes(parser, content);

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

                if(LOG.isLoggable(Level.FINE)) {
                  LOG.log(Level.FINE, 
                  "Adding: {0} RSS Feed. author: {1}, title: {2}\nURL: {3}", 
                  new Object[]{add, feed.getAuthor(), feed.getTitle(), feed.getUrl()});
                }
                  
                final boolean created = feedHandler.process(feed);
                
                if(created) {
                  ++added;  
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
                new Object[]{added, site.getSite(), url});
      }
      
      return added;
    }
    
    @Override
    public String getTaskName() {
      return getClass().getName();
    }

  private HtmlDocument getNodes(Parser parser, String html) {
    try {
      parser.reset();
      parser.setInputHTML(html);
      return parser.parse(null);
    } catch (Exception e) {
      if(LOG.isLoggable(Level.WARNING)){
         LOG.log(Level.WARNING, "{0}", e.toString());
      } 
    }
    return null;
  }

  public int getAdded() {
    return added;
  }
}
