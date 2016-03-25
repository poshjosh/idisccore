package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.JPQL;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feedhit;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Usersitehitcount;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.config.QueryHints;


/**
 * @(#)Updateusersitehitcounts.java   21-Apr-2015 15:40:26
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
public class Updateusersitehitcounts implements Runnable, Serializable {
    
    private int offset;
    
    private final int batchSize;

    public Updateusersitehitcounts() {
        this(100);
    }

    public Updateusersitehitcounts(int batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public void run() {
XLogger xlogger = XLogger.getInstance();
        ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
        
        JPQL<Site> jpql = factory.getJpql(Site.class);
        
        List<Site> sites = jpql.getResultList((Map)null, null, (Map)null, -1, -1, false);
        
xlogger.log(Level.FINER, "Found {0} sites", this.getClass(), sites == null ? null : sites.size());        
        if(sites == null || sites.isEmpty()) {
            return;
        }

        int found_count;
        do{
            
            List<Installation> inst_list = this.getInstallations(factory, offset, batchSize);
            
            found_count = inst_list == null ? 0 : inst_list.size();

xlogger.log(Level.FINER, "Found {0} installations", this.getClass(), found_count);

            this.offset += found_count;

// SELECT ... from feeds
            
            if(found_count < 1) {

                break;
            }

long tb4 = System.currentTimeMillis();
long mb4 = Runtime.getRuntime().freeMemory();

            int [] hitcounts = new int[sites.size()];
            
            for(Installation inst:inst_list) {

xlogger.log(Level.INFO, "Computing site counts for installation: {0}", this.getClass(), inst);

                List<Feedhit> feedhit_list = inst.getFeedhitList();

                Arrays.fill(hitcounts, 0);

                for(Feedhit feedhit:feedhit_list) {

                    Site site = feedhit.getFeedid().getSiteid();

                    int index = sites.indexOf(site);

                    hitcounts[index] = hitcounts[index] + 1;
                }

xlogger.log(Level.INFO, "Consumed memory: {0}, time: {1}", this.getClass(), 
mb4-Runtime.getRuntime().freeMemory(), System.currentTimeMillis()-tb4);

                Date reusedDate = new Date();
                Usersitehitcount reusedEntity = new Usersitehitcount();

                EntityManager em = factory.getEntityManager(Usersitehitcount.class);

                try{
                   
                    em.getTransaction().begin();

                    for(Site site:sites) {

                        int index = sites.indexOf(site);
                        int hits = hitcounts[index];

                        if(hits == 0) {
                            continue;
                        }

                        reusedEntity.setDatecreated(reusedDate);
                        reusedEntity.setHitcount(hits);
                        reusedEntity.setInstallation(inst);
                        reusedEntity.setSite(site);

                        em.merge(reusedEntity);
//                        em.persist(reusedEntity);
//                        em.detach(reusedEntity);
xlogger.log(Level.FINER, "Installation: {0}, site: {1}, count: {2}", this.getClass(), inst, site, hits);
                    }

                    em.getTransaction().commit();

xlogger.log(Level.INFO, "Consumed memory: {0}, time: {1}", this.getClass(), 
mb4-Runtime.getRuntime().freeMemory(), System.currentTimeMillis()-tb4);
                    
                }finally{
                    em.close();
                }
            }
        }while(found_count >= batchSize);
    }
    
    public List<Installation> getInstallations(ControllerFactory factory, int off, int limit) {
        
//        EntityController<Installation, Integer> instCtrl = factory.getEntityController(Installation.class, Integer.class);

//        List<Installation> inst_list = instCtrl.find(this.batchSize, this.offset);
        
        EntityManager em = factory.getEntityManager(Installation.class);
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        
        CriteriaQuery<Installation> cq = cb.createQuery(Installation.class);
        
        Root<Installation> root = cq.from(Installation.class);
        
        TypedQuery<Installation> tq = em.createQuery(cq);

        tq.setFirstResult(off);
        tq.setMaxResults(limit);

// http://java-persistence-performance.blogspot.com/2010/08/batch-fetching-optimizing-object-graph.html
// http://java-persistence-performance.blogspot.com/2011/06/how-to-improve-jpa-performance-by-1825.html
//                
        tq.setHint("eclipselink.read-only", "true");

// http://vard-lokkur.blogspot.com/2011/05/eclipselink-jpa-queries-optimization.html                
//                
        tq.setHint(QueryHints.BATCH, "i.feedhitList");
        tq.setHint(QueryHints.BATCH_TYPE, BatchFetchType.IN);
        
        return tq.getResultList();
    }

    public int getBatchSize() {
        return batchSize;
    }

    public int getOffset() {
        return offset;
    }
}
