package com.idisc.core.jpa;

import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Root;
import org.eclipse.persistence.annotations.BatchFetchType;

/**
 * @author poshjosh
 */
public class FeedSearch extends BaseSearch<Feed> {

  public FeedSearch() {
    super(Feed.class, new String[]{ "title", "keywords", "description", "content" }, "feeddate");
  }

  @Override
  protected void addOrderBy(CriteriaBuilder cb, CriteriaQuery<Feed> query, Root<Feed> root) {
    query.orderBy(new Order[] { cb.desc(root.get("feeddate")) });
  }
    
  @Override
  public TypedQuery<Feed> createTypedQuery(EntityManager em, String toFind, Date after)
  {

      TypedQuery<Feed> typedQuery = super.createTypedQuery(em, toFind, after);
 
      typedQuery.setHint("eclipselink.read-only", "true");

      typedQuery.setHint("eclipselink.batch", "f.commentList");
      typedQuery.setHint("eclipselink.batch", "f.feedhitList");
      typedQuery.setHint("eclipselink.batch", "f.bookmarkfeedList");
      typedQuery.setHint("eclipselink.batch", "f.favoritefeedList");
      typedQuery.setHint("eclipselink.batch.type", BatchFetchType.IN);

      return typedQuery;
  }
}
