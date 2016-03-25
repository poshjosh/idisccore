package com.idisc.core.web;

import com.bc.htmlparser.ParseJob;
import com.bc.json.config.JsonConfig;
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
import com.idisc.core.AppProperties;
import com.idisc.core.FeedUpdateTask;
import com.idisc.pu.entities.one.Feed_;
import com.scrapper.Crawler;
import com.scrapper.config.Config;
import com.scrapper.context.CapturerContext;
import com.scrapper.filter.DefaultUrlFilter;
import com.scrapper.tag.Link;
import com.scrapper.util.PageNodes;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.htmlparser.NodeFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.MetaTag;
import org.htmlparser.tags.TitleTag;

/**
 * @(#)NewsCrawler.java   29-Nov-2014 14:19:05
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
public class NewsCrawler 
        extends Crawler
        implements TaskHasResult<Collection<Feed>>{

    private final float tolerance;
    
    private final Sitetype sitetype;
    
    private final Collection<Feed> result;
    
    private transient Feed feedToFind;
    
    public NewsCrawler(
            JsonConfig config, 
            Collection<Feed> resultsBuffer) {
        this(IdiscApp.getInstance().getCapturerApp().getConfigFactory().getContext(config), resultsBuffer);
    }
    
    public NewsCrawler(
            CapturerContext context, 
            Collection<Feed> resultsBuffer) {
        
        super(context);
        
        com.bc.manager.Filter<String> urlFilter = context.getCaptureUrlFilter();
        if(urlFilter instanceof DefaultUrlFilter) {
            // Only works if url has explicit and complete date. see method docs 
            // 
            ((DefaultUrlFilter)urlFilter).setMaxAgeDays(2);
        }

XLogger.getInstance().log(Level.FINER, "Creating", this.getClass());

        Configuration config = IdiscApp.getInstance().getConfiguration();

        tolerance = config.getFloat(AppProperties.TOLERANCE, 0.0f);
        
XLogger.getInstance().log(Level.FINER, "Tolerance: {0}", this.getClass(), tolerance);

        result = resultsBuffer;
        this.setParseLimit(this.getLimit(Config.Extractor.parseLimit));
        this.setCrawlLimit(this.getLimit(Config.Extractor.crawlLimit));
        
XLogger.getInstance().log(Level.FINER, "Updated parse limit: {0}, crawl limit: {1}", 
        this.getClass(), this.getParseLimit(), this.getCrawlLimit());

        EnumReferences refs = NewsCrawler.this.getControllerFactory().getEnumReferences();
        sitetype = (Sitetype)refs.getEntity(References.sitetype.web); 
XLogger.getInstance().log(Level.FINE, "Done creating: {0}", this.getClass(), NewsCrawler.this);

        this.setBatchInterval(0);
    }
    
    @Override
    public boolean isInDatabase(String link) {
//        if(feedToFind == null) {
//            feedToFind = new Feed();
//        }
//        feedToFind.setUrl(link);
        EntityController<Feed, Integer> ec = this.getFeedController();
//        Map map = ec.toMap(feedToFind, false);
//        boolean found = ec.selectFirst(map) != null;
        boolean found = ec.selectFirst(Feed_.url.getName(), link) != null;
if(found) {
    XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, "Link is already in database: {0}", this.getClass(), link);
}
        return found;
    }
    
    private int getLimit(Config.Extractor name) {
        Integer limit = this.getContext().getConfig().getInt(name);
        if(limit == null) {
            throw new NullPointerException("Required config value: "+name+" == null");
        }
        return limit;
    }
    
    @Override
    protected void doRun() {
        
        final int scrappLimit = this.getLimit(Config.Extractor.scrappLimit);
        
        int scrapped = 0;
        
        NodeFilter filter0 = new NodeClassFilter(ImageTag.class);
        NodeFilter filter1 = new TagNameFilter("IMG");
        NodeFilter imagesFilter = new OrFilter(new NodeFilter[]{filter0, filter1});
        
        ParseJob parseJob = new ParseJob();
        
        while(this.hasNext() && (scrappLimit<=0 || scrapped<scrappLimit)) {

            if(this.isStopInitiated()) {
                break;
            }
            
            PageNodes pageNodes = this.next();
            
XLogger.getInstance().log(Level.FINE, "PageNodes: {0}", this.getClass(), pageNodes);                

            if(pageNodes == null || pageNodes.getBody() == null) {
                continue;
            }

//@todo Look for a better algorithm. This was done because a site was noticed to return 
//            
            if(pageNodes.getTitle() != null && pageNodes.getTitle().toPlainTextString().toLowerCase().contains("400 bad request")) {
                continue;
            }
            
            if(pageNodes.getURL().equals(this.getStartUrl())) {
                continue;
            }

            Feed feed = this.toFeed(pageNodes, imagesFilter, parseJob);
            
            synchronized(result) {
XLogger.getInstance().log(FeedUpdateTask.LOG_LEVEL, 
        "Adding Web Feed. Site {0}, author: {1}, title: {2}\nURL: {3}", this.getClass(), 
feed.getSiteid()==null?null:feed.getSiteid().getSite(), feed.getAuthor(), feed.getTitle(), feed.getUrl());
                result.add(feed);
            }

            ++scrapped;
        }
    }
    
    private Feed toFeed(PageNodes pageNodes, NodeFilter imagesFilter, ParseJob parseJob) {

        Feed feed = new Feed();
        
        updateFeedWith(this.getSitename(), sitetype, feed, pageNodes, imagesFilter, "news", tolerance, parseJob);
        
        return feed;
    }

    public static void updateFeedWith(
            String sitename, Sitetype sitetype, Feed feed, 
            PageNodes pageNodes, NodeFilter imagesFilter, 
            String defaultCategories, float tolerance, ParseJob parseJob) {
        Site site = Util.findSite(sitename, sitetype, true);
        if(site == null) {
            throw new NullPointerException();
        }
        updateFeedWith(site, feed, pageNodes, imagesFilter, defaultCategories, tolerance, parseJob);
    }

//@update    
    public static void updateFeedWith(
            Site site, Feed feed, PageNodes pageNodes, NodeFilter imagesFilter,
            String defaultCategories, float tolerance, ParseJob parseJob) {

        feed.setSiteid(site);
        
        final String sitename = site.getSite();
        
        NodeExtractor extractor = new NodeExtractor(tolerance, sitename);
        
        Map<String, String> extract = extractor.extract(pageNodes);
        
//if(true) {
//System.out.println("URL: "+pageNodes.getFormattedURL());
//System.out.println("Title: "+(pageNodes.getTitle()==null?null:pageNodes.getTitle().toPlainTextString()));
//System.out.println("Extract: "+extract);
//    return feed;
//}        
        JsonConfig config = extractor.getContext().getConfig();
        Map defaultValues = config.getMap(Config.Formatter.defaultValues);

////////////// Author        
        String author = getValue(extract, "author", defaultValues, parseJob);
        if(author == null || author.trim().isEmpty()) {
            author = sitename;
        }
XLogger.getInstance().log(Level.FINER, "Author. {0} = {1}", 
        NewsCrawler.class, extract.get("author"), author);
        feed.setAuthor(author);
        
////////////// Categories        
        String categories = getValue(extract, "categories", defaultValues, parseJob);
        if(categories == null) {
            categories = defaultCategories;
        }
        feed.setCategories(categories);
        
////////////// Content        
        String content = extract.get("content");
        feed.setContent(Util.truncate(Feed.class, "content", content));
        
//        feed.setDatecreated(new Date()); // set before creation

////////////// Description        
        String description = getValue(extract, "description", defaultValues, parseJob);
        if(description == null) {
            Boolean descriptionIsGeneric = getBoolean(config, Config.Extractor.isDescriptionGeneric);
            if(descriptionIsGeneric) {
                description = format(content, (String)defaultValues.get("description"), 300, parseJob);
            }else{
                if(pageNodes.getDescription() != null) {
                    MetaTag meta = pageNodes.getDescription();
                    description = Util.truncate(Feed.class, "description", meta.getAttribute("content"));
                }
            }
        }
        feed.setDescription(description);
XLogger.getInstance().log(Level.FINER, "Description. {0} = {1}", 
        NewsCrawler.class, extract.get("description"), feed.getDescription());
        
////////////// Feeddate
        String dateStr = extract.get("feeddate");
        if(dateStr != null) {
            dateStr = getPlainText(dateStr.trim(), parseJob);
        }

        Date feeddate = null;
        if(dateStr != null && !dateStr.isEmpty()) {
            
            String [] datePatterns = extractor.getDatePatterns();

XLogger.getInstance().log(Level.FINER, "Date patterns: {0}", NewsCrawler.class, 
        datePatterns == null ? null : Arrays.toString(datePatterns));
            
            if(datePatterns != null && datePatterns.length != 0) {
                SimpleDateFormat df = new SimpleDateFormat();
                for(String pattern:datePatterns) {
                    df.applyPattern(pattern);
                    try{
                        
                        feeddate = df.parse(dateStr);
                        
XLogger.getInstance().log(Level.FINER, "Parsed date: {0}", NewsCrawler.class, feeddate);

                        break;
                    }catch(ParseException ignored) { }
                }
            }
        }
        if(feeddate == null){
            feeddate = new Date(); // We need this for sorting
        }
XLogger.getInstance().log(Level.FINER, "Feeddate. {0} = {1}", 
        NewsCrawler.class, extract.get("feeddate"), feeddate);
        feed.setFeeddate(feeddate); 

////////////// Imageurl
        String imageurl = extract.get("imageurl");
        if(imageurl != null) {
            feed.setImageurl(imageurl);
        }else{
            if(imagesFilter != null) {
                String imageUrl = Util.getFirtImageUrl(pageNodes.getNodeList(), imagesFilter);
                if(imageUrl != null) {
                    feed.setImageurl(imageUrl);
                }
            }
        }
        if(feed.getImageurl() == null) {
            Link link = pageNodes.getIcon();
            if(link == null) {
                link = pageNodes.getIco();
            }
            if(link != null) {
                feed.setImageurl(link.getLink());
            }
        }
XLogger.getInstance().log(Level.FINER, "Imageurl. {0} = {1}", 
        NewsCrawler.class, extract.get("imageurl"), feed.getImageurl());

////////////// Title        
        String keywords = getValue(extract, "keywords", defaultValues, parseJob);
        if(keywords == null) {
            if(pageNodes.getKeywords() != null) {
                MetaTag meta = pageNodes.getKeywords();
                keywords = Util.truncate(Feed.class, "keywords", meta.getAttribute("content"));
            }
        }
        feed.setKeywords(keywords);
        
////////////// Title
        String title = getValue(extract, "title", defaultValues, parseJob);
        if(title == null) {
            Boolean titleIsInUrl = getBoolean(config, Config.Extractor.isTitleInUrl);
            if(titleIsInUrl) {
                String url = pageNodes.getURL();
                title = extractTitleFromUrl(url);
            }
            if(title == null) {
                Boolean titleIsGeneric = getBoolean(config, Config.Extractor.isTitleGeneric);
                if(titleIsGeneric) {
                    title = format(content, null, Util.getColumnDisplaySize(Feed.class, "title"), parseJob);
                }else{
                    if(pageNodes.getTitle() != null) {
                        TitleTag titleTag = pageNodes.getTitle();
                        title = titleTag.toPlainTextString();
                    }
                }
            }
        }
        feed.setTitle(title);
        
        // We use this not the formatted value so 
        // that we can check for duplicates in the future
        // using methods like isInDatabase(String link)
        feed.setUrl(pageNodes.getURL());
    }
    
    private static String getValue(Map<String, String> extract, String col, Map defaultValues, ParseJob parseJob) {
        
        String val = extract.get(col);
        
        if(val != null) {
            
            val = format(col, val, defaultValues, parseJob);
        }
        
        return val;
    }
    
    private static String format(String col, String val, Map defaultValues, ParseJob parseJob) {
        
        int maxLen = Util.getColumnDisplaySize(Feed.class, col) - 3;
        
        return format(val, (String)defaultValues.get(col), maxLen, parseJob);
    }
    
//@update    
    public static final String format(
            String s, String defaultValue, int maxLen, ParseJob pj) {
        if(s == null) {
            s = defaultValue;
        }
        if(pj != null) {
            s = getPlainText(s, pj);
            if(s == null) {
                s = defaultValue;
            }
        }
        return Util.truncate(s, maxLen);
    }

//@update    
    private static String getPlainText(String s, ParseJob pj) {
        return getPlainText(s, 1, pj);
    }
    
//@update    
    private static String getPlainText(String s, int spaces, ParseJob pj) {
        String output;
        pj.reset();
        try{
            pj.comments(false).formatter(null).html(false).innerHtml(false
            ).maxSeparators(spaces).plainText(true).tagFilter(null);
            StringBuilder sb = pj.parse(s);
            output = sb == null || sb.length() == 0 ? null : sb.toString();
        }catch(IOException e) {
            output = null;
        }
        return output;
    }
    
    private static String extractTitleFromUrl(String url) {
//   .../the-government-has-done-it-again.html            
// we extract: the government has done it again            
        url = url.trim();
        if(url.endsWith("/")) {
            url = url.substring(0, url.length()-1);
        }
        String title;
        int a = url.lastIndexOf('/');
        if(a != -1) {
//            int b = url.lastIndexOf('.', a); // This counts backwards from a
            int b = url.lastIndexOf('.');
            if(b == -1 || b < a) {
                b = url.length();
            }
            title = url.substring(a+1, b);
            title = title.replaceAll("\\W", " "); //replace all non word chars
        }else{
            title = null;
        }
        return title;
    }
    
    private static Boolean getBoolean(JsonConfig config, Config.Extractor key) {
        Boolean bval = config.getBoolean(key);
        if(bval == null) {
            bval = Boolean.FALSE;
        }
        return bval;
    }
    
    @Override
    public Collection<Feed> getResult() {
        return result;
    }
    
    private EntityController<Feed, Integer> ec_accessViaGetter;
    private EntityController<Feed, Integer> getFeedController() {
        if(ec_accessViaGetter == null) {
            ec_accessViaGetter = this.getControllerFactory().getEntityController(Feed.class, Integer.class);
        }
        return ec_accessViaGetter;
    }
    
    private ControllerFactory getControllerFactory() {
        return IdiscApp.getInstance().getControllerFactory();
    }

    @Override
    public boolean isResumable() {
        // We save the feeds manually so this should be false
        return false;
    }
    @Override
    public boolean isResume() {
        // true, ensures isInDatabase(String link) is called
        return true;
    }
}
/**
 * 
    @Override
    protected boolean isToBeCrawled(String link) {
        boolean toBeCrawled = super.isToBeCrawled(link); 
System.out.println("To be crawled: "+toBeCrawled+", crawled: "+this.getCrawled()+" link: "+link);        
        return toBeCrawled;
    }

    @Override
    public PageNodes next() {
System.out.println(this.getClass().getName()+". next");        
        PageNodes next = super.next(); 
System.out.println(this.getClass().getName()+". next: "+next);        
        return next;
    }

    @Override
    public boolean hasNext() {
System.out.println(this.getClass().getName()+". hasNext");        
        boolean hasNext = super.hasNext(); 
System.out.println(this.getClass().getName()+". hasNext: "+hasNext);        
        return hasNext;
    }

    @Override
    protected void postParse(PageNodes page) {
        super.postParse(page);
System.out.println(this.getClass().getName()+". postParse");        
    }

    @Override
    protected void preParse(String url) {
        super.preParse(url);
System.out.println(this.getClass().getName()+". preParse");        
    }

    @Override
    protected NodeList doParse(String url) throws ParserException {
System.out.println(this.getClass().getName()+". Parsing: "+url);        
        NodeList nodeList = super.doParse(url); 
System.out.println(this.getClass().getName()+". Parse result: "+(nodeList==null?null:nodeList.size()));        
        return nodeList;
    }
 * 
 */