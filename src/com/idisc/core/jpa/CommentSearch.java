package com.idisc.core.jpa;

import com.idisc.pu.entities.Comment;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.eclipse.persistence.annotations.BatchFetchType;

/**
 * @author poshjosh
 */
public class CommentSearch extends BaseSearch<Comment> {

  public CommentSearch() {
    super(Comment.class, new String[]{"commentSubject", "commentText"}, "datecreated");
  }
    
  @Override
  public TypedQuery<Comment> createTypedQuery(EntityManager em, String toFind, Date after)
  {

      TypedQuery<Comment> typedQuery = super.createTypedQuery(em, toFind, after);
 
      typedQuery.setHint("eclipselink.read-only", "true");

      typedQuery.setHint("eclipselink.batch", "c.commentList");
      typedQuery.setHint("eclipselink.batch.type", BatchFetchType.IN);

      return typedQuery;
  }
}
