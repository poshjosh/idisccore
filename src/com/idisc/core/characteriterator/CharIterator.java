package com.idisc.core.characteriterator;

import java.io.IOException;

/**
 * @author Josh
 */
public interface CharIterator {
    
    boolean hasNext();    
    
    char next() throws IOException;
}
