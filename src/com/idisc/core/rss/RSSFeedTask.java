package com.idisc.core.rss;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.task.AbstractStoppableTask;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.FeedUpdateTask;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;

public class RSSFeedTask
  extends ConcurrentTaskList
{
  private final Sitetype sitetype;
  private Properties feedProperties;
  
  public RSSFeedTask(long timeout, TimeUnit timeUnit)
  {
    super(timeout, timeUnit);
    EnumReferences refs = getControllerFactory().getEnumReferences();
    this.sitetype = ((Sitetype)refs.getEntity(References.sitetype.rss));
    this.feedProperties = new RSSMgr().getFeedNamesProperties();
  }
  
  @Override
  public List<String> getTaskNames()
  {
    Set<String> feedNames = this.feedProperties.stringPropertyNames();
    return new ArrayList(feedNames);
  }
  
  @Override
  public StoppableTask createNewTask(String feedName)
  {
    XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);
    StoppableTask task = new DownloadSyndFeedTask(feedName);
    
    return task;
  }
  
  private class DownloadSyndFeedTask extends AbstractStoppableTask {
    private final String feedName;
    
    private DownloadSyndFeedTask(String feedName) { this.feedName = feedName; }
    
    @Override
    protected void doRun()
    {
      XLogger.getInstance().entering(this.getClass(), "doRun()", feedName);
      try
      {
        
        String url = RSSFeedTask.this.feedProperties.getProperty(this.feedName);

        XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, "Downloading RSS Feed for {0} = {1}", 
                getClass(), this.feedName, url);
        
        SyndFeed syndFeed = new RSSMgr().getSyndFeed(url);
        
        if (syndFeed == null) {
          XLogger.getInstance().log(Level.WARNING, "Failed to create SyndFeed for: {0} = {1}", getClass(), this.feedName, url);
          return;
        }
        XLogger.getInstance().log(Level.FINER, "Successfully created SyndFeed for: {0} = {1}", getClass(), this.feedName, url);
        
        String baseUrl = com.bc.util.Util.getBaseURL(url);
        NodeFilter imagesFilter = baseUrl == null ? null : com.idisc.core.Util.createImagesFilter(baseUrl);
        
        Parser parser = new Parser();
        
        Feed reusedFeedParams = new Feed();
        
        List entries = syndFeed.getEntries();
    
        StringBuilder catStrBuilder = new StringBuilder();
        
        StringBuilder contentStrBuilder = new StringBuilder();
        
        int added = 0;
        
        EntityController<Feed, Integer> feedCtrl = RSSFeedTask.this.getControllerFactory().getEntityController(Feed.class, Integer.class);

        for (Object obj : entries)
        {
          if (isStopInitiated()) {
            break;
          }
          
          SyndEntry entry = (SyndEntry)obj;
          
          List contents = entry.getContents();
          
          contentStrBuilder.setLength(0);
          if ((contents != null) && (!contents.isEmpty())) {
            for (Object oval : contents)
            {
                
              SyndContent content = (SyndContent)oval;

              String contentValue = content.getValue();

              if ((contentValue != null) && (!contentValue.isEmpty()))
              {

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

            if ((description != null) && (!description.isEmpty()))
            {
              contentStrBuilder.append(description);
            }
            else if(entry.getTitle() != null && !entry.getTitle().isEmpty())
            {
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

            feed.setAuthor(com.idisc.core.Util.truncate(Feed.class, "author", entry.getAuthor()));

            String cat = catStrBuilder.length() == 0 ? null : catStrBuilder.toString();
            feed.setCategories(com.idisc.core.Util.truncate(Feed.class, "categories", cat));

            String content = contentStrBuilder.length() == 0 ? null : contentStrBuilder.toString();
            feed.setContent(com.idisc.core.Util.truncate(Feed.class, "content", content));

            feed.setDescription(com.idisc.core.Util.truncate(Feed.class, "description", description));

            Date date = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();

            feed.setFeeddate(date == null ? new Date() : date);

            NodeList nodes = RSSFeedTask.this.getNodes(parser, content);
            if ((imagesFilter != null) && (nodes != null) && (!nodes.isEmpty())) {
              String imageUrl = com.idisc.core.Util.getFirstImageUrl(nodes, imagesFilter);
              if (imageUrl != null) {
                feed.setImageurl(imageUrl);
              }
            }

            Site site = com.idisc.core.Util.findSite(this.feedName, RSSFeedTask.this.sitetype, true);
            if (site == null) {
              throw new NullPointerException();
            }
            feed.setSiteid(site);
            feed.setTitle(com.idisc.core.Util.truncate(Feed.class, "title", entry.getTitle()));
            feed.setUrl(entry.getLink());

            boolean add = (RSSFeedTask.this.isAcceptDuplicates()) || (!com.idisc.core.Util.isInDatabase(feedCtrl, reusedFeedParams, feed));
            if (add)
            {
              Collection<Feed> resultBuffer = RSSFeedTask.this.getResult();

              synchronized (resultBuffer)
              {

                resultBuffer.add(feed);

                ++added;
              }
            }
            XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
            "Added: {0} RSS Feed. author: {1}, title: {2}\nURL: {3}", 
            getClass(), add, feed.getAuthor(), feed.getTitle(), feed.getUrl());
        }
        XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, "Added {0} Feed records for: {1} = {2}", getClass(), Integer.valueOf(added), this.feedName, url);

      }
      catch (IOException|FeedException e)
      {
        XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
//        XLogger.getInstance().log(Level.WARNING, "", this.getClass(), e);
      }
      catch (RuntimeException e)
      {
        XLogger.getInstance().log(Level.WARNING, "Error extracting RSS Feed from: "+this.feedName, getClass(), e);
      }
    }
    
    @Override
    public String getTaskName()
    {
      return getClass().getName();
    }
  }
  
  private NodeList getNodes(Parser parser, String html) {
    XLogger.getInstance().entering(this.getClass(), "getNodes(org.htmlparser.Parser, String)", null);
    try {
      parser.reset();
      parser.setInputHTML(html);
      return parser.parse(null);
    } catch (Exception e) {
      XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString()); 
    }
    return null;
  }
  
  private ControllerFactory getControllerFactory()
  {
    return IdiscApp.getInstance().getControllerFactory();
  }

    public Properties getFeedProperties() {
        return feedProperties;
    }

    public void setFeedProperties(Properties feedProperties) {
        this.feedProperties = feedProperties;
    }
}
