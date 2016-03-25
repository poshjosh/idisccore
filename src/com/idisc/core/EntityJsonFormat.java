package com.idisc.core;

import com.bc.htmlparser.ParseJob;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
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
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

public class EntityJsonFormat extends com.bc.util.JsonFormat
{
  private boolean plainTextOnly;
  private int maxTextLength;
  private Map _m;
  private Map _m1;
  private ParseJob htmlParser;
  private EntityController<Feeduser, Integer> _uc;
  private EntityController<Feed, Integer> _fc;
  private EntityController<Installation, Integer> _ic;
  private EntityController<Comment, Integer> _cc;
  private EntityController<Site, Integer> _sc;
  private EntityController<Feedhit, Integer> _fhc;
  
  private Map getReusedMap()
  {
    if (this._m == null) {
      this._m = new HashMap(20, 0.75F);
    } else {
      this._m.clear();
    }
    return this._m;
  }
  
  private Map getReusedMap1()
  {
    if (this._m1 == null) {
      this._m1 = new HashMap(20, 0.75F);
    } else {
      this._m1.clear();
    }
    return this._m1;
  }
  














  public void appendJSONString(Object value, StringBuilder appendTo)
  {
    if ((value instanceof Feed)) {
      appendJsonString((Feed)value, appendTo);
    } else if ((value instanceof Installation)) {
      appendJsonString((Installation)value, appendTo);
    } else if ((value instanceof Comment)) {
      appendJsonString((Comment)value, appendTo);
    } else if ((value instanceof Site)) {
      appendJsonString((Site)value, appendTo);
    } else if ((value instanceof Feeduser)) {
      appendJsonString((Feeduser)value, appendTo);
    } else {
      super.appendJSONString(value, appendTo);
    }
  }
  
  public void appendJsonString(Feeduser user, StringBuilder appendTo) {
    Map map = toMap(user);
    appendJSONString(map, appendTo);
  }
  
  public Map toMap(Feeduser user) {
    return toMap(user, getReusedMap(), getReusedMap1());
  }
  
  private Map toMap(Feeduser user, Map feeduserMap, Map installationMap)
  {
    List<Installation> installations = user.getInstallationList();
    
    getUserController().toMap(user, feeduserMap, false);
    
    feeduserMap.put("commentList", null);
    feeduserMap.put("feedhitList", null);
    

    XLogger.getInstance().log(Level.FINER, "Feeduser details: {0}", getClass(), feeduserMap);
    
    if ((installations != null) && (!installations.isEmpty()))
    {
      Installation installation = (Installation)installations.get(installations.size() - 1);
      
      if (installation != null) {
        toMap(installation, installationMap);
      } else {
        installationMap = null;
      }
    } else {
      installationMap = null;
    }
    
    XLogger.getInstance().log(Level.FINER, "Installation details: {0}", getClass(), installationMap);
    
    if (installationMap != null)
    {

      feeduserMap.putAll(installationMap);
    }
    
    return feeduserMap;
  }
  
  public void appendJsonString(Feed feed, StringBuilder appendTo) {
    Map map = toMap(feed);
    appendJSONString(map, appendTo);
  }
  
  public Map toMap(Feed feed) {
    Map map = toMap(feed, getReusedMap(), getReusedMap1());
    return map;
  }
  


  private Map toMap(Feed feed, Map feedMap, Map siteMap)
  {
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
        XLogger.getInstance().log(Level.WARNING, "No site found for feed:: id: {0}, author: {1}, title: {2}", getClass(), feed.getFeedid(), feed.getAuthor(), feed.getTitle());
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
    
    feedMap.put("hitcount", Long.valueOf(hitcount));
    
    return feedMap;
  }
  
  public void appendJsonString(Comment comment, StringBuilder appendTo) {
    Map map = toMap(comment);
    appendJSONString(map, appendTo);
  }
  
  public Map toMap(Comment comment) {
    Map map = toMap(comment, getReusedMap(), getReusedMap1());
    return map;
  }
  
  private Map toMap(Comment comment, Map commentMap, Map installationMap)
  {
    Installation installation = comment.getInstallationid();
    
    Feed feed = comment.getFeedid();
    



    comment.setFeedid(null);
    comment.setInstallationid(null);
    comment.setRepliedto(null);
    
    comment.setCommentList(null);
    
    getCommentController().toMap(comment, commentMap, false);
    


    Map feedMap = java.util.Collections.singletonMap("feedid", feed.getFeedid());
    
    if (feedMap != null) {
      commentMap.put("feedid", feedMap);
    }
    
    toMap(installation, installationMap);
    
    if (installationMap != null) {
      commentMap.put("installationid", installationMap);
    }
    
    return commentMap;
  }
  
  public void appendJsonString(Installation installation, StringBuilder appendTo) {
    Map map = toMap(installation);
    appendJSONString(map, appendTo);
  }
  
  public Map toMap(Installation installation) {
    Map map = toMap(installation, getReusedMap());
    return map;
  }
  
  private Map toMap(Installation installation, Map installationMap)
  {
    if (installation != null)
    {


      installation.setBookmarkfeedList(null);
      installation.setCommentList(null);
      installation.setExtractedemailList(null);
      installation.setFavoritefeedList(null);
      installation.setFeedhitList(null);
      installation.setUsersitehitcountList(null);
      
      installation.setFeeduserid(null);
      
      getInstallationController().toMap(installation, installationMap, false);
    }
    
    return installationMap;
  }
  
  public void appendJsonString(Site site, StringBuilder appendTo) {
    Map map = toMap(site);
    appendJSONString(map, appendTo);
  }
  
  public Map toMap(Site site) {
    Map map = toMap(site, getReusedMap());
    return map;
  }
  
  private Map toMap(Site site, Map siteMap)
  {
    if (site != null)
    {


      site.setArchivedfeedList(null);
      site.setFeedList(null);
      site.setSitetypeid(null);
      site.setUsersitehitcountList(null);
      
      getSiteController().toMap(site, siteMap, false);
    }
    
    return siteMap;
  }
  
  private String getPlainText(String s)
  {
    if (s == null) {
      return s;
    }
    if (this.htmlParser == null) {
      this.htmlParser = new ParseJob();
    }
    String output;
    try
    {
      output = this.htmlParser.separator("\n\n").maxSeparators(1).comments(false).plainText(true).parse(s).toString();
    } catch (java.io.IOException e) {
      XLogger.getInstance().log(Level.WARNING, "Error extracting plain text from: " + (s.length() <= 100 ? s : s.substring(0, 100)), getClass(), e);
      
      output = s;
    }
    return output;
  }
  
  public long getHitcount(Feed feed) {
    return getHitcount_2(feed);
  }
  








  private long getHitcount_0(Feed feed)
  {
    EntityController<Feedhit, Integer> ec = getFeedhitController();
    
    Map<String, Integer> params = java.util.Collections.singletonMap("feedid", feed.getFeedid());
    return ec.count(params);
  }
  
  private long getHitcount_1(Feed feed) {
    Class entityClass = Feedhit.class;
    EntityManager em = getControllerFactory().getEntityManager(entityClass);
    CriteriaBuilder qb = em.getCriteriaBuilder();
    CriteriaQuery<Long> cq = qb.createQuery(Long.class);
    javax.persistence.criteria.Root root = cq.from(entityClass);
    cq.select(qb.count(root));
    
    cq.where(qb.equal(root.get("feedid"), feed.getFeedid()));
    return ((Long)em.createQuery(cq).getSingleResult()).longValue();
  }
  
  public long getHitcount_2(Feed feed) {
    List<Feedhit> feedhits = feed.getFeedhitList();
    return feedhits == null ? 0L : feedhits.size();
  }
  
  protected String format(String sval)
  {
    return format(sval, getMaxTextLength());
  }
  
  protected String format(String sval, int maximumLength)
  {
    if ((sval == null) || (sval.isEmpty())) {
      return sval;
    }
    
    sval = truncate(sval, maximumLength);
    
    boolean old = org.htmlparser.util.Translate.DECODE_LINE_BY_LINE;
    try {
      org.htmlparser.util.Translate.DECODE_LINE_BY_LINE = true;
      sval = org.htmlparser.util.Translate.decode(sval);
    } finally {
      org.htmlparser.util.Translate.DECODE_LINE_BY_LINE = old;
    }
    
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
  
  public EntityController<Feeduser, Integer> getUserController()
  {
    if (this._uc == null) {
      this._uc = getControllerFactory().getEntityController(Feeduser.class, Integer.class);
    }
    return this._uc;
  }
  
  public EntityController<Feed, Integer> getFeedController()
  {
    if (this._fc == null) {
      this._fc = getControllerFactory().getEntityController(Feed.class, Integer.class);
    }
    return this._fc;
  }
  
  public EntityController<Installation, Integer> getInstallationController()
  {
    if (this._ic == null) {
      this._ic = getControllerFactory().getEntityController(Installation.class, Integer.class);
    }
    return this._ic;
  }
  
  public EntityController<Comment, Integer> getCommentController()
  {
    if (this._cc == null) {
      this._cc = getControllerFactory().getEntityController(Comment.class, Integer.class);
    }
    return this._cc;
  }
  
  public EntityController<Site, Integer> getSiteController()
  {
    if (this._sc == null) {
      this._sc = getControllerFactory().getEntityController(Site.class, Integer.class);
    }
    return this._sc;
  }
  
  public EntityController<Feedhit, Integer> getFeedhitController()
  {
    if (this._fhc == null) {
      this._fhc = getControllerFactory().getEntityController(Feedhit.class, Integer.class);
    }
    return this._fhc;
  }
  
  public ControllerFactory getControllerFactory() {
    return IdiscApp.getInstance().getControllerFactory();
  }
  
  public boolean isPlainTextOnly() {
    return this.plainTextOnly;
  }
  
  public void setPlainTextOnly(boolean plainTextOnly) {
    this.plainTextOnly = plainTextOnly;
  }
  
  public int getMaxTextLength() {
    return this.maxTextLength;
  }
  
  public void setMaxTextLength(int maxTextLength) {
    this.maxTextLength = maxTextLength;
  }
}
