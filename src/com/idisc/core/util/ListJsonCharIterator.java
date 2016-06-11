package com.idisc.core.util;

import com.bc.util.JsonFormat;
import com.idisc.core.EntityJsonFormat;
import java.util.List;

/**
 * @author Josh
 */
public class ListJsonCharIterator<E> extends ListCharIterator<E> {
    
    private boolean doneFirst;

    private final JsonFormat jsonFormat;
    
    public ListJsonCharIterator(List<E> list) {
        this(list, 1024, new EntityJsonFormat());
    }

    public ListJsonCharIterator(List<E> list, int bufferSize, JsonFormat jsonFormat) {
        super(list, bufferSize);
        if(list == null || list.isEmpty()) {
            throw new UnsupportedOperationException();
        }
        this.jsonFormat = jsonFormat;
    }

    /**
     * This method is overriden to construct the output in a json format
     * @param appendTo
     * @param listItem 
     */
    @Override
    protected void appendChars(StringBuilder appendTo, E listItem) {
        
        if(!doneFirst) {
            
            doneFirst = true;
            
            appendTo.append('[');
            
        }else{
            
            appendTo.append(',').append(' ');
        }
        
        this.jsonFormat.appendJSONString(listItem, appendTo);
        
        if(!this.hasNextListItem()) {
         
            appendTo.append(']');
        }
    }
    
    public final JsonFormat getJsonFormat() {
        return jsonFormat;
    }
}
