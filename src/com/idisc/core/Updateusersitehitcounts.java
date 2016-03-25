package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.JPQL;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
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
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.annotations.BatchFetchType;













public class Updateusersitehitcounts
  implements Runnable, Serializable
{
  private int offset;
  private final int batchSize;
  
  public Updateusersitehitcounts()
  {
    this(100);
  }
  
  public Updateusersitehitcounts(int batchSize) {
    this.batchSize = batchSize;
  }
  
  public void run()
  {
    XLogger xlogger = XLogger.getInstance();
    ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
    
    JPQL<Site> jpql = factory.getJpql(Site.class);
    
    List<Site> sites = jpql.getResultList((Map)null, null, (Map)null, -1, -1, false);
    
    xlogger.log(Level.FINER, "Found {0} sites", getClass(), sites == null ? null : Integer.valueOf(sites.size()));
    if ((sites == null) || (sites.isEmpty()))
      return;
    int found_count;
    long tb4;
    long mb4;
    int[] hitcounts;
    do {
      List<Installation> inst_list = getInstallations(factory, this.offset, this.batchSize);
      
      found_count = inst_list == null ? 0 : inst_list.size();
      
      xlogger.log(Level.FINER, "Found {0} installations", getClass(), Integer.valueOf(found_count));
      
      this.offset += found_count;
      


      if (found_count < 1) {
        break;
      }
      

      tb4 = System.currentTimeMillis();
      mb4 = Runtime.getRuntime().freeMemory();
      
      hitcounts = new int[sites.size()];
      
      for (Installation inst : inst_list)
      {
        xlogger.log(Level.INFO, "Computing site counts for installation: {0}", getClass(), inst);
        
        List<Feedhit> feedhit_list = inst.getFeedhitList();
        
        Arrays.fill(hitcounts, 0);
        
        for (Feedhit feedhit : feedhit_list)
        {
          Site site = feedhit.getFeedid().getSiteid();
          
          int index = sites.indexOf(site);
          
          hitcounts[index] += 1;
        }
        
        xlogger.log(Level.INFO, "Consumed memory: {0}, time: {1}", getClass(), Long.valueOf(mb4 - Runtime.getRuntime().freeMemory()), Long.valueOf(System.currentTimeMillis() - tb4));
        

        Date reusedDate = new Date();
        Usersitehitcount reusedEntity = new Usersitehitcount();
        
        EntityManager em = factory.getEntityManager(Usersitehitcount.class);
        
        try
        {
          em.getTransaction().begin();
          
          for (Site site : sites)
          {
            int index = sites.indexOf(site);
            int hits = hitcounts[index];
            
            if (hits != 0)
            {


              reusedEntity.setDatecreated(reusedDate);
              reusedEntity.setHitcount(hits);
              reusedEntity.setInstallation(inst);
              reusedEntity.setSite(site);
              
              em.merge(reusedEntity);
              

              xlogger.log(Level.FINER, "Installation: {0}, site: {1}, count: {2}", getClass(), inst, site, Integer.valueOf(hits));
            }
          }
          em.getTransaction().commit();
          
          xlogger.log(Level.INFO, "Consumed memory: {0}, time: {1}", getClass(), Long.valueOf(mb4 - Runtime.getRuntime().freeMemory()), Long.valueOf(System.currentTimeMillis() - tb4));
        }
        finally
        {
          em.close();
        }
      }
    } while (found_count >= this.batchSize);
  }
  




  public List<Installation> getInstallations(ControllerFactory factory, int off, int limit)
  {
    EntityManager em = factory.getEntityManager(Installation.class);
    
    CriteriaBuilder cb = em.getCriteriaBuilder();
    
    CriteriaQuery<Installation> cq = cb.createQuery(Installation.class);
    
    Root<Installation> root = cq.from(Installation.class);
    
    TypedQuery<Installation> tq = em.createQuery(cq);
    
    tq.setFirstResult(off);
    tq.setMaxResults(limit);
    



    tq.setHint("eclipselink.read-only", "true");
    


    tq.setHint("eclipselink.batch", "i.feedhitList");
    tq.setHint("eclipselink.batch.type", BatchFetchType.IN);
    
    return tq.getResultList();
  }
  
  public int getBatchSize() {
    return this.batchSize;
  }
  
  public int getOffset() {
    return this.offset;
  }
}
