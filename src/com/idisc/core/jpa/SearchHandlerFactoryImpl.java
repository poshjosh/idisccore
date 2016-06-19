package com.idisc.core.jpa;

import com.bc.jpa.search.AbstractSearchResults;
import com.bc.jpa.search.SearchResults;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * @author Josh
 */
public class SearchHandlerFactoryImpl implements SearchHandlerFactory {
    
    // we use the same cacheSize for both type cache and session cache
    private final int cacheSize;
    
    private final Lock lock;
    
    private final Map<Class, Map<String, SearchResults>> typeCache;
    
    public SearchHandlerFactoryImpl() {
        cacheSize = 8;
        lock = new ReentrantLock();
        typeCache = Collections.synchronizedMap(new HashMapNoNulls(cacheSize, 0.75f));
    }
    
    @Override
    public Set<Class> getEntityTypes() {
        return new HashSet(typeCache.keySet());
    }
    
    @Override
    public int removeAll(boolean close) {
        int removed = 0;
        try{
            lock.lock();
            Set<Class> entityTypes = typeCache.keySet();
            for(Class entityType:entityTypes) {
                Map<String, SearchResults> sessionCache = typeCache.get(entityType);
                if(sessionCache != null) {
                    Set<String> sessionIds = sessionCache.keySet();
                    for(String sessionId:sessionIds) {
                        Object o = this.remove(sessionId, entityType, close);
                        if(o != null) {
                            ++removed;
                        }
                    }
                }
            }
        }finally{
            lock.unlock();
        }
        return removed;
    }
    
    @Override
    public int removeAll(String sessionId, boolean close) {
        int removed = 0;
        try{
            lock.lock();
            Set<Class> entityTypes = typeCache.keySet();
            for(Class entityType:entityTypes) {
                Object o = this.remove(sessionId, entityType, close);
                if(o != null) {
                    ++removed;
                }
            }
        }finally{
            lock.unlock();
        }
        return removed;
    }
    
    @Override
    public <E> SearchResults<E> remove(String sessionId, Class<E> entityType, boolean close) {
        try{
            lock.lock();
            Map<String, SearchResults> sessionCache = typeCache.get(entityType);
            SearchResults<E> output = sessionCache == null ? null : sessionCache.remove(sessionId);
            if(output != null && close) {
                output.close();
            }
            return output;
        }finally{
            lock.unlock();
        }
    }
    
    @Override
    public <E> SearchResults<E> get(String sessionId, Class<E> entityType, boolean createNew) {
        
        return this.get(sessionId, entityType, Collections.EMPTY_MAP, createNew);
    }

    @Override
    public <E> SearchResults<E> get(String sessionId, Class<E> entityType, 
            Map<String, Object> parameters, boolean createNew) {

        SearchResults output = null;

        try{
            
            lock.lock();

            Map<String, SearchResults> sessionCache = typeCache.get(entityType);

            if(sessionCache != null) {

                output = sessionCache.get(sessionId);
            }

            if(createNew) {

                if(output != null) {

                    output.close();
                }

                output = this.create(entityType, parameters);

                if(sessionCache == null) {

                    sessionCache = new HashMapNoNulls(cacheSize, 0.75f);

                    typeCache.put(entityType, sessionCache);
                }

                sessionCache.put(sessionId, output);
            }
        }finally{
        
            lock.unlock();
        }
        
        return output;
    }
    
    protected <E> SearchResults<E> create(Class<E> entityType, Map<String, Object> parameters) {
        
        Search search;
        
        if(entityType == Comment.class) {
            
            search = new CommentSearch();
            
        }else if(entityType == Feed.class){
            
            search = new FeedSearch();
            
        }else{
            
            throw new UnsupportedOperationException("Cannot create "+SearchResults.class.getName()+" for: type: "+entityType.getName());
        }
        
        String query = parameters == null ? null : (String)parameters.get("query");
        Integer limit = parameters == null || parameters.get("limit") == null ? 20 : (Integer)parameters.get("limit");
        Date after = parameters == null ? null : (Date)parameters.get("after");
        
        return new SearchResultsImpl(search, entityType, query, after, limit);
    } 
    
    class SearchResultsImpl<T> extends AbstractSearchResults<T> implements SearchResults<T> {

        private transient EntityManager entityManager;

        private final Date after;
        private final Search search;
        private final Class<T> entityType;

        SearchResultsImpl(Search<T> search, Class<T> entityType) {
            this(search, entityType, null, null, 20);
        }

        SearchResultsImpl(Search<T> search, Class<T> entityType, String query, Date after, int limit) {
            super(query, limit, true);
            this.search = search;
            this.after = after;
            this.entityType = entityType;
        }

        @Override
        public TypedQuery<T> createQuery() {
          this.closeEntityManager();
          this.entityManager = IdiscApp.getInstance().getControllerFactory().getEntityManager(entityType);
          return search.createTypedQuery(this.entityManager, this.getSearchTerm(), after);
        }

        @Override
        public void close() {
XLogger.getInstance().log(Level.INFO, "= = = = = = = = = = = = = = = = = = = = = = = == = = Closing search results", this.getClass());
          super.close();
          this.closeEntityManager();
        }

        private void closeEntityManager() {
          if(this.entityManager != null && this.entityManager.isOpen()) {
              this.entityManager.close();
          }
        }
    }

    
    private class HashMapNoNulls<K, V> extends HashMap<K, V> {

        public HashMapNoNulls(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        public HashMapNoNulls(int initialCapacity) {
            super(initialCapacity);
        }

        public HashMapNoNulls() { }

        @Override
        public V put(K key, V value) {
            if(key == null || value == null) {
                throw new NullPointerException();
            }
            return super.put(key, value); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
