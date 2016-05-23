package com.idisc.core;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Lock;

/**
 * This Map implementation does not accept Null keys or values.
 * @author poshjosh
 */
public class SimpleDictionary implements Serializable {
    
    private int size;
    
    private final int [] keyHashes;
    private final Object [] values;
    
    private final Lock lock;
    
    public SimpleDictionary() {
        this(20);
    }
    
    public SimpleDictionary(int maxSize) {
        keyHashes = new int[maxSize];
        for(int i=0; i<keyHashes.length; i++) {
            keyHashes[i] = -1;
        }
        values = new Object[maxSize];
        lock = new ReentrantLock();
    }
    
    /**
     * @param size
     * @param maxSize
     * @return The new size 
     */
    protected int resize(int size, int maxSize) {
        return size;
    }

    public boolean isFull() { 
        return size == keyHashes.length;
    }

    public boolean isEmpty() {
        return size < 1;
    }

    public int size() {
        return size;
    }

    public boolean containsKey(Object key) {
        this.rejectIfNull(key, "key");
        return indexOfKey(key) != -1;
    }

    public boolean containsValue(Object value) {
        this.rejectIfNull(value, "value");
        return indexOfValue(value) != -1;
    }
    
    public Object get(Object key) {
        this.rejectIfNull(key, "key");
        try{
            lock.lock();
            final int index = indexOfKey(key);
            return index == -1 ? null : values[index];
        }finally{
            lock.unlock();
        }
    }

    public Object put(Object key, Object value) {
        this.rejectIfNull(key, "key");
        this.rejectIfNull(value, "value");
        Object old_value;
        try{
            lock.lock();
            final int keyHash = key.hashCode();
            final int index = this.indexOfKeyHash(keyHash);
            if(index != -1) {
                old_value = values[index];
                values[index] = value;
            }else{
                old_value = null;
                size = this.resize(size, keyHashes.length);
                keyHashes[size] = keyHash;
                values[size] = value;
                ++size;
            }
        }finally{
            lock.unlock();
        }
        return old_value;
    }
    
    public void clear() {
        this.clear(0);
    }
    
    public void clear(int offset) {
        try{
            lock.lock();
            for(int i=offset; i<keyHashes.length; i++) {
                keyHashes[i] = -1;
                values[i] = null;
            }
            size = offset;
        }finally{
            lock.unlock();
        }
    }
    
    private int indexOfKey(Object key) {
        final int keyHash = key.hashCode();
        return this.indexOfKeyHash(keyHash);
    }
    private int indexOfKeyHash(int keyHash) {
        int output = -1;
        try{
            lock.lock();
            for(int i=0; i<keyHashes.length; i++) {
                int n = keyHashes[i];
                if(n == keyHash) {
                    output = i;
                    break;
                }
            }
        }finally{
            lock.unlock();
        }
        return output;
    }
    private int indexOfValue(Object val) {
        int output = -1;
        try{
            lock.lock();
            for(int i=0; i<values.length; i++) {
                Object e = values[i];
                if(e == null && val == null) {
                    output = i;
                    break;
                }else if(e != null && val != null) {
                    if(e.equals(val)) {
                        output = i;
                        break;
                    }
                }
            }
        }finally{
            lock.unlock();
        }
        return output;
    }
    private void rejectIfNull(Object oval, String type) {
        if(oval == null) {
            throw new NullPointerException("This Map implementation does not accept a Null "+type);
        }
    }
}
