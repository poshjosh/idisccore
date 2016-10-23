package com.idisc.core;

import com.bc.jpa.JpaContext;
import com.bc.util.XLogger;
import com.idisc.pu.FeedService;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class FeedResultUpdater {
    
  private final Class cls = FeedResultUpdater.class;
  private final XLogger logger = XLogger.getInstance();
    
  public Map<String, Collection<Feed>> process(Map<String, Collection<Feed>> allResults) {
      
    final Set<String> names = allResults.keySet();
    
    logger.log(Level.FINER, "Saving feeds of types: {0}", cls, names);
    
    Map<String, Collection<Feed>> failedToCreate = null;
    
    for (String name : names)  {
        
      Collection<Feed> taskResults = allResults.get(name);
      
      if (taskResults != null && !taskResults.isEmpty()) {

        Collection<Feed> failed = process(name, taskResults);
        
        if(!failed.isEmpty()) {
            
          if(failedToCreate  == null) {
            failedToCreate = new HashMap<>();
          }
        
          failedToCreate.put(name, failed);
        }
      }
    }
    
    return failedToCreate == null ? Collections.EMPTY_MAP : failedToCreate;
  }
  
  /**
   * @param name
   * @param feedsToCreate
   * @return The collection of feeds (a subset of the input collection) which were not created.
   */
  public Collection<Feed> process(String name, Collection<Feed> feedsToCreate) {
    
    if(logger.isLoggable(Level.FINE, cls)) {
      logger.log(Level.FINE, "Task: {0}, has {1} results.", cls, 
      name, feedsToCreate == null ? null : feedsToCreate.size());
    }  
    
    Collection<Feed> failedToCreate;    
    
    if (feedsToCreate == null || feedsToCreate.isEmpty()) {
        
      failedToCreate = Collections.EMPTY_LIST;
      
    }else{
        
      JpaContext jpaContext = IdiscApp.getInstance().getJpaContext();

      // Use a copy to prevent concurrent modification exception
      //
      Collection<Feed> toCreate = new ArrayList(feedsToCreate); 

      FeedService feedService = new FeedService(jpaContext);

      try {

        failedToCreate = feedService.createIfNoneExistsWithMatchingData(toCreate);

        logger.log(Level.FINE, "Created {0} records for task: {1}.", 
        cls, toCreate.size() - failedToCreate.size(), name);

      }catch (Exception e) {

        failedToCreate = Collections.EMPTY_LIST;

        logger.log(Level.WARNING, "Unexpected error updating feeds of type: " + name, cls, e);
      }
    }
    
    return failedToCreate;
  }
}
