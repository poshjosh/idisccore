package com.idisc.core.characteriterator;

import java.io.IOException;
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
    
    protected void appendChars(Appendable appendTo, E element) throws IOException {
        appendTo.append(String.valueOf(element));
    }
    
    @Override
    public boolean hasNext() {
        return this.hasNextListItem() || this.hasNextBufferChar();
    }
    
    protected boolean hasNextListItem() {
        return (nextInList < list.size());
    }
    
    protected boolean hasNextBufferChar() {
        return (nextInBuffer < buffer.length());
    }

    @Override
    public char next() throws IOException {
        return next(true);
    }
    
    protected char next(boolean increment) throws IOException {
        
//System.out.println("Increment: "+increment+", list offset: "+nextInList+", buffer offset: "+nextInBuffer+", bufferSize: "+buffer.length());        
        if(!this.hasNext()) {
            
            return CharacterIterator.DONE;
        }
        
        if(buffer.length() == 0) {
            
            E element = increment ? list.get(nextInList++) : list.get(nextInList);
            
            this.appendChars(buffer, element);
            
//System.out.println("= = = = = = = = = = Buffer refilled");            
        }else{
            
            if(nextInBuffer >= buffer.length()) {
                
                buffer.setLength(0);
                
                nextInBuffer = 0;
//System.out.println("= = = = = = = = = = Buffer cleared now recursing");                
                
                return next(increment);
            }
        }
        
        char next = increment ? buffer.charAt(nextInBuffer++) : buffer.charAt(nextInBuffer);
//System.out.println("Next: " + next + ", increment: "+increment+", list offset: "+nextInList+", buffer offset: "+nextInBuffer+", bufferSize: "+buffer.length());        
        return next;
    }
}
