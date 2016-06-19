package com.idisc.core.jpa;

import com.bc.jpa.search.SearchResults;
import java.util.Map;
import java.util.Set;

/**
 * @author Josh
 */
public interface SearchHandlerFactory {

    Set<Class> getEntityTypes();
    
    <E> SearchResults<E> get(String sessionId, Class<E> entityType, boolean createNew);
    
    <E> SearchResults<E> get(String sessionId, Class<E> entityType, Map<String, Object> parameters, boolean createNew);

    int removeAll(boolean close);
    
    int removeAll(String sessionId, boolean close);
    
    <E> SearchResults<E> remove(String sessionId, Class<E> entityType, boolean close);
}
