package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Archivedfeed;
import com.bc.jpa.EntityController;
import com.bc.jpa.JPQL;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.annotations.BatchFetchType;
import org.eclipse.persistence.config.QueryHints;


/**
 * @(#)FeedArchiver.java   25-Feb-2015 23:50:34
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
public class FeedArchiver {

    public int archiveFeeds(long maxAge, TimeUnit timeUnit, int batchSize) {
        
        Date before = new Date(convertAgeToTime(maxAge, timeUnit));

        return this.archiveFeeds(before, batchSize);
    }
    
    /**
     * Within this method, when multiple Exceptions of the same class occurs 
     * serially, only the first one is logged
     * @param before Archive all feeds which are older than this value
     * @param batchSize The feeds are archived in batches of this value
     * @return The number of feeds that were successfully archived
     */
    public int archiveFeeds(Date before, int batchSize) {
        
        EntityController<Feed, Integer> ec = this.getFeedController();
        
XLogger.getInstance().log(Level.FINE, "Executing query to archive feeds before: {0}", this.getClass(), before);
        
        int offset = 0;
        do{
            
            List feedids = this.getFeedIdsBefore(before, offset, batchSize);

XLogger.getInstance().log(Level.FINE, "Offset: {0}, batchSize: {1}, Ids: {2}", 
        this.getClass(), offset, feedids==null?null:feedids.size(), feedids);
            
            if(feedids == null || feedids.isEmpty()) {
                break;
            }
            
            offset = feedids.size();
            
            EntityManager em = ec.getEntityManager();

            try{
                for(Object feedid:feedids) {
                    String insertSelectQuery = "INSERT INTO `archivedfeed` (archivedfeedid, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid)  SELECT null, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid FROM `feed` WHERE `feed`.`feedid` = '"+feedid+"'";
                    String deleteQuery = "DELETE FROM `feed` WHERE `feed`.`feedid` = '"+feedid+"'";
                    try{
                        int updateCount = this.executeUpdate(em, insertSelectQuery, deleteQuery);
                    }catch(Exception e) {
                        if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass())) {
                            XLogger.getInstance().log(Level.FINER, "Error archiving feed with feedid: "+feedid, this.getClass(), e);
                        }else{
                            XLogger.getInstance().log(Level.FINE, e.toString(), this.getClass());
                        }
                    }
                }
            }finally{
                em.close();
            }
            
        }while(true);
        
        return offset;
    }

    /**
     * <b>NOTE:</b> The EntityManager is not closed by this method
     * @param em The EntityManager to use
     * @param insertSelectQuery
     * @param deleteQuery
     * @return The update count
     * @throws java.lang.Exception
     */
    public int executeUpdate(EntityManager em, String insertSelectQuery, String deleteQuery) throws Exception{
// We have to commit each transaction independently or whole batches may never execute. 
// Also if any single update fails we have to be able to continue processing
        int updateCount = -1;
        try{
            EntityTransaction t = em.getTransaction();
            try{
                t.begin();
                Query q = em.createNativeQuery(insertSelectQuery);
                int insertCount = q.executeUpdate();
                if(insertCount > 0) {
                    q = em.createNativeQuery(deleteQuery);
                    int deleteCount = q.executeUpdate();
                    if(deleteCount == insertCount) {
                        t.commit();
                        updateCount = insertCount;
                    }else{
                        t.rollback();
                        updateCount = 0;
                    }
                }else{
                    updateCount = 0;
                }
            }finally{
                if(t.isActive()) {
                    t.rollback();
                    updateCount = 0;
                }
            }
        }finally{
XLogger.getInstance().log(Level.FINER, "Insert Query: {0}\nDelete Query: {1}\nUpdate count: {2}", 
        this.getClass(), insertSelectQuery, deleteQuery, updateCount);
        }
        
        return updateCount;
    }
    
    private void handleException(Throwable e) {
// org.eclipse.persistence.exceptions.DatabaseException        
//com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException: Duplicate entry '198149' for key 'feedid'    
        boolean log = true;
        if(e instanceof org.eclipse.persistence.exceptions.DatabaseException ||
                ((e = e.getCause()) instanceof org.eclipse.persistence.exceptions.DatabaseException)) {
            
            org.eclipse.persistence.exceptions.DatabaseException dbe = (org.eclipse.persistence.exceptions.DatabaseException)e;
            
            if(dbe.getInternalException() instanceof com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException &&
                    dbe.getInternalException().getMessage().contains("Duplicate")) {
                
                log = false;
            }
        }
        if(log) {
// Lighter logging for this                
            XLogger.getInstance().log(Level.WARNING, e.toString(), this.getClass());
        }
    }

    public int archiveFeeds_0(long maxAge, TimeUnit timeUnit, int limit) {
        
        Date before = new Date(convertAgeToTime(maxAge, timeUnit));

        return archiveFeeds_0(before, limit);
    }
        
    public int archiveFeeds_0(Date before, int batchSize) {
        
        EntityController<Feed, Integer> ec = this.getFeedController();
        
XLogger.getInstance().log(Level.FINE, "Executing query to archive feeds before: {0}", this.getClass(), before);
        
        int offset = 0;
        do{
            
            EntityManager em = ec.getEntityManager();

            List<Feed> feeds = this.getFeedsBefore(em, before, offset, batchSize);

XLogger.getInstance().log(Level.FINE, "Offset: {0}, batchSize: {1}", 
        this.getClass(), offset, feeds==null?null:feeds.size());
            
            if(feeds == null || feeds.isEmpty()) {
                break;
            }
            
            offset = feeds.size();
            
            try{
                
                Archivedfeed archivedfeed = new Archivedfeed();
                
                for(Feed feed:feeds) {
                    
                    boolean updated = this.archiveFeed(em, feed, archivedfeed);
                    
                    em.detach(archivedfeed);
                }
            }finally{
                em.close();
            }
            
        }while(true);
        
        return offset;
    }

    /**
     * <b>NOTE:</b> The EntityManager is not closed by this method
     * @param em The EntityManager to use
     * @param feed The feed whose archived version will be archived
     * @param archivedfeed The archived feed which will be populated by data from the feed
     * @return true if update was successful, otherwise false
     */
    public boolean archiveFeed(EntityManager em, Feed feed, Archivedfeed archivedfeed) {
// We have to commit each transaction independently or whole batches may never execute. 
// Also if any single update fails we have to be able to continue processing
        boolean output = false;

        try{
            this.updateArchivedfeed(archivedfeed, feed);

            EntityTransaction t = em.getTransaction();

            try{

                t.begin();

// http://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge                
                em.persist(archivedfeed); 
//                em.detach(reusedEntity);

                em.remove(feed); 

                t.commit();
                
                output = true;

            }finally {
                if(t.isActive()) {
                    t.rollback();
                    output = false;
                }
            }
        }catch(Exception e) {
// Lighter logging for this                
            XLogger.getInstance().log(Level.WARNING, e.toString(), this.getClass());
        }finally{
XLogger.getInstance().log(Level.FINER, "Updated: {0}, Delete feed: {1}, Insert archvivedfeed: {2}", 
        this.getClass(), output, feed, archivedfeed);
        }
        
        return output;
    }
    
    public List getFeedIdsBefore(Date before, int offset, int limit) {
        EntityController<Feed, Integer> ec = this.getFeedController();
        JPQL jpql = ec.getJpql();
        String old_value = jpql.getComparisonOperator();
        try{
            jpql.setComparisonOperator("<");
            return ec.selectColumn("feedid", "feeddate", before, offset, limit);
        }finally{
            jpql.setComparisonOperator(old_value);
        }
    }
    
    public List<Feed> getFeedsBefore(Date before, int offset, int limit) {
        
        EntityController<Feed, Integer> ec = this.getFeedController();

        EntityManager em = ec.getEntityManager();
        
        try{
            
            return this.getFeedsBefore(em, before, offset, limit);

        }finally{

            em.close();
        }
    }
    
    private List<Feed> getFeedsBefore(EntityManager em, Date before, int offset, int limit) {
        
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Feed> query = cb.createQuery(Feed.class);
        Root<Feed> feed = query.from(Feed.class);
//@column literal not good            
XLogger.getInstance().log(Level.FINER, "Select feeds before: {0}", this.getClass(), before);
        query.where(cb.lessThan(feed.<Date>get("feeddate"), before));

        TypedQuery<Feed> typedQuery = em.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(limit);

// http://java-persistence-performance.blogspot.com/2010/08/batch-fetching-optimizing-object-graph.html
// http://java-persistence-performance.blogspot.com/2011/06/how-to-improve-jpa-performance-by-1825.html
//           
// We delete records which we archive, hence this is not a read-only operation
//            
//            typedQuery.setHint("eclipselink.read-only", "true");

// http://vard-lokkur.blogspot.com/2011/05/eclipselink-jpa-queries-optimization.html                
//                
        typedQuery.setHint(QueryHints.BATCH, "f.commentList");
        typedQuery.setHint(QueryHints.BATCH, "f.feedhitList");
        typedQuery.setHint(QueryHints.BATCH_TYPE, BatchFetchType.IN);

        List<Feed> feeds = typedQuery.getResultList();

XLogger.getInstance().log(Level.FINER, "Expected: {0}, retreived {1} feeds from database", 
this.getClass(), limit, feeds==null?null:feeds.size());

        return feeds;
    }
   
    public static final long convertAgeToTime(long maxAge, TimeUnit timeUnit) {
        
        long maxAgeInMillis = timeUnit.toMillis(maxAge);
    
        return System.currentTimeMillis() - maxAgeInMillis;
    }
    
    public void updateArchivedfeed(Archivedfeed feedarchive, Feed feed) {
        feedarchive.setAuthor(feed.getAuthor());
        feedarchive.setCategories(feed.getCategories());
        feedarchive.setContent(feed.getContent());
        feedarchive.setDatecreated(feed.getDatecreated());
        feedarchive.setDescription(feed.getDescription());
        feedarchive.setExtradetails(feed.getExtradetails());
        feedarchive.setArchivedfeedid(null); // In case this object is being reused
        feedarchive.setFeeddate(feed.getFeeddate());
        feedarchive.setFeedid(feed.getFeedid());
        feedarchive.setImageurl(feed.getImageurl());
        feedarchive.setKeywords(feed.getKeywords());
        feedarchive.setRawid(feed.getRawid());
        feedarchive.setSiteid(feed.getSiteid());
        feedarchive.setTimemodified(feed.getTimemodified());
        feedarchive.setTitle(feed.getTitle());
        feedarchive.setUrl(feed.getUrl());
    }

    private EntityController<Feed, Integer> _fc;
    private EntityController<Feed, Integer> getFeedController() {
        if(_fc == null) {
            _fc = IdiscApp.getInstance().getControllerFactory().getEntityController(Feed.class, Integer.class);
        }
        return _fc;
    }
    
    private EntityController<Archivedfeed, Integer> _afc;
    private EntityController<Archivedfeed, Integer> getArchivedfeedController() {
        if(_afc == null) {
            _afc = IdiscApp.getInstance().getControllerFactory().getEntityController(Archivedfeed.class, Integer.class);
        }
        return _afc;
    }
}
/***
 * 
    public int archiveFeeds_1(long maxAge, TimeUnit timeUnit) {
// The null below is a place hold for `my_table_archive_id`
//        
// INSERT INTO `my_table_archive` (my_table_archive_id, col_2, col_3, ..., col_n)
// SELECT null, my_table_id, col_2, col_3, ..., col_n
// FROM `my_table`
// WHERE entry_date < '2011-01-01 00:00:00';
//        
        ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
        
        PersistenceMetaData metaData = cf.getMetaData();

        Class entityClass;
        JPQL jpql;
        String tableAlias;
        List columnNames;
        String queryString;
        
        entityClass = Archivedfeed.class;
        jpql = cf.getJpql(entityClass);
//        tableAlias = jpql.getTableAlias();
        columnNames = new ArrayList(Arrays.asList(metaData.getColumnNames(entityClass)));
//        columnNames.remove("archivedfeedid"); // We remove the id column
        
        StringBuilder appendTo = new StringBuilder();
        appendTo.append("INSERT INTO ").append(entityClass.getSimpleName());
// Native query doesn't need this        
//        appendTo.append(' ').append(tableAlias);

        if(!columnNames.isEmpty()) {
            
            appendTo.append(" (");

            jpql.appendColumns(columnNames, null, null, appendTo);
            appendTo.append(") ");
        }
        
        appendTo.append(" SELECT ");
        
        entityClass = Feed.class;
        jpql = cf.getJpql(entityClass);
        tableAlias = jpql.getTableAlias();
        Object firstColumn = columnNames.get(0);
        columnNames.set(0, null);  // This placehold for archivedfeedid
        
        if(!columnNames.isEmpty()) {
            jpql.appendColumns(columnNames, null, null, appendTo);
        }else{
            appendTo.append('*');
        }
        
        appendTo.append(" FROM ").append(entityClass.getSimpleName());
// Native query doesn't need this        
        appendTo.append(' ').append(tableAlias);
        
        Date  date = new Date(this.getTimeMillis(maxAge, timeUnit));
        
        Map where = Collections.singletonMap("feeddate", date);
        
        String comparisonOperator = "<";
        
        jpql.setComparisonOperator(comparisonOperator); // '<' didn't work
        jpql.appendWhereClause("WHERE", where.keySet(), tableAlias, null, appendTo);
        
        queryString = appendTo.toString();
        
        appendTo.setLength(0);

// REPLACE first column with original value        
        columnNames.set(0, firstColumn);
        
        EntityManager em = jpql.getEntityManagerFactory().createEntityManager();

        try{
            
            com.bc.sql.MySQLDateTimePatterns dtp = new com.bc.sql.MySQLDateTimePatterns();
            Object sqlDate = SQLUtils.toSQLType(dtp, Types.DATE, date);
            
            queryString = "INSERT INTO `archivedfeed` (archivedfeedid, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid)  SELECT null, feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid FROM `feed` WHERE `feed`.`feeddate` "+comparisonOperator+" '"+sqlDate+"'";
            Query insertSelectQuery = em.createNativeQuery(queryString);

XLogger.getInstance().log(Level.INFO, "Query string: {0}\nTyped query: {1}", this.getClass(), queryString, insertSelectQuery);

            jpql.updateQuery(em, insertSelectQuery, where, false);
            
            // Delete inserted
            //
            appendTo.append("DELETE ");
            
            if(!columnNames.isEmpty()) {
                jpql.appendColumns(columnNames, null, null, appendTo);
            }else{
                appendTo.append('*');
            }
            
            appendTo.append(" FROM ").append(entityClass.getSimpleName());
// Native query doesn't need this        
            appendTo.append(' ').append(tableAlias);

            jpql.setComparisonOperator(comparisonOperator); // Didn't work
            jpql.appendWhereClause("WHERE", where.keySet(), tableAlias, null, appendTo);
// Native query doesn't need tableAlias
//            jpql.appendWhereClause("WHERE", where.keySet(), tableAlias, null, appendTo);
        
            queryString = appendTo.toString();
            appendTo.setLength(0);
            
//            queryString = "DELETE feedid, rawid, url, imageurl, author, title, keywords, categories, description, content, feeddate, datecreated, timemodified, extradetails, siteid FROM `feed` f WHERE f.feeddate "+comparisonOperator+" '"+sqlDate+"'";
            queryString = "DELETE FROM `feed` WHERE `feed`.`feeddate` "+comparisonOperator+" '"+sqlDate+"'";
            Query deleteQuery = em.createNativeQuery(queryString);

XLogger.getInstance().log(Level.INFO, "Query string: {0}\nTyped query: {1}", this.getClass(), queryString, deleteQuery);

            jpql.updateQuery(em, deleteQuery, where, false);
            
            em.getTransaction().begin();
            
            boolean success = false;

            int insertUpdateCount = insertSelectQuery.executeUpdate(); 
            int deleteUpdateCount = -1;
            if(insertUpdateCount > 0) {
                
                deleteUpdateCount = deleteQuery.executeUpdate();
                
                success = insertUpdateCount == deleteUpdateCount;
            }
            
            if(success) {
                em.getTransaction().commit();
            }else{
                em.getTransaction().rollback();
            }
            
XLogger.getInstance().log(Level.WARNING, "Inserted: {0}, Deleted: {1}", this.getClass(), insertUpdateCount, deleteUpdateCount);

            return success ? deleteUpdateCount : 0;
            
        }finally{
            em.close();
        }
    }

 * 
 */