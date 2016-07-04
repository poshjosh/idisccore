package com.idisc.core.comparator;

import java.util.Comparator;
import java.util.Date;

/**
 * @author Josh
 * @param <E>
 */
public abstract class AbstractComparator<E> implements Comparator<E> {
    
    private final boolean reverseOrder;

    public AbstractComparator() {
        
        this(false);
    }

    public AbstractComparator(boolean reverseOrder) {
        this.reverseOrder = reverseOrder;
    }
    
    public abstract long getScore(E e);

    @Override
    public int compare(E e1, E e2) {
        
        final long score1 = getScore(e1);
        
        final long score2 = getScore(e2);
        
        return this.compareLongs(score1, score2);
    }
    
    public final int compareInts(int x, int y) { 
        
        return this.reverseOrder ? Integer.compare(y, x) : Integer.compare(x, y);
    }
  
    public final int compareLongs(long x, long y) { 
        
        return this.reverseOrder ? Long.compare(y, x) : Long.compare(x, y);
    }

    public final int compareDates(Date date_a, Date date_b) { 
        if ((date_a == null) && (date_b == null)) {
            return 0;
        }    
        if (date_a == null) {
            return this.reverseOrder ? 1 : -1;
        }
        if (date_b == null) {
            return this.reverseOrder ? -1 : 1;
        }
        return this.reverseOrder ? date_b.compareTo(date_a) : date_a.compareTo(date_b);
    }
  
    public final boolean isReverseOrder() {
        
        return reverseOrder;
    }
}