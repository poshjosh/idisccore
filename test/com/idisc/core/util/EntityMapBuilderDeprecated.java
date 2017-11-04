package com.idisc.core.util;

import com.bc.htmlparser.ParseJob;
import com.bc.jpa.util.MapBuilderForEntity;
import com.bc.util.MapBuilder;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.idisc.pu.entities.Country;
import java.util.Collections;

/**
 * @deprecated
 * @author Josh
 */
@Deprecated
public class EntityMapBuilderDeprecated {
    
  private static transient final Class cls = EntityMapBuilderDeprecated.class;
  private static transient final XLogger logger = XLogger.getInstance();
  
  private final boolean plainTextOnly;
  private final int maxTextLength;
  
  private final MapBuilder mapBuilder;
  
  private final Map<Class, Map> mapCache;
  
  public EntityMapBuilderDeprecated() {  
    this(false, 1000);
  }
  
  public EntityMapBuilderDeprecated(boolean plainTextOnly, int maxTextLength) {  
    this.plainTextOnly = plainTextOnly;
    this.maxTextLength = maxTextLength;
    mapBuilder = new MapBuilderForEntity();
    mapBuilder.methodFilter(MapBuilder.MethodFilter.ACCEPT_ALL)
            .nullsAllowed(false).maxDepth(0).maxCollectionSize(100);
    mapCache = new HashMap<>();
  }
  
  public Map toMap(Object value) {
    if ((value instanceof Feed)) {
      return toMap((Feed)value);
    } else if ((value instanceof Installation)) {
      return toMap((Installation)value);
    } else if ((value instanceof Site)) {
      return toMap((Site)value);
    } else if ((value instanceof Comment)) {
      return toMap((Comment)value);
    } else if ((value instanceof Feeduser)) {
      return toMap((Feeduser)value);
    }else if (value instanceof Country) {
      return toMap((Country)value);
    } else {
      throw new UnsupportedOperationException();
    }
  }
  
  public Map toMap(Feeduser user) {
      
    List<Installation> installations = user.getInstallationList();
    
    Map feeduserMap = this.getMapFor(Feeduser.class);
    
    this.mapBuilder.sourceType(Feeduser.class).source(user).target(feeduserMap).build();
    
    feeduserMap.put("commentList", null);
    feeduserMap.put("feedhitList", null);
    
    logger.log(Level.FINER, "Feeduser details: {0}", cls, feeduserMap);
    
    Map installationMap;
    
    if ((installations != null) && (!installations.isEmpty())) {
        
      Installation installation = (Installation)installations.get(installations.size() - 1);
      
      installationMap = installation == null ? Collections.EMPTY_MAP : toMap(installation);
      
    } else {
      installationMap = Collections.EMPTY_MAP;
    }
    feeduserMap.putAll(installationMap);
    
    logger.log(Level.FINER, "Installation details: {0}", cls, installationMap);

    return feeduserMap;
  }
  
  public Map toMap(Feed feed) {
    
    final Map feedMap = this.getMapFor(Feed.class);
      
    final String originalContent = feed.getContent();
    
    try{
        
      feed.setTitle(format(feed.getTitle()));
      feed.setKeywords(format(feed.getKeywords()));
    
      String content = isPlainTextOnly() ? getPlainText(feed.getContent()) : feed.getContent();
    
      feed.setContent(format(content));
    
      feed.setDescription(format(getPlainText(feed.getDescription())));

      String imageUrl = feed.getImageurl();
      
      if (imageUrl == null) {
        Site site = feed.getSiteid();
        if (site != null) {
          feed.setImageurl(site.getIconurl());
        } else {
          logger.log(Level.WARNING, "No site found for feed:: id: {0}, author: {1}, title: {2}", 
                  cls, feed.getFeedid(), feed.getAuthor(), feed.getTitle());
        }
      }

      feed.setCommentList(null);

      long hitcount = getHitcount(feed);
    
      feed.setFeedhitList(null);
    
      this.mapBuilder.sourceType(Feed.class).source(feed).target(feedMap).build();
    
      Site site = feed.getSiteid();
    
      Map siteMap = site == null ? Collections.EMPTY_MAP : toMap(site);
      feedMap.put("siteid", siteMap);
      
      feedMap.put("hitcount", hitcount);
      
    }finally{
      feed.setContent(originalContent);
    }
    
    return feedMap;
  }
  
  public Map toMap(Comment comment) {
      
    Installation installation = comment.getInstallationid();
    
    Feed feed = comment.getFeedid();
    
    comment.setCommentList(null);
    
    Map commentMap = this.getMapFor(Comment.class);
    
    this.mapBuilder.sourceType(Comment.class).source(comment).target(commentMap).build();

    Map feedMap = java.util.Collections.singletonMap("feedid", feed.getFeedid());
    commentMap.put("feedid", feedMap);
    
    commentMap.remove("repliedto");
    
    Map installationMap = installation == null ? Collections.EMPTY_MAP : toMap(installation);
    commentMap.put("installationid", installationMap);
    
    return commentMap;
  }
  
  public Map toMap(Installation installation) {
      
    installation.setBookmarkfeedList(null);
    installation.setCommentList(null);
    installation.setExtractedemailList(null);
    installation.setFavoritefeedList(null);
    installation.setFeedhitList(null);
      
    installation.setFeeduserid(null);
    
    installation.setApplaunchlogList(null);
      
    Map installationMap = this.getMapFor(Installation.class);
      
    this.mapBuilder.sourceType(Installation.class).source(installation).target(installationMap).build();
    
    Country country = installation.getCountryid();
    
    Map countryMap = country == null ? Collections.EMPTY_MAP : this.toMap(country);
    installationMap.put("countryid", countryMap);
    
    return installationMap;
  }
  
  public Map toMap(Site site) {
      
    site.setArchivedfeedList(null);
    site.setFeedList(null);
    site.setSitetypeid(null);
      
    Map siteMap = this.getMapFor(Site.class);
      
    this.mapBuilder.sourceType(Site.class).source(site).target(siteMap).build();
      
    Country country = site.getCountryid();
    Map countryMap = country == null ? Collections.EMPTY_MAP : this.toMap(country);
    
    siteMap.put("countryid", countryMap);
    
    return siteMap;
  }
  
  public Map toMap(Country country) {
      
    country.setInstallationList(null);
    country.setLocaladdressList(null);
    country.setSiteList(null);
      
    Map countryMap = this.getMapFor(Country.class);
    
    this.mapBuilder.sourceType(Country.class).source(country).target(countryMap).build();
    
    return countryMap;
  }
  
  private ParseJob htmlParser;
  private String getPlainText(String s) {
    if (s == null) {
      return s;
    }
    if (this.htmlParser == null) {
      this.htmlParser = new ParseJob();
    }
    String output;
    try {
      output = this.htmlParser.separator("\n\n").maxSeparators(1).comments(false).plainText(true).parse(s).toString();
    } catch (java.io.IOException e) {
      logger.log(Level.WARNING, "Error extracting plain text from: " + (s.length() <= 100 ? s : s.substring(0, 100)), cls, e);
      output = s;
    }
    return output;
  }
  
  public long getHitcount(Feed feed) {
    List<Feedhit> feedhits = feed.getFeedhitList();
    return feedhits == null ? 0L : feedhits.size();
  }
  
  protected String format(String sval) {
    return format(sval, getMaxTextLength());
  }
  
  protected String format(String sval, int maximumLength) {
    if ((sval == null) || (sval.isEmpty())) {
      return sval;
    }
    
    sval = truncate(sval, maximumLength);
    
    return sval;
  }
  
  protected String truncate(String sval, int maxLen) {
    if (sval == null) {
      return sval;
    }
    if (sval.length() <= maxLen) {
      return sval;
    }
    return sval.substring(0, maxLen);
  }
  
  private Map getMapFor(Class entityType) {
    Map map = mapCache.get(entityType);
    if(map == null) {
      map = new HashMap(32, 0.75f);  
      mapCache.put(entityType, map);
    }
    return map;
  }

  public final boolean isPlainTextOnly() {
    return this.plainTextOnly;
  }
  
  public final int getMaxTextLength() {
    return this.maxTextLength;
  }
}
