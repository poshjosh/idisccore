package com.idisc.core.html;

/**
 * @author Josh
 * @param <E>
 */
public interface ToHtml<E> {
    
    String toHtml(E e);
    
    void appendHtml(E e, StringBuilder appendTo);
}
