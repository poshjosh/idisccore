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
import java.util.logging.Level;
import java.util.regex.Pattern;


/**
 * @(#)FeedComparator.java   31-Jan-2015 13:35:36
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class AbstractFeedComparator implements Comparator<Feed> {
    
    private boolean invertSort;
    
    public AbstractFeedComparator() { }

    public abstract long getAddValuePerHit();

    public abstract long getAddedValueMax();
    
    public Installation getInstallation() {
        return null;
    }
    
    @Override
    public int compare(Feed o1, Feed o2) {
        
        long t1 = this.computeTimeForSorting(o1);
        
        long t2 = this.computeTimeForSorting(o2);

        if(!invertSort) {
            return t1 > t2 ? 1 : (t1 < t2 ? -1 : 0);
        }else{
            return t1 > t2 ? -1 : (t1 < t2 ? 1 : 0);
        }
    }
    
    private long computeTimeForSorting(Feed feed) {
        long time = feed.getFeeddate() == null ? 0 : feed.getFeeddate().getTime();
        time = time + this.getAddedValueTime(feed);
        return time;
    }
    
    private boolean exceptionLogged;
    private Pattern breakingnewsPattern;
    
    protected long getAddedValueTime(Feed feed) {
        
        long output;
        
        int hits = feed.getFeedhitList() == null ? 0 : feed.getFeedhitList().size();
        if(hits > 0) {
            long addedValue =  hits * this.getAddValuePerHit();
            output = addedValue;
        }else{
            output = 0;
        }
        
        Installation inst = this.getInstallation();
        
        Site site = feed.getSiteid();

        try{
            output += this.getAddedValueFor(inst, site);
        }catch(RuntimeException e) {
            if(!exceptionLogged) {
                exceptionLogged = true;
                XLogger.getInstance().log(Level.WARNING, "Unexpected exception", this.getClass(), e);
            }
        }
        
        String title = feed.getTitle(); 
        if(output > 0 && title != null) {
            if(breakingnewsPattern == null) {
                // breaking
                // B-R-E-A-K-I-N-G
                // 
                breakingnewsPattern = Pattern.compile("b\\W{0,1}r\\W{0,1}e\\W{0,1}a\\W{0,1}k\\W{0,1}i\\W{0,1}n\\W{0,1}g", Pattern.CASE_INSENSITIVE);
            }
            // having breaking in the title multiplies the added value by 2
            if(breakingnewsPattern.matcher(title).find()) {
                output = output * 2;
            }
        }
        
        if(output > this.getAddedValueMax()) {
            output = this.getAddedValueMax();
        }
        
        return output;
    }
    
    private long user_totalhits = -1;
    private long numberOfSites = -1;
    
    private final Map<Site, Long> userhits = new HashMap<>(); 
    
    protected long getAddedValueFor(Installation inst, Site site) {
        
        long output = 0;
        
        if(inst != null && site != null) {

            if(!userhits.containsKey(site)) {
                
                // load userhitcounts from database
                
                HashMap<String, Object> params = new HashMap<>(2, 1.0f);
                
                params.put("site", site);
                params.put("installation", inst);
                
                EntityController<Usersitehitcount, Integer> ec = this.getUsersitehitcountController();

                long appsitehits = ec.count(params);
                
                userhits.put(site, appsitehits);
            }
            
            Long user_sitehits = userhits.get(site);

            if(user_sitehits != null) {

                if(user_totalhits == -1) {
                    this.initRequirements();
                }
                
                long addedVal;
                if(user_sitehits == 0 || user_totalhits == 0) {
                    addedVal = 0;
                }else{
                    addedVal = (user_sitehits/user_totalhits * numberOfSites) * (this.getAddValuePerHit() * 4);
                }

                output = addedVal;
            }
        }
        
        return output;
    }
    
    private void initRequirements() {
        
        if(this.getInstallation() == null) {
            throw new NullPointerException();
        }
        
        ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
        
        EntityController<Feedhit, Integer> feedhitCtrl = factory.getEntityController(Feedhit.class, Integer.class);
        Map params = Collections.singletonMap("installationid", this.getInstallation());
        this.user_totalhits = feedhitCtrl.count(params);
        
        EntityController<Site, Integer> siteCtrl = factory.getEntityController(Site.class, Integer.class);
        this.numberOfSites = siteCtrl.count();
    }
    
    private EntityController<Usersitehitcount, Integer> uhc_accessViaGetter;
    private EntityController<Usersitehitcount, Integer> getUsersitehitcountController() {
        if(uhc_accessViaGetter == null) {
            uhc_accessViaGetter = IdiscApp.getInstance().getControllerFactory().getEntityController(
                    Usersitehitcount.class, Integer.class);
        }
        return uhc_accessViaGetter;
    }
    
    private int compareOld(Feed o1, Feed o2) {
        
        Date d1 = o1.getFeeddate();
        Date d2 = o2.getFeeddate();
        
// Uncommented due to performance issues
//        
        int h1 = o1.getFeedhitList() == null ? 0 : o1.getFeedhitList().size();
        int h2 = o2.getFeedhitList() == null ? 0 : o2.getFeedhitList().size();
        
        
        int x = this.compareInts(h1, h2);
        
        if(x == 0) {
            x = this.compareDates(d1, d2);
        }
        
//if(o1.getFeedid() > o2.getFeedid() && x < 0 ||
//        o1.getFeedid() < o2.getFeedid() && x > 0) {        
//XLogger.getInstance().log(Level.INFO, "{0} is {1} {2}", this.getClass(),
//o1.getFeedid(), x > 0 ? "greater than" : x == 0 ? "equals to" : "less than", o2.getFeedid());
//}
        return x;
    }

    protected int compareInts(int hit_a, int hit_b) {
        int x;
        if(invertSort) {
            x = hit_b < hit_a ? -1 : (hit_b == hit_a ? 0 : 1);
        }else{
            x = hit_a < hit_b ? -1 : (hit_a == hit_b ? 0 : 1);
        }
        return x;
    }
    protected int compareDates(Date date_a, Date date_b) {
        if(date_a == null && date_b == null) {
            return 0;
        }else if(date_a == null){
            return invertSort ? 1 : -1;
        }else if(date_b == null) {
            return invertSort ? -1 : 1;
        }else{
            return invertSort ? date_b.compareTo(date_a) : date_a.compareTo(date_b);
        }
    }

    public boolean isInvertSort() {
        return invertSort;
    }

    public void setInvertSort(boolean invertSort) {
        this.invertSort = invertSort;
    }
}
