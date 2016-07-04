package com.idisc.core;

import com.bc.jpa.query.JPQL;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import com.bc.jpa.JpaContext;

/**
 * @author Josh
 */
public class FeedSelector implements Serializable {
    
    public FeedSelector() { }
    
    public List<Feed> sort(List<Feed> feedList, Comparator<Feed> comparator, int maxOutputSize) {
        
        Collections.sort(feedList, comparator);

        final int feedListSize = feedList.size();

        final int finalMetricsSize = feedListSize < maxOutputSize ? feedListSize : maxOutputSize;

        List<Feed> outputList = finalMetricsSize >= feedListSize ? feedList : feedList.subList(0, finalMetricsSize);

        return outputList;
    }

    public List<Feed> getList(int maxAgeDays, int batchSize) {
        
        return this.getList(toAge(maxAgeDays), batchSize);
    }
    
    public List<Feed> getList(Date maxAge, int batchSize) {
        
        return this.getList(maxAge, -1, batchSize);
    }
        
    
    public List<Feed> getList(int maxAgeDays, int maxSpread, int batchSize) {
        
        return this.getList(toAge(maxAgeDays), maxSpread, batchSize);
    }
    
    public List<Feed> getList(Date maxAge, int maxSpread, int batchSize) {
        
        JpaContext factory = IdiscApp.getInstance().getJpaContext();

        JPQL<Feed> jpql = factory.getJpql(Feed.class);

        int offset = 0;

        List<Feed> feedList = maxSpread < 1 ? new LinkedList() : new ArrayList<>(maxSpread);

        do{

            List<Feed> batch;

            if(maxAge == null) {
                batch = jpql.getResultList((Map)null, null, null, offset, batchSize, true);
            }else{
                jpql.setComparisonOperator(">");
                Map parameters = Collections.singletonMap("feeddate", maxAge);
                batch = jpql.getResultList(parameters, "AND", null, offset, batchSize, true);
            }    

            final int currentBatchSize = this.sizeOf(batch);

XLogger.getInstance().log(Level.FINE, "Batch size: {0}, offset: {1}, results: {2}", 
        this.getClass(), batchSize, offset, currentBatchSize);   

            if(currentBatchSize < 1) {
                break;
            }

            offset += currentBatchSize;

            feedList.addAll(batch);

        }while(maxSpread < 1 || offset < maxSpread);
        
        return feedList;
    }    
    
    private Date toAge(int maxAgeDays) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -maxAgeDays);
        final Date maxAge = cal.getTime();
        return maxAge;
    }
    
    private int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }
}
