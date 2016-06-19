package com.idisc.core.characteriterator;

import com.bc.util.JsonBuilder;
import com.idisc.core.util.EntityJsonBuilder;
import java.io.IOException;
import java.util.List;

/**
 * @author Josh
 * @param <E>
 */
public class ListJsonCharIterator<E> extends ListCharIterator<E> {
    
    private boolean doneFirst;

    private final JsonBuilder jsonBuilder;
    
    public ListJsonCharIterator(List<E> list) {
        this(list, 8192, new EntityJsonBuilder(8192));
    }

    public ListJsonCharIterator(List<E> list, int bufferSize, JsonBuilder jsonFormat) {
        super(list, bufferSize);
        if(list == null || list.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        this.jsonBuilder = jsonFormat;
    }

    /**
     * This method is overriden to construct the output in a json format
     * @param appendTo
     * @param listItem 
     * @throws java.io.IOException 
     */
    @Override
    protected void appendChars(Appendable appendTo, E listItem) throws IOException {
        
        if(!doneFirst) {
            
            doneFirst = true;
            
            appendTo.append('[');
            
        }else{
            
            appendTo.append(',').append(' ');
        }
        
        this.jsonBuilder.appendJSONString(listItem, appendTo);
        
        if(!this.hasNextListItem()) {
         
            appendTo.append(']');
        }
    }

    public final boolean isDoneFirst() {
        return doneFirst;
    }
    
    public final JsonBuilder getJsonBuilder() {
        return jsonBuilder;
    }
}
