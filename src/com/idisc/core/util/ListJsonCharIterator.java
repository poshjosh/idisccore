package com.idisc.core.util;

import com.bc.util.JsonFormat;
import com.idisc.core.EntityJsonFormat;
import java.util.List;

/**
 * @author Josh
 */
public class ListJsonCharIterator<E> extends ListCharIterator<E> {

    private final JsonFormat jsonFormat;
    
    private final StringBuilder listItemBuffer;
    
    public ListJsonCharIterator(List<E> list) {
        this(list, 1024, new EntityJsonFormat());
    }

    public ListJsonCharIterator(List<E> list, int bufferSize, JsonFormat jsonFormat) {
        super(list, bufferSize);
        this.jsonFormat = jsonFormat;
        this.listItemBuffer = new StringBuilder(bufferSize);
    }

    @Override
    public CharSequence getChars(E listItem) {
        this.listItemBuffer.setLength(0);
        this.jsonFormat.appendJSONString(listItem, listItemBuffer);
        return this.listItemBuffer;
    }
    
    public final JsonFormat getJsonFormat() {
        return jsonFormat;
    }
}
