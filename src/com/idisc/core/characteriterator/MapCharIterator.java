package com.idisc.core.characteriterator;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Josh
 */
public class MapCharIterator<K, V> extends ListCharIterator<Map.Entry<K, V>> {

    public MapCharIterator(Map<K, V> map) {
        super(new ArrayList(map.entrySet()));
    }

    public MapCharIterator(Map<K, V> map, int bufferSize) {
        super(new ArrayList(map.entrySet()), bufferSize);
    }
}
