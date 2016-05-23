package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.idisc.pu.entities.Feedhit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.junit.Test;

/**
 * @author poshjosh
 */
public class CountFeedhits extends IdiscTestBase {

    public CountFeedhits() throws Exception {
        super(Level.FINER);
    }
    
    @Test
    public void test() {
        final int [] installationids = {2, 2, 2};
        final int [] siteids = {3, 4, 5};
        for(int i=0; i<installationids.length; i++) {
            Number feedhitCount = this.countFeedhits(installationids[i], siteids[i]);
System.out.println("= x = x = x = x = Siteid: "+siteids[i]+ ", hitcount: "+feedhitCount);        
        }  
    }

    EntityManager em;
    Query query;
    public Number countFeedhits(Integer installationid, Integer siteid) { 
        
        ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
        
        try{
            if(em == null) {
                em = cf.getEntityManager(Feedhit.class);
                String queryString = "SELECT COUNT(t3.feedhitid) FROM site t0, installation t1, feed t2, feedhit t3 WHERE t0.siteid = ?1 AND t1.installationid = ?2 AND t2.siteid = ?3 AND t1.installationid = t3.installationid AND t2.feedid = t3.feedid";
                query = em.createNativeQuery(queryString);
            }
            query.setParameter(1, siteid);
            query.setParameter(2, installationid);
            query.setParameter(3, siteid);
            return (Number)query.getSingleResult();
        }finally{
//            em.close();
        }
    }
}
// COUNT NUMBER OF FEEDHITS FOR A PARTICULAR SITE e.g site with siteid = '28'
//SELECT COUNT(t2.feedhitid) FROM site t0, feed t1, feedhit t2
//WHERE t0.siteid = '28'
//AND t1.siteid = '28'
//AND t1.feedid = t2.feedid;

// COUNT NUMBER OF FEEDHITS FOR A PARTICULAR SITE AND INSTALLATION 
// e.g site with siteid = '28', installation with installationid = '2'
//SELECT COUNT(t3.feedhitid) FROM site t0, installation t1, feed t2, feedhit t3
//WHERE t0.siteid = '28'
//AND t1.installationid = '2'
//AND t2.siteid = '28'
//AND t1.installationid = t3.installationid
//AND t2.feedid = t3.feedid;
