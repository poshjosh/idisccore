/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core;

import com.bc.jpa.context.JpaContext;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Archivedfeed;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import com.bc.jpa.dao.Select;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 6, 2016 8:44:02 PM
 */
public class FeedArchiver {

  private final JpaContext jpaContext;
  
  public FeedArchiver(JpaContext jpaContext) { 
    this.jpaContext = jpaContext;
  } 
   
  public int archiveFeedsBefore(long maxAge, TimeUnit timeUnit, int batchSize) {
      
    Date before = new Date(convertAgeToTime(maxAge, timeUnit));
   
    return archiveFeedsBefore(before, batchSize);
  }

  public int archiveFeedsBefore(Date date, int batchSize) {
      
    final XLogger logger = XLogger.getInstance();
    final Class cls = this.getClass();
    
    logger.log(Level.FINE, "Archiving feeds before: {0}", cls, date);
    
    final Date THRESH_HOLD = new Date(TimeUnit.HOURS.toMillis(24));
    
    int offset = 0;
    
    for (;;) {
        
      try(Select<Integer> dao = jpaContext.getDaoForSelect(Feed.class, Integer.class)){
        
        List<Integer> feedids = dao.from(Feed.class)
        .select(Feed_.feedid.getName())
        .where(Feed_.feeddate.getName(), Select.LT, date)
        .createQuery().setFirstResult(offset).setMaxResults(batchSize)
        .getResultList();
          
        final Object sizeOfBatch = feedids == null ? null : feedids.size();
      
        logger.log(Level.FINE, "Offset: {0}, batchSize: {1}, batch: {2}", 
            cls, offset, batchSize, sizeOfBatch);
//        System.out.println("- - - - - - - Offset "+offset+", batchSize: "+batchSize+", size of batch: "+sizeOfBatch);
      
        if ((feedids == null) || (feedids.isEmpty())) {
          break;
        }
      
        EntityManager em = dao.getEntityManager();
          
        EntityTransaction t = em.getTransaction();
          
        try{  
          
          t.begin();
          
          for(Integer feedid : feedids) {
              
            Feed feed = em.find(Feed.class, feedid);
            
            if(feed.getFeeddate().before(THRESH_HOLD)) {
               
              logger.log(Level.FINE, "Archive of feed not executed. Reason: Feeddate not set for feed with ID: {0}, title: {1}", 
                      cls, feed.getFeedid(), feed.getTitle());
              
              continue;
            }
            
            boolean exists;
            try{
              final String col = Feed_.feedid.getName();
              TypedQuery<Number> query = em.createQuery("SELECT a."+col+" FROM "+Archivedfeed.class.getSimpleName()+" a WHERE a."+col+" = :"+col, Number.class);  
              query.setParameter(col, feed.getFeedid());
              exists = query.getSingleResult() != null;
            }catch(NoResultException ignored) {
              exists = false;
            }
            
            if(!exists) {
                
              Archivedfeed archivedfeed = new Archivedfeed();
        
              this.updateArchivedfeed(archivedfeed, feed);
        
              em.persist(archivedfeed); 
              logger.log(Level.FINER, 
                  "Archiving feed with id: {0}, feedddate: {0}, title: {0}", 
                  cls, feed.getFeedid(), feed.getFeeddate(), feed.getTitle());
            }
            
            em.remove(feed);
            
            logger.log(Level.FINER, 
                    "Deleting feed with id: {0}, feedddate: {0}, title: {0}", 
                    cls, feed.getFeedid(), feed.getFeeddate(), feed.getTitle());
          }
          
          t.commit();
          
        }finally{
          if(t.isActive()) {
            t.rollback();
          }  
        }
        
        offset += feedids.size();
      }
    }

    return offset;
  }

  public void updateArchivedfeed(Archivedfeed archivedfeed, Feed feed) {
    archivedfeed.setArchivedfeedid(null);
    archivedfeed.setAuthor(feed.getAuthor());
    archivedfeed.setCategories(feed.getCategories());
    archivedfeed.setContent(feed.getContent());
    archivedfeed.setDatecreated(feed.getDatecreated());
    archivedfeed.setDescription(feed.getDescription());
    archivedfeed.setExtradetails(feed.getExtradetails());
    archivedfeed.setFeeddate(feed.getFeeddate());
    archivedfeed.setFeedid(feed.getFeedid());
    archivedfeed.setImageurl(feed.getImageurl());
    archivedfeed.setKeywords(feed.getKeywords());
    archivedfeed.setRawid(feed.getRawid());
    archivedfeed.setSiteid(feed.getSiteid());
    archivedfeed.setTimemodified(feed.getTimemodified());
    archivedfeed.setTitle(feed.getTitle());
    archivedfeed.setUrl(feed.getUrl());
  }

  private long convertAgeToTime(long maxAge, TimeUnit timeUnit) {
      
    long maxAgeInMillis = timeUnit.toMillis(maxAge);
    
    return System.currentTimeMillis() - maxAgeInMillis;
  }
}
