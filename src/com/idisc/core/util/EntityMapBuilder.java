package com.idisc.core.util;

import com.bc.htmlparser.ParseJob;
import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import com.bc.jpa.JpaContext;

/**
 * @author Josh
 */
public class EntityMapBuilder {
    
  private static transient final Class cls = EntityMapBuilder.class;
  private static transient final XLogger logger = XLogger.getInstance();
  
  private final boolean plainTextOnly;
  private final int maxTextLength;
  
  public EntityMapBuilder() {  
    this(false, 1000);
  }
  
  public EntityMapBuilder(boolean plainTextOnly, int maxTextLength) {  
    this.plainTextOnly = plainTextOnly;
    this.maxTextLength = maxTextLength;
  }
  
  public Map toMap(Object value, StringBuilder appendTo) {
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
    } else {
      throw new UnsupportedOperationException();
    }
  }
  
  public Map toMap(Feeduser user) {
    return toMap(user, getReusedMap(), getReusedMap1());
  }
  
  private Map toMap(Feeduser user, Map feeduserMap, Map installationMap) {
      
    List<Installation> installations = user.getInstallationList();
    
    getUserController().toMap(user, feeduserMap, false);
    
    feeduserMap.put("commentList", null);
    feeduserMap.put("feedhitList", null);
    
    logger.log(Level.FINER, "Feeduser details: {0}", cls, feeduserMap);
    
    if ((installations != null) && (!installations.isEmpty())) {
        
      Installation installation = (Installation)installations.get(installations.size() - 1);
      
      if (installation != null) {
        toMap(installation, installationMap);
      } else {
        installationMap = null;
      }
    } else {
      installationMap = null;
    }
    
    logger.log(Level.FINER, "Installation details: {0}", cls, installationMap);
    
    if (installationMap != null) {

      feeduserMap.putAll(installationMap);
    }
    
    return feeduserMap;
  }
  
  public Map toMap(Feed feed) {
    Map map = toMap(feed, getReusedMap(), getReusedMap1());
    return map;
  }

  private Map toMap(Feed feed, Map feedMap, Map siteMap) {
    
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
    
      getFeedController().toMap(feed, feedMap, false);
    
      Site site = feed.getSiteid();
    
      toMap(site, siteMap);
    
      if (siteMap != null) {
        feedMap.put("siteid", siteMap);
      }
    
      feedMap.put("hitcount", hitcount);
    
    }finally{
      feed.setContent(originalContent);
    }
    
    return feedMap;
  }
  
  public Map toMap(Comment comment) {
    Map map = toMap(comment, getReusedMap(), getReusedMap1());
    return map;
  }
  
  private Map toMap(Comment comment, Map commentMap, Map installationMap) {
      
    Installation installation = comment.getInstallationid();
    
    Feed feed = comment.getFeedid();
    
    comment.setCommentList(null);
    
    getCommentController().toMap(comment, commentMap, false);

    Map feedMap = java.util.Collections.singletonMap("feedid", feed.getFeedid());
    
    commentMap.remove("repliedto");
    
    if (feedMap != null) {
      commentMap.put("feedid", feedMap);
    }else{
      commentMap.remove("feedid");
    }
    
    toMap(installation, installationMap);
    
    if (installationMap != null) {
      commentMap.put("installationid", installationMap);
    }else{
      commentMap.remove("installationid");
    }
    
    return commentMap;
  }
  
  public Map toMap(Installation installation) {
    Map map = toMap(installation, getReusedMap());
    return map;
  }
  
  private Map toMap(Installation installation, Map installationMap) {
    if (installation != null) {

      installation.setBookmarkfeedList(null);
      installation.setCommentList(null);
      installation.setExtractedemailList(null);
      installation.setFavoritefeedList(null);
      installation.setFeedhitList(null);
      
      installation.setFeeduserid(null);
      
      getInstallationController().toMap(installation, installationMap, false);
    }
    
    return installationMap;
  }
  
  public Map toMap(Site site) {
    Map map = toMap(site, getReusedMap());
    return map;
  }
  
  private Map toMap(Site site, Map siteMap) {
    if (site != null) {
      site.setArchivedfeedList(null);
      site.setFeedList(null);
      site.setSitetypeid(null);
      getSiteController().toMap(site, siteMap, false);
    }
    return siteMap;
  }
  
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
    return getHitcount_2(feed);
  }

  private long getHitcount_0(Feed feed) {
    EntityController<Feedhit, Integer> ec = getFeedhitController();
    Map<String, Integer> params = java.util.Collections.singletonMap("feedid", feed.getFeedid());
    return ec.count(params);
  }
  
  private Long getHitcount_1(Feed feed) {
    Class entityClass = Feedhit.class;
    EntityManager em = getControllerFactory().getEntityManager(entityClass);
    CriteriaBuilder qb = em.getCriteriaBuilder();
    CriteriaQuery<Long> cq = qb.createQuery(Long.class);
    javax.persistence.criteria.Root root = cq.from(entityClass);
    cq.select(qb.count(root));
    cq.where(qb.equal(root.get("feedid"), feed.getFeedid()));
    return em.createQuery(cq).getSingleResult();
  }
  
  public long getHitcount_2(Feed feed) {
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
  
  private Map _m;
  private Map _m1;
  private ParseJob htmlParser;
  private EntityController<Feeduser, Integer> _uc;
  private EntityController<Feed, Integer> _fc;
  private EntityController<Installation, Integer> _ic;
  private EntityController<Comment, Integer> _cc;
  private EntityController<Site, Integer> _sc;
  private EntityController<Feedhit, Integer> _fhc;
  
  private Map getReusedMap() {
    if (this._m == null) {
      this._m = new HashMap(32, 0.75F);
    } else {
      this._m.clear();
    }
    return this._m;
  }
  
  private Map getReusedMap1() {
    if (this._m1 == null) {
      this._m1 = new HashMap(32, 0.75F);
    } else {
      this._m1.clear();
    }
    return this._m1;
  }

  public EntityController<Feeduser, Integer> getUserController() {
    if (this._uc == null) {
      this._uc = getControllerFactory().getEntityController(Feeduser.class, Integer.class);
    }
    return this._uc;
  }
  
  public EntityController<Feed, Integer> getFeedController() {
    if (this._fc == null) {
      this._fc = getControllerFactory().getEntityController(Feed.class, Integer.class);
    }
    return this._fc;
  }
  
  public EntityController<Installation, Integer> getInstallationController() {
    if (this._ic == null) {
      this._ic = getControllerFactory().getEntityController(Installation.class, Integer.class);
    }
    return this._ic;
  }
  
  public EntityController<Comment, Integer> getCommentController() {
    if (this._cc == null) {
      this._cc = getControllerFactory().getEntityController(Comment.class, Integer.class);
    }
    return this._cc;
  }
  
  public EntityController<Site, Integer> getSiteController() {
    if (this._sc == null) {
      this._sc = getControllerFactory().getEntityController(Site.class, Integer.class);
    }
    return this._sc;
  }
  
  public EntityController<Feedhit, Integer> getFeedhitController() {
    if (this._fhc == null) {
      this._fhc = getControllerFactory().getEntityController(Feedhit.class, Integer.class);
    }
    return this._fhc;
  }
  
  public JpaContext getControllerFactory() {
    return IdiscApp.getInstance().getJpaContext();
  }
  
  public final boolean isPlainTextOnly() {
    return this.plainTextOnly;
  }
  
  public final int getMaxTextLength() {
    return this.maxTextLength;
  }
}
