package com.idisc.core;

import com.idisc.core.util.SimpleDictionary;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author posh
 */
public class SimpleDictionaryTest {
    
    public SimpleDictionaryTest() {
    }

    /**
     * Test of isFull method, of class SimpleDictionary.
     */
    @Test
    public void testIsFull() {
        System.out.println("isFull");
        final int max = 20;
        SimpleDictionary instance = new SimpleDictionary(max);
        final int maxMinusOne = max - 1;
        for(int i=0; i<maxMinusOne; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        boolean expResult = false;
        boolean result = instance.isFull();
        assertEquals(expResult, result);
    }

    /**
     * Test of size method, of class SimpleDictionary.
     */
    @Test
    public void testSize() {
        System.out.println("size");
        final int max = 20;
        SimpleDictionary instance = new SimpleDictionary(max);
        final int size = max / 2;
        for(int i=0; i<size; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        int expResult = size;
        int result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of isEmpty method, of class SimpleDictionary.
     */
    @Test
    public void testIsEmpty() {
        System.out.println("isEmpty");
        SimpleDictionary instance = new SimpleDictionary();
        boolean expResult = true;
        boolean result = instance.isEmpty();
        assertEquals(expResult, result);
        instance.put(0, "First");
        expResult = false;
        result = instance.isEmpty();
        assertEquals(expResult, result);
    }

    /**
     * Test of containsKey method, of class SimpleDictionary.
     */
    @Test
    public void testContainsKey() {
        System.out.println("containsKey");
        final int size = 10;
        SimpleDictionary instance = new SimpleDictionary();
        for(int i=0; i<size; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        Object key = "key_"+size;
        boolean expResult = false;
        boolean result = instance.containsKey(key);
        assertEquals(expResult, result);
        key = "key_"+(size-1);
        expResult = true;
        result = instance.containsKey(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of containsValue method, of class SimpleDictionary.
     */
    @Test
    public void testContainsValue() {
        System.out.println("containsValue");
        final int size = 10;
        SimpleDictionary instance = new SimpleDictionary();
        for(int i=0; i<size; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        Object value = "value_"+size;
        boolean expResult = false;
        boolean result = instance.containsValue(value);
        assertEquals(expResult, result);
        value = "value_"+(size-1);
        expResult = true;
        result = instance.containsValue(value);
        assertEquals(expResult, result);
    }

    /**
     * Test of get method, of class SimpleDictionary.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        Object key = "key_"+0;
        SimpleDictionary instance = new SimpleDictionary();
        Object expResult = null;
        Object result = instance.get(key);
        assertEquals(expResult, result);
        expResult = "value_"+0;
        instance.put(key, expResult);
        result = instance.get(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of put method, of class SimpleDictionary.
     */
    @Test
    public void testPut() {
        System.out.println("put");
        Object key = 0;
        Object first = "First";
        SimpleDictionary instance = new SimpleDictionary();
        Object expResult = null;
        Object result = instance.put(key, first);
        assertEquals(expResult, result);
        Object second = "Second";
        expResult = first;
        result = instance.put(key, second);
        assertEquals(expResult, result);
    }

    /**
     * Test of clear method, of class SimpleDictionary.
     */
    @Test
    public void testClear_0args() {
        System.out.println("clear");
        SimpleDictionary instance = new SimpleDictionary();
        instance.clear();
        Object expResult = 0;
        Object result = instance.size();
        assertEquals(expResult, result);
        final int size = 10;
        for(int i=0; i<size; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        instance.clear();
        expResult = 0;
        result = instance.size();
        assertEquals(expResult, result);
    }

    /**
     * Test of clear method, of class SimpleDictionary.
     */
    @Test
    public void testClear_int() {
        SimpleDictionary instance = new SimpleDictionary();
        instance.clear(0);
        Object expResult = 0;
        Object result = instance.size();
        assertEquals(expResult, result);
        final int size = 10;
        for(int i=0; i<size; i++) {
            instance.put("key_"+i, "value_"+i);
        }
        final int offset = (int)size/2;
        expResult = offset;
        instance.clear(offset);
        result = instance.size();
        assertEquals(expResult, result);
        instance.clear(instance.size()-1);
        expResult = instance.size();
        result = instance.size();
        assertEquals(expResult, result);
    }
}
