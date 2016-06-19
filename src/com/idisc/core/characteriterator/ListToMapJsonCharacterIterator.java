package com.idisc.core.characteriterator;

import com.bc.util.JsonBuilder;
import java.io.IOException;
import java.util.List;

/**
 * @author Josh
 */
public class ListToMapJsonCharacterIterator<E> extends ListJsonCharIterator<E> {

    private final String name;
    
    public ListToMapJsonCharacterIterator(String name, List<E> list) {
        super(list);
        if(name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    public ListToMapJsonCharacterIterator(String name, List<E> list, int bufferSize, JsonBuilder jsonBuilder) {
        super(list, bufferSize, jsonBuilder);
        if(name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }
    
    /**
     * This method is overriden to construct the output in a json format
     * @param appendTo
     * @param listItem 
     * @throws java.io.IOException 
     */
    @Override
    protected void appendChars(Appendable appendTo, E listItem) throws IOException {
        
        if(!this.isDoneFirst()) {
            appendTo.append('{').append('"').append(name).append('"').append(':');
        }
        
        super.appendChars(appendTo, listItem);
        
        if(!this.hasNextListItem()) {
         
            appendTo.append('}');
        }
    }

}
