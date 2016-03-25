package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Usersitehitcount;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Pattern;

public abstract class AbstractFeedComparator
  implements Comparator<Feed>
{
  private boolean invertSort;
  private Map<Pattern, Integer> elite;
  private boolean exceptionLogged;
  
  public abstract long getAddValuePerHit();
  
  public abstract long getAddedValueMax();
  
  public Installation getInstallation()
  {
    return null;
  }
  

  public int compare(Feed o1, Feed o2)
  {
    long t1 = computeTimeForSorting(o1);
    
    long t2 = computeTimeForSorting(o2);
    
    if (!this.invertSort) {
      return t1 < t2 ? -1 : t1 > t2 ? 1 : 0;
    }
    return t1 < t2 ? 1 : t1 > t2 ? -1 : 0;
  }
  
  private long computeTimeForSorting(Feed feed)
  {
    long time = feed.getFeeddate() == null ? 0L : feed.getFeeddate().getTime();
    time += getAddedValueTime(feed);
    return time;
  }
  





  protected long getAddedValueTime(Feed feed)
  {
    long hits = countFeedHits(feed);
    long output; if (hits > 0L) {
      long addedValue = hits * getAddValuePerHit();
      output = addedValue;
    } else {
      output = 0L;
    }
    
    Installation inst = getInstallation();
    
    Site site = feed.getSiteid();
    try
    {
      output += getAddedValueFor(inst, site);
    } catch (RuntimeException e) {
      if (!this.exceptionLogged) {
        this.exceptionLogged = true;
        XLogger.getInstance().log(Level.WARNING, "Unexpected exception", getClass(), e);
      }
    }
    
    String title = feed.getTitle();
    if ((output > 0L) && (title != null) && 
      (this.elite != null) && (!this.elite.isEmpty())) {
      Set<Map.Entry<Pattern, Integer>> entries = this.elite.entrySet();
      for (Map.Entry<Pattern, Integer> entry : entries) {
        Pattern pattern = (Pattern)entry.getKey();
        Integer addedValueFactor = (Integer)entry.getValue();
        
        if (pattern.matcher(title).find())
        {
          output *= addedValueFactor.intValue();
        }
      }
    }
    

    if (output > getAddedValueMax()) {
      output = getAddedValueMax();
    }
    
    return output;
  }
  
  private long user_totalhits = -1L;
  private long numberOfSites = -1L;
  
  private final Map<Site, Long> userhits = new HashMap();
  private EntityController<Usersitehitcount, Integer> uhc_accessViaGetter;
  private EntityController<Feedhit, Integer> _fhc;
  
  protected long getAddedValueFor(Installation inst, Site site) { long output = 0L;
    
    if ((inst != null) && (site != null))
    {
      if (!this.userhits.containsKey(site))
      {


        HashMap<String, Object> params = new HashMap(2, 1.0F);
        
        params.put("site", site);
        params.put("installation", inst);
        
        EntityController<Usersitehitcount, Integer> ec = getUsersitehitcountController();
        
        long appsitehits = ec.count(params);
        
        this.userhits.put(site, Long.valueOf(appsitehits));
      }
      
      Long user_sitehits = (Long)this.userhits.get(site);
      
      if (user_sitehits != null)
      {
        if (this.user_totalhits == -1L) {
          initRequirements();
        }
        long addedVal;
        if ((user_sitehits.longValue() == 0L) || (this.user_totalhits == 0L)) {
          addedVal = 0L;
        } else {
          addedVal = user_sitehits.longValue() / this.user_totalhits * this.numberOfSites * (getAddValuePerHit() * 4L);
        }
        
        output = addedVal;
      }
    }
    
    return output;
  }
  
  private void initRequirements()
  {
    if (getInstallation() == null) {
      throw new NullPointerException();
    }
    
    ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
    
    EntityController<Feedhit, Integer> feedhitCtrl = factory.getEntityController(Feedhit.class, Integer.class);
    Map params = Collections.singletonMap("installationid", getInstallation());
    this.user_totalhits = feedhitCtrl.count(params);
    
    EntityController<Site, Integer> siteCtrl = factory.getEntityController(Site.class, Integer.class);
    this.numberOfSites = siteCtrl.count();
  }
  
  private EntityController<Usersitehitcount, Integer> getUsersitehitcountController()
  {
    if (this.uhc_accessViaGetter == null) {
      this.uhc_accessViaGetter = IdiscApp.getInstance().getControllerFactory().getEntityController(Usersitehitcount.class, Integer.class);
    }
    
    return this.uhc_accessViaGetter;
  }
  
  private int compareOld(Feed o1, Feed o2)
  {
    Date d1 = o1.getFeeddate();
    Date d2 = o2.getFeeddate();
    



    int h1 = (int)countFeedHits(o1);
    
    int h2 = (int)countFeedHits(o2);
    
    int x = compareInts(h1, h2);
    
    if (x == 0) {
      x = compareDates(d1, d2);
    }
    





    return x;
  }
  
  protected int compareInts(int hit_a, int hit_b) { 
    int x;
    if (this.invertSort) {
      x = hit_b == hit_a ? 0 : hit_b < hit_a ? -1 : 1;
    } else {
      x = hit_a == hit_b ? 0 : hit_a < hit_b ? -1 : 1;
    }
    return x;
  }
  
  protected int compareDates(Date date_a, Date date_b) { if ((date_a == null) && (date_b == null))
      return 0;
    if (date_a == null)
      return this.invertSort ? 1 : -1;
    if (date_b == null) {
      return this.invertSort ? -1 : 1;
    }
    return this.invertSort ? date_b.compareTo(date_a) : date_a.compareTo(date_b);
  }
  
  private long countFeedHits(Feed feed)
  {
    Map params = Collections.singletonMap("feedid", feed);
    return getFeedhitController().count(params);
  }
  
  public EntityController<Feedhit, Integer> getFeedhitController()
  {
    if (this._fhc == null) {
      ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
      this._fhc = cf.getEntityController(Feedhit.class, Integer.class);
    }
    return this._fhc;
  }
  
  public boolean isInvertSort() {
    return this.invertSort;
  }
  
  public void setInvertSort(boolean invertSort) {
    this.invertSort = invertSort;
  }
  
  public Map<Pattern, Integer> getElite() {
    return this.elite;
  }
  
  public void setElite(Map<Pattern, Integer> elite) {
    this.elite = elite;
  }
}
