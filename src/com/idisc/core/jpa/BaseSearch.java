package com.idisc.core.jpa;

import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author poshjosh
 */
public class BaseSearch<T> implements Search<T> {
    
  private final Class<T> entityClass;
  
  private final String dateColumn;
  
  private final String [] searchColumns;
  
  public BaseSearch(Class<T> entityClass) {
    this(entityClass, null, null);
  }

  public BaseSearch(Class<T> entityClass, String [] searchColumns, String dateColumn) {
    this.entityClass = entityClass;
    this.searchColumns = searchColumns;
    this.dateColumn = dateColumn;
  }
    
  protected void addOrderBy(CriteriaBuilder cb, CriteriaQuery<T> query, Root<T> root) { }

    @Override
  public List<T> select(int offset, int limit)
  {
    return this.select(null, null, offset, limit);
  }
  
    @Override
  public List<T> select(String toFind, Date after, int offset, int limit)
  {
    long mb4 = Runtime.getRuntime().freeMemory();
    long tb4 = System.currentTimeMillis();

    EntityManager em = IdiscApp.getInstance().getControllerFactory().getEntityManager(this.entityClass);
    
    List<T> selected;
    try
    {
     
      TypedQuery<T> typedQuery = this.createTypedQuery(em, toFind, after, offset, limit);
      selected = typedQuery.getResultList();
      
XLogger.getInstance().log(Level.FINE, "Expected: {0}, retreived {1} items from database. Consumed memory: {2}, time: {3}", 
        getClass(), limit, selected == null ? null : Integer.valueOf(selected.size()), mb4 - Runtime.getRuntime().freeMemory(), System.currentTimeMillis() - tb4);
    }
    finally
    {
      em.close();
    }
XLogger.getInstance().log(Level.FINE, "Selected {0} items for query: {1}, offset: {2}, limit: {3}", 
        this.getClass(), (selected==null?null:selected.size()), toFind, offset, limit);
    return selected;
  }

    @Override
  public TypedQuery<T> createTypedQuery(
          EntityManager em, int offset, int limit)
  {
    return this.createTypedQuery(em, null, null, offset, limit);
  }
  
    @Override
  public TypedQuery<T> createTypedQuery(
          EntityManager em, String toFind, Date after, int offset, int limit)
  {

      TypedQuery<T> typedQuery = this.createTypedQuery(em, toFind, after);
      
      typedQuery.setFirstResult(offset);
      typedQuery.setMaxResults(limit);

      return typedQuery;
  }

    @Override
  public TypedQuery<T> createTypedQuery(EntityManager em) {

    return this.createTypedQuery(em, null, null);
  }
  
    @Override
  public TypedQuery<T> createTypedQuery(EntityManager em, String toFind, Date after)
  {

      CriteriaBuilder cb = em.getCriteriaBuilder();
      CriteriaQuery<T> query = cb.createQuery(this.entityClass);
      Root<T> root = query.from(this.entityClass);

      Predicate[] predicates = buildPredicates(cb, root, after, searchColumns, toFind);

      if ((predicates != null) && (predicates.length != 0))
      {
        if (predicates.length == 1) {
          query.where(predicates[0]);
        } else {
          query.where(cb.or(predicates));
        }
      }

      this.addOrderBy(cb, query, root);

      TypedQuery<T> typedQuery = em.createQuery(query);

      return typedQuery;
  }
  
  private Predicate[] buildPredicates(CriteriaBuilder cb, Root<T> root, 
          Date after, String[] columnNames, String toFind)
  {
    List<Predicate> predicates = new ArrayList();
    
    if (toFind == null)
    {
      XLogger.getInstance().log(Level.FINER, "Select feeds after: {0}", getClass(), after);
      if (after != null)
      {
        predicates.add(cb.greaterThan(root.<Date>get(dateColumn), after));
      }
    }
    else {
      toFind = "%" + toFind + "%";
      
      for (String key : columnNames)
      {
        Predicate p;
        if (after == null)
        {
          p = cb.like(root.<String>get(key), toFind);
        }
        else
        {
          p = cb.and(cb.greaterThan(root.<Date>get(dateColumn), after), cb.like(root.<String>get(key), toFind));
        }

        predicates.add(p);
      }
    }
    
    return (Predicate[])predicates.toArray(new Predicate[0]);
  }
}
