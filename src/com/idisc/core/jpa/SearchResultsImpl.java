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
public class SearchResultsImpl<T> extends AbstractSearchResults<T> implements SearchResults<T> {
    
    private transient EntityManager entityManager;
    
    private final Date after;
    private final Search search;
    private final Class<T> entityType;
    

    public SearchResultsImpl(Search<T> search, Class<T> entityType) {
        this(search, entityType, null, null, 20);
    }
    
    public SearchResultsImpl(Search<T> search, Class<T> entityType, String toFind, int batchSize) {
        this(search, entityType, toFind, null, batchSize);
    }

    public SearchResultsImpl(Search<T> search, Class<T> entityType, String toFind, Date after, int batchSize) {
        super(toFind, batchSize, true);
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
      super.close();
      this.closeEntityManager();
    }
    private void closeEntityManager() {
      if(this.entityManager != null && this.entityManager.isOpen()) {
          this.entityManager.close();
      }
    }
}

