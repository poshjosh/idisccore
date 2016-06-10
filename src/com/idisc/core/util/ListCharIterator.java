package com.idisc.core.util;

import java.io.Serializable;
import java.text.CharacterIterator;
import java.util.Collections;
import java.util.List;

/**
 * @author Josh
 */
public class ListCharIterator<E> implements CharIterator, Serializable {
    
    private final List<E> list;
    
    private final StringBuilder buffer;
    
    private int nextInList;
    
    private int nextInBuffer;

    public ListCharIterator(List<E> list) {
        this(list, 1024);
    }
    
    public ListCharIterator(List<E> list, int bufferSize) {
        this.list = Collections.unmodifiableList(list);
        this.buffer = new StringBuilder(bufferSize);
    }
    
    public CharSequence getChars(E element) {
        return String.valueOf(element);
    }
    
    @Override
    public boolean hasNext() {
        return next(false) != CharacterIterator.DONE;
    }

    @Override
    public char next() {
        return next(true);
    }
    
    private char next(boolean increment) {
//System.out.println("Increment: "+increment+", list offset: "+nextInList+", buffer offset: "+nextInBuffer+", bufferSize: "+buffer.length());        
        if(nextInList >= list.size() && nextInBuffer >= buffer.length()) {
            return CharacterIterator.DONE;
        }
        if(buffer.length() == 0) {
            E element = this.nextElement(increment);
            buffer.append(getChars(element));
            nextInBuffer = 0;
//System.out.println("= = = = = = = = = = Buffer refilled");            
        }else{
            if(nextInBuffer >= buffer.length()) {
                buffer.setLength(0);
//System.out.println("= = = = = = = = = = Buffer cleared now recursing");                
                ++nextInList;
                return next(increment);
            }
        }
        char next = increment ? buffer.charAt(nextInBuffer++) : buffer.charAt(nextInBuffer);
//System.out.println("Next: " + next + ", increment: "+increment+", list offset: "+nextInList+", buffer offset: "+nextInBuffer+", bufferSize: "+buffer.length());        
        return next;
    }
    
    protected E nextElement(boolean increment) {
        return increment ? list.get(nextInList++) : list.get(nextInList);
    }
}
