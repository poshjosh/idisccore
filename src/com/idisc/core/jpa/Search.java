package com.idisc.core.jpa;

import java.util.Date;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

/**
 * @author poshjosh
 */
public interface Search<T> {

    TypedQuery<T> createTypedQuery(EntityManager em, int offset, int limit);

    TypedQuery<T> createTypedQuery(EntityManager em, String toFind, Date after, int offset, int limit);

    TypedQuery<T> createTypedQuery(EntityManager em);

    TypedQuery<T> createTypedQuery(EntityManager em, String toFind, Date after);

    List<T> select(int offset, int limit);

    List<T> select(String toFind, Date after, int offset, int limit);
    
}
