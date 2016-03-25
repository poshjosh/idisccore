package com.idisc.core.twitter;

import com.bc.htmlparser.ParseJob;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.TaskHasResult;
import com.idisc.core.Util;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.bc.json.config.JsonConfig;
import com.idisc.core.AppProperties;
import com.idisc.core.web.NewsCrawler;
import com.scrapper.ResumableUrlParser;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.tag.Link;
import com.scrapper.util.PageNodes;
import com.scrapper.util.PageNodesImpl;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Trend;
import twitter4j.URLEntity;
import twitter4j.User;

/**
 * @(#)TwitterFeedTask.java   18-Oct-2014 00:07:05
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.1
 * @since    0.1
 */
public class TwitterFeedTask 
        implements Serializable,
        TaskHasResult<Collection<Feed>> {

    final private Collection<Feed> result;

    private final Sitetype timeline;

    private final Sitetype trending;
    
    private final EnumReferences refs;
    
    private final float tolerance;
    
    public TwitterFeedTask() {
        result = Collections.synchronizedCollection(new ArrayList<Feed>());
        refs = TwitterFeedTask.this.getControllerFactory().getEnumReferences();
        timeline = (Sitetype)refs.getEntity(References.sitetype.timeline);
        trending = (Sitetype)refs.getEntity(References.sitetype.trend); 
        Configuration config = IdiscApp.getInstance().getConfiguration();
        tolerance = config.getFloat(AppProperties.TOLERANCE, 0.0f);
    }
    
    @Override
    public void run() {
XLogger.getInstance().log(Level.FINER, "Running {0} :: {1}", 
this.getClass(), this.getClass().getSimpleName(), this);                    

        
        try{
            
            TwitterClient twr = new TwitterClient();
            
            final long sinceId = this.getNewestTweetId();
XLogger.getInstance().log(Level.FINER, "Newest tweet id: {0}", this.getClass(), sinceId);            
  
            // Time consuming & possibly memory consuming
//            List<Trend> trends = twr.getLocationTrends(request.getRemoteAddr());
            
//            this.addTrends(trends);
            
            ResponseList<Status> timeLine;
            if(sinceId == -1) {
                timeLine = twr.getTwitter().getHomeTimeline();
            }else{
                Paging paging = new Paging();
                paging.setSinceId(sinceId);
                timeLine = twr.getTwitter().getHomeTimeline(paging);
            }
            
XLogger.getInstance().log(Level.FINE, "Twitter timeline count: {0}", this.getClass(), timeLine==null?null:timeLine.size());            
XLogger.getInstance().log(Level.FINER, "Twitter timeline: {0}", this.getClass(), timeLine);            

            if(timeLine != null && !timeLine.isEmpty()) {
                
                this.addTimeline(timeLine);
            }
        }catch(twitter4j.TwitterException e) {
            
            XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
            
        }catch(RuntimeException e) {
            
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
        }
    }
    
    private long getNewestTweetId() {
        
        EntityController<Feed, Integer> ec = 
                this.getControllerFactory().getEntityController(Feed.class, Integer.class);
        
//@column literal @todo literals not good        
//@related_twitter categories        
//        
        Map params = Collections.singletonMap("categories", "statuses");
        
        Map<String, String> orderBy = Collections.singletonMap("feeddate", "DESC");
        
        List<Feed> feeds = ec.select(params, orderBy, 0, 1);
        
        if(feeds == null || feeds.isEmpty()) {
            return -1;
        }else{
            String rawId = feeds.get(0).getRawid();
            if(rawId == null) {
                return -1;
            }else{
                return Long.parseLong(rawId);
            }
        }
    }

    private void addTimeline(List<Status> statuses) {
        
        if(statuses == null || statuses.isEmpty()) {
            return;
        }
        
        NodeFilter filter0 = new NodeClassFilter(ImageTag.class);
        NodeFilter filter1 = new TagNameFilter("IMG");
        NodeFilter imagesFilter = new OrFilter(new NodeFilter[]{filter0, filter1});
        
//@update        
        ParseJob parseJob = new ParseJob();
        
        for(Status status:statuses) {

            Feed feed = new Feed();

            Site site = Util.findSite("twitter", timeline, true);
            
            feed.setSiteid(site);
            
            boolean updatedWithDirectContents = false;
            
// The sources are usually twitter clients like dlvr.it and iGANTA
// linking to their websites gives us nothing
            
            // First try extracting link from URL Entities
            //
            String link = null;
            
            URLEntity [] entities = status.getURLEntities();
            if(entities != null && entities.length != 0) {
                
                for(URLEntity entity:entities) {
                    
                    String expandedUrl = entity.getExpandedURL();
                    
XLogger.getInstance().log(Level.INFO, "URL: {0}\nExpanded URL: {1}", 
this.getClass(), entity.getURL(), expandedUrl);

                    link = expandedUrl != null ? expandedUrl : entity.getURL();
                    
//@todo Write a better algorithm.      
                    // Notice we use expandedUrl in the first part and link in the second
                    if(expandedUrl != null && !link.toLowerCase().contains("hootsuite")) {
                        // Notice we use the expanded url
                        updatedWithDirectContents = this.updateFeedWithDirectContent(feed, expandedUrl, imagesFilter, parseJob);
                    }

                    if(updatedWithDirectContents) {
                        break;
                    }
                }
            }

            String imageUrl = null;
            MediaEntity [] mediaEntities = status.getMediaEntities();
            if(mediaEntities != null) {
                for(MediaEntity media:mediaEntities) {
                    
XLogger.getInstance().log(Level.INFO, "Media URL: {0}\nExpanded URL: {1}\nURL: {2}", 
this.getClass(), media.getMediaURL(), media.getExpandedURL(), media.getURL());
                    
                    imageUrl = media.getMediaURL() == null ?
                            media.getMediaURLHttps() : media.getMediaURL();
                    
                    if(imageUrl != null) {
                        break;
                    }
                }
            }
            
            User user = status.getUser();

            if(imageUrl == null && user != null) {
                imageUrl = user.getMiniProfileImageURL();
                if(imageUrl == null) {
                    imageUrl = user.getProfileImageURL();
                }
            }
            feed.setImageurl(imageUrl);
            
            if(link == null) {
                link = this.getHrefFromHtmlLink(status.getSource());
            }
            
            // At this stage remember feed may have been updated with direct contents
            // So we always check if a value already exists before updating
            //

            if(feed.getUrl() == null) {
                
                feed.setUrl(link);
            }

            if(feed.getAuthor() == null) {
                
                final String userName = user == null ? null : user.getName();

                feed.setAuthor(Util.truncate(Feed.class, "author", userName));
            }
            
//@related_twitter categories
//            
            if(!updatedWithDirectContents) {
                feed.setCategories("statuses");
            }
//System.out.println("Adding: "+feed.getContent());            
//            feed.setDatecreated(new Date()); // Add just before creation
//            feed.setDescription(???);
            
            // We need this for sorting
            if(feed.getFeeddate() == null) {
                feed.setFeeddate(status.getCreatedAt()==null?new Date():status.getCreatedAt());
            }

            feed.setRawid(""+status.getId());
                
            if(!updatedWithDirectContents) {
                feed.setContent(Util.truncate(Feed.class, "content", status.getText()));
                feed.setKeywords(null);
            }

            synchronized(result) {
    
XLogger.getInstance().log(Level.INFO, "Adding Twitter Status Feed. Site: {0}, author: {1}, title: {2}\nURL: {3}", this.getClass(), 
feed.getSiteid()==null?null:feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
                
                result.add(feed);
            }
        }
    }
    
//@update    
    private boolean updateFeedWithDirectContent(Feed feed, String link, NodeFilter imagesFilter, ParseJob parseJob) {
    
        try{
            
            ScrapperConfigFactory cf = IdiscApp.getInstance().getCapturerApp().getConfigFactory();
            
            Set<String> sites = cf.getSitenames();
            
            String selectedSite = null;
            for(String site:sites) {
                if(site.equalsIgnoreCase("default")) {
                    continue;
                }
                JsonConfig cfg = cf.getConfig(site);
                String baseUrl = cfg.getString("url", "value");
                
                if(link.startsWith(baseUrl)) {
                    selectedSite = site;
                    break;
                }
            }
XLogger.getInstance().log(Level.FINER, "Selected site: {0} for link: {1}", this.getClass(), selectedSite, link);

            if(selectedSite == null) {
// We only extract from sites for which we have configs                
                return false;
            }

            Sitetype webtype = (Sitetype)refs.getEntity(References.sitetype.web);
            
            Site site = Util.findSite(selectedSite, webtype , false);
            
            if(site == null) {
                throw new NullPointerException();
            }
            
            ResumableUrlParser parser = this.getParser();

            parser.setSitename(site.getSite());

            NodeList nodeList = parser.parse(link);

            PageNodes pageNodes = new PageNodesImpl(link, nodeList);
//@update            
            NewsCrawler.updateFeedWith(site.getSite(), webtype, feed, pageNodes, imagesFilter, "news", tolerance, parseJob);
            
            return true;
            
        }catch(ParserException e) {
            
            return false;
        }
    }
    
    private void updateFeedWith(Site site, Feed feed, PageNodes pageNodes) {

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
//if(true) {
//System.out.println("URL: "+pageNodes.getFormattedURL());
//System.out.println("Title: "+(pageNodes.getTitle()==null?null:pageNodes.getTitle().toPlainTextString()));
//    return feed;
//}        
        feed.setAuthor(sitename);
        
        feed.setCategories("status news");
        
        String content = pageNodes.getBody().getChildren().toHtml();
        
//System.out.println(this.getClass().getName()+". Content length: "+(content==null?null:content.length()));        
        
        feed.setContent(Util.truncate(Feed.class, "content", content));
        
//        feed.setDatecreated(new Date()); // set before creation
        
        if(pageNodes.getDescription() != null) {
            MetaTag meta = pageNodes.getDescription();
            feed.setDescription(Util.truncate(Feed.class, "description", meta.getAttribute("content")));
        }
        
        Link link = pageNodes.getIcon();
        if(link == null) {
            link = pageNodes.getIco();
        }
        if(link != null) {
            feed.setImageurl(link.getLink());
        }
        
        if(pageNodes.getKeywords() != null) {
            MetaTag meta = pageNodes.getKeywords();
            feed.setKeywords(Util.truncate(Feed.class, "keywords", meta.getAttribute("content")));
        }
        
        if(pageNodes.getTitle() != null) {
            TitleTag title = pageNodes.getTitle();
            feed.setTitle(title.toPlainTextString());
        }
        
        // We use this not the formatted value so 
        // that we can check for duplicates in the future
        // using methods like isInDatabase(String link)
        feed.setUrl(pageNodes.getURL());
    }
    
    private void addTrends(List<Trend> trends) {
        
        if(trends == null || trends.isEmpty()) {
            return;
        }
        
        for(Trend trend:trends) {
            
            Feed feed = new Feed();
            feed.setAuthor("twitter trends");
//@related_twitter categories
//            
            feed.setCategories("trending");
            feed.setContent(trend.getName());
//            feed.setDatecreated(new Date()); // Add just before creation
//            feed.setDescription(???);
            feed.setFeeddate(new Date());
            Site site = Util.findSite("twitter", trending, true);
            feed.setSiteid(site);
            feed.setTitle(trend.getName());
            feed.setUrl(trend.getURL());
            
            synchronized(result) {
                
XLogger.getInstance().log(Level.INFO, "Adding Twitter Trend Feed. Site: {0}, author: {1}, title: {2}\nURL: {3}", this.getClass(), 
feed.getSiteid()==null?null:feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
                
                result.add(feed);
            }
        }
    }
    
    private String getHrefFromHtmlLink(String htmlLinkTag) {
        String output = null;
        if(htmlLinkTag != null && !htmlLinkTag.isEmpty()) {
            org.htmlparser.Parser parser = new org.htmlparser.Parser();
            try{
                parser.setInputHTML(htmlLinkTag);
                org.htmlparser.util.NodeList nodes = parser.parse(null);
                if(nodes != null && !nodes.isEmpty()) {
                    // only one expected
                    org.htmlparser.Node first = nodes.get(0);
                    if(first instanceof org.htmlparser.tags.LinkTag) {
                        output = ((org.htmlparser.tags.LinkTag)first).getLink();
                    }
                }
            }catch(org.htmlparser.util.ParserException e) {
                XLogger.getInstance().log(Level.WARNING, 
                        "Failed to extract href attribute from: "+htmlLinkTag, 
                        this.getClass(), e);
            }
        }
        return output;
    }
    
    private ResumableUrlParser _parser_no_direct_access;
    private ResumableUrlParser getParser() {
        if(_parser_no_direct_access == null) {
            _parser_no_direct_access = new ResumableUrlParser(){
                private transient Feed feedToFind;
                @Override
                public boolean isResumable() {
                    return false;
                }
                @Override
                public boolean isResume() {
                    return true;
                }
                @Override
                public boolean isInDatabase(String link) {
                    if(feedToFind == null) {
                        feedToFind = new Feed();
                    }
                    feedToFind.setUrl(link);
                    EntityController<Feed, Integer> ec = this.getFeedController();
                    Map map = ec.toMap(feedToFind, false);
                    return ec.selectFirst(map) != null;
                }
                private EntityController<Feed, Integer> ec_accessViaGetter;
                private EntityController<Feed, Integer> getFeedController() {
                    if(ec_accessViaGetter == null) {
                        ec_accessViaGetter = TwitterFeedTask.this.getControllerFactory().getEntityController(Feed.class, Integer.class);
                    }
                    return ec_accessViaGetter;
                }
            };
        }
        return _parser_no_direct_access;
    }

    private ControllerFactory getControllerFactory() {
        return IdiscApp.getInstance().getControllerFactory();
    }
    
    @Override
    public Collection<Feed> getResult() {
        return result;
    }
}
