package com.idisc.core.jpa;

import com.idisc.pu.entities.Comment;
import java.util.Date;

/**
 * Example class that wraps the execution of a {@link javax.persistence.TypedQuery} 
 * calculating the current size and then paging the results using the provided 
 * page size.<br/><br/>
 * <b>Notes:</b>
 * <ul>
 * <li>The query should contain an ORDER BY</li> 
 * <li>The following methods must not have been called on the query:<br/>
 * {@link javax.persistence.TypedQuery#setFirstResult(int)}<br/> 
 * {@link javax.persistence.TypedQuery#setMaxResults(int)}
 * </li>
 * <li>The usage of this may produce incorrect results if the matching data set 
 * changes on the database while the results are being paged.</li>
 * </ul>
 * @author poshjosh
 */
public class CommentSearchResults extends BaseSearchResults<Comment> {

    public CommentSearchResults() {
        this(null, 20);
    }

    public CommentSearchResults(String toFind, int batchSize) {
        this(toFind, null, batchSize);
    }

    public CommentSearchResults(String toFind, Date after, int batchSize) {
        super(new CommentSearch(), Comment.class, toFind, after, batchSize);
    }
}
