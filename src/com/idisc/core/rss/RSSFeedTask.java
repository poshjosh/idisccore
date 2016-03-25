package com.idisc.core.rss;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.process.AbstractStoppableTask;
import com.bc.process.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConcurrentTaskList;
import com.idisc.core.FeedUpdateTask;
import com.idisc.core.IdiscApp;
import com.idisc.core.Util;
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
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;

/**
 * @author Josh
 */
public class RSSFeedTask extends ConcurrentTaskList {

    private final Sitetype sitetype;
    
    private final Properties feeds;
    
    public RSSFeedTask(long timeout, TimeUnit timeUnit) {
        super(timeout, timeUnit);
        EnumReferences refs = RSSFeedTask.this.getControllerFactory().getEnumReferences();
        sitetype = (Sitetype)refs.getEntity(References.sitetype.rss);
        feeds = new RSSMgr().getFeedNamesProperties();
    }
    
    @Override
    public List<String> getTaskNames() {
        Properties props = new RSSMgr().getFeedNamesProperties();
        Set<String> feedNames = props.stringPropertyNames();
        return new ArrayList(feedNames);
    }
    
    @Override
    public StoppableTask createNewTask(final String feedName) {

        StoppableTask task = new DownloadSyndFeedTask(feedName);
        
        return task;
    }
    
    private class DownloadSyndFeedTask extends AbstractStoppableTask{
        private final String feedName;
        private DownloadSyndFeedTask(String feedName) {
            this.feedName = feedName;
        }
        @Override
        protected void doRun() {
            try{
                
XLogger.getInstance().log(Level.FINER, "Running {0} for : {1}", 
this.getClass(), this.getClass().getSimpleName(), feedName);                    
                
                String url = feeds.getProperty(feedName);
                
                EntityController<Feed, Integer> feedCtrl = 
                        RSSFeedTask.this.getControllerFactory().getEntityController(
                                Feed.class, Integer.class);
        
                SyndFeed syndFeed = new RSSMgr().getSyndFeed(url);
                
                if(syndFeed == null) {
XLogger.getInstance().log(Level.WARNING, "Failed to create SyndFeed for: {0}", this.getClass(), url);                    
                    return;
                }else{
XLogger.getInstance().log(Level.FINE, "Successfully created SyndFeed for: {0}", this.getClass(), url);                    
                }

                NodeFilter filter0 = new NodeClassFilter(ImageTag.class);
                NodeFilter filter1 = new TagNameFilter("IMG");
                NodeFilter imagesFilter = new OrFilter(new NodeFilter[]{filter0, filter1});
                
                Parser parser = new Parser();
                
                Feed reusedFeedParams = new Feed();
                
                List entries = syndFeed.getEntries();
                
                StringBuilder catStrBuilder = new StringBuilder();
                
                StringBuilder contentStrBuilder = new StringBuilder();
                
                int added = 0;
                
                for(Object obj:entries) {
                    
                    if(this.isStopInitiated()) {
                        break;
                    }
                    
                    SyndEntry entry = (SyndEntry)obj;
                
                    List contents = entry.getContents();
                    
                    contentStrBuilder.setLength(0);
                    if(contents != null && !contents.isEmpty()) {
                        for(Object oval:contents) {
                            
                            SyndContent content = (SyndContent)oval;
                            String contentValue = content.getValue();
                            if(contentValue == null || contentValue.isEmpty()) {
                                continue;
                            }
                            contentStrBuilder.append(contentValue);
                            contentStrBuilder.append("<br/><br/>");
                        }
                    }

                    SyndContent syndDescription = entry.getDescription();
                    
                    String description = null;
                    
                    if(syndDescription != null) {
                        description = syndDescription.getValue();
                    }
                    
                    // Content cannot be null
                    //
                    if(contentStrBuilder.length() == 0) {
                        if(description == null || description.isEmpty()) {
                            continue;
                        }else{
                            contentStrBuilder.append(description);
                        }
                    }
                    
                    List categories = entry.getCategories();
                    catStrBuilder.setLength(0);
                    if(categories != null) {
                        Iterator iter = categories.iterator();
                        while(iter.hasNext()) {
                            Object category = iter.next();
                            SyndCategoryImpl syndCat = ((SyndCategoryImpl)category);
                            String name = syndCat.getName();
                            if(name == null || name.isEmpty()) {
                                continue;
                            }
                            catStrBuilder.append(name);
                            if(iter.hasNext()) {
                                catStrBuilder.append(',');
                            }
                        }
                    }

                    Feed feed = new Feed();
                    
                    feed.setAuthor(Util.truncate(Feed.class, "author", entry.getAuthor()));
                    
                    String cat = catStrBuilder.length()==0?null:catStrBuilder.toString();
                    feed.setCategories(Util.truncate(Feed.class, "categories", cat));
                    
                    String content = contentStrBuilder.length()==0?null:contentStrBuilder.toString();
                    feed.setContent(Util.truncate(Feed.class, "content", content));
                    
//                    feed.setDatecreated(new Date()); // Add just before creation
                    feed.setDescription(Util.truncate(Feed.class, "description", description));
                    
                    Date date = entry.getUpdatedDate() == null ? entry.getPublishedDate() : entry.getUpdatedDate();
                    // We need this for sorting
                    feed.setFeeddate(date==null?new Date():date);

////////////////////////////////////////                    
                    NodeList nodes = RSSFeedTask.this.getNodes(parser, content);
                    if(nodes != null && !nodes.isEmpty()) {
                        String imageUrl = Util.getFirtImageUrl(nodes, imagesFilter);
                        if(imageUrl != null) {
                            feed.setImageurl(imageUrl);
                        }
                    }
/////////////////////////////////////////                    
                    
                    Site site = Util.findSite(feedName, sitetype, true);
                    if(site == null) {
                        throw new NullPointerException();
                    }
                    feed.setSiteid(site);
                    feed.setTitle(Util.truncate(Feed.class, "title", entry.getTitle()));
                    feed.setUrl(entry.getLink());
                    
                    if(RSSFeedTask.this.isAcceptDuplicates() || !Util.isInDatabase(feedCtrl, reusedFeedParams, feed)) {

                        Collection<Feed> resultBuffer = RSSFeedTask.this.getResult();
                        
                        synchronized(resultBuffer) {
                            
XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
        "Adding RSS Feed. Site {0}, author: {1}, title: {2}\nURL: {3}", this.getClass(), 
feed.getSiteid()==null?null:feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
                            
                            resultBuffer.add(feed);
                            
                            ++added;
                        }    
                    }
                }
                
XLogger.getInstance().log(Level.FINE, "Added {0} Feed records for: {1}", 
this.getClass(), added, url);                    
                
            }catch(IOException | FeedException e) {
                
                XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
                
            }
        }
        @Override
        public String getTaskName() {
            return this.getClass().getName();
        }
    }
    
    private NodeList getNodes(Parser parser, String html) {
        try{
            parser.reset();
            parser.setInputHTML(html);
            return parser.parse(null);
        }catch(Exception e) {
            XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
            return null;
        }
    }
    
    private ControllerFactory getControllerFactory() {
        return IdiscApp.getInstance().getControllerFactory();
    }
}
