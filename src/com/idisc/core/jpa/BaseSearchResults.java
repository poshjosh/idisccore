package com.idisc.core.jpa;

import com.bc.jpa.search.AbstractSearchResults;
import com.bc.jpa.search.SearchResults;
import com.idisc.core.IdiscApp;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * @author poshjosh
 */
public class BaseSearchResults<T> extends AbstractSearchResults<T> implements SearchResults<T> {
    
    private EntityManager em;
    private String toFind;
    private Date after;
    private int batchSize;
    private Search search;

    public BaseSearchResults(Search<T> search, Class<T> entityType) {
        this(search, entityType, null, null, 20);
    }
    
    public BaseSearchResults(Search<T> search, Class<T> entityType, String toFind, int batchSize) {
        this(search, entityType, toFind, null, batchSize);
    }

    public BaseSearchResults(Search<T> search, Class<T> entityType, String toFind, Date after, int batchSize) {
        this.search = search;
        this.em = IdiscApp.getInstance().getControllerFactory().getEntityManager(entityType);
        this.toFind = toFind;
        this.after = after;
        this.batchSize = batchSize;
    }

    @Override
    public int getBatchSize() {
        return batchSize;
    }
    
    @Override
    public TypedQuery<T> createQuery() {
      return search.createTypedQuery(em, toFind, after);
    }
    
    @Override
    public String getSearchTerm() {
      return toFind;
    }
    @Override
    public void close() {
      super.close();
      if(em != null) {
        em.close();
      }
      this.toFind = null;
      this.after = null;
    }
}

