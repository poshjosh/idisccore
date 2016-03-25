package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.bc.jpa.EntityController;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;


/**
 * @(#)FeedResultUpdater.java   28-Nov-2014 16:23:14
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
public class FeedResultUpdater {

    public int process(Map<String, Collection<Feed>> allResults) {

XLogger.getInstance().log(Level.FINER, "Saving feeds.", this.getClass());
        
        EntityController<Feed, Integer> ec = this.getFeedController();

        int created = 0;
        
        for(String taskName:allResults.keySet()) {
            
            Collection<Feed> taskResults = allResults.get(taskName);
            
            if(taskResults == null || taskResults.isEmpty()) {
                continue;
            }
            
            created += this.process(taskName, taskResults);
        }
        
XLogger.getInstance().log(Level.FINE, "Saved {0} feeds", this.getClass(), created);                        
        return created;
    }

    public int process(String taskName, Collection<Feed> taskResults) {
        
XLogger.getInstance().log(Level.FINE, "Task: {0}, has {1} results.", 
        this.getClass(), taskName, taskResults==null?null:taskResults.size());

        if(taskResults == null || taskResults.isEmpty()) {
            return 0;
        }

        int created = 0;
        
        EntityManager em = this.getFeedController().getEntityManager();
        
        Iterator<Feed> iter = taskResults.iterator();

        Feed reusedFeedParams = new Feed();

        Date dateCreated = new Date();
        
        try{
            
            int count = 0;
            
            while(iter.hasNext()) {

//@todo @bug    
                Feed toCreate = null;
                try{
                    toCreate = iter.next();
                }catch(ConcurrentModificationException e) { 
                    if(++count > 100) {
XLogger.getInstance().log(Level.WARNING, "WARNING::BUG", this.getClass(), e);
                        break;
                    }
                }
                
                if(toCreate == null) {
                    continue;
                }
                
                boolean updated = this.update(em, reusedFeedParams, toCreate, dateCreated);
                
                if(updated) {
                    ++created;
                }
            }

XLogger.getInstance().log(Level.FINE, "Created {0} records for task: {1}.", 
        this.getClass(), created, taskName);
            
        }catch(Exception e) {
            
            XLogger.getInstance().log(Level.WARNING, "Unexpected error updating feeds of type: "+taskName, this.getClass(), e);
            
        }finally{
            
            em.close();
        }

        return created;
    }
    
    private boolean update(EntityManager em, Feed reusedFeedParams, Feed toCreate, Date dateCreated){
// We have to commit each feed separately 
        boolean output = false;

        EntityTransaction t = null;

        try{

            if(!Util.isInDatabase(this.getFeedController(), reusedFeedParams, toCreate)) {

                t = em.getTransaction();

                t.begin();

                dateCreated.setTime(System.currentTimeMillis());
                toCreate.setDatecreated(dateCreated);

                em.persist(toCreate);

                t.commit();

                output = true;

//XLogger.getInstance().log(Level.FINER, "Created feed: {0}", this.getClass(), toString(feed));
            }else{
//XLogger.getInstance().log(Level.FINER, "Feed already exists: {0}", this.getClass(), toString(feed));                        
            }
        }catch(Exception e) {
            XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
        }finally{
            if(t != null && t.isActive()) {
                t.rollback();
                output = false;
            }
        }
        return output;
    }
    
    private String toString(Feed feed) {
        StringBuilder builder = new StringBuilder();
        builder.append("Id: ").append(feed.getFeedid());
        builder.append(", site: ").append(feed.getSiteid().getSite());
        builder.append(", author: ").append(feed.getAuthor());
        builder.append(", date: ").append(feed.getFeeddate());
        builder.append(", link: ").append(feed.getUrl());
        builder.append(", imageUrl: ").append(feed.getImageurl());
        return builder.toString();
    }

    private EntityController<Feed, Integer> _fc;
    private EntityController<Feed, Integer> getFeedController() {
        if(_fc == null) {
            _fc = IdiscApp.getInstance().getControllerFactory().getEntityController(Feed.class, Integer.class);
        }
        return _fc;
    }
}
