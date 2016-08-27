/*
 * Copyright 2016 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core;

import com.bc.util.IntegerArray;
import java.io.Serializable;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 22, 2016 6:59:37 PM
 */
public class IntDict implements Serializable, Cloneable {
    
    private final IntegerArray keys;
    private final IntegerArray vals;

    public IntDict() {
        this(10);
    }
    
    public IntDict(int size) {
        keys = new IntegerArray(size);
        vals = new IntegerArray(size);
    }

    public IntDict(IntDict dict) {
        this(dict.size());
        this.addAll(dict);
    }

    public void clear() {
        keys.clear();
        vals.clear();
    }
    
    public int size() {
        return keys.size();
    }

    public int get(int key, int outputIfNone) {
        int index = keys.indexOf(key);
        return index == -1 ? outputIfNone : vals.get(index);
    }
  
    public void add(int key, int val) {
        keys.add(key);
        vals.add(val);
    }
    
    public void addAll(IntDict dict) {
        keys.addAll(dict.keys);
        vals.addAll(dict.vals);
    }
    
    public int put(int key, int val, int outputIfNone) {
        int index = keys.indexOf(key);
        if(index == -1) {
            keys.add(key);
            vals.add(val);
            return outputIfNone;
        }else{
            keys.set(index, key);
            vals.set(index, val);
            return vals.get(index);
        }
    }
    
    public void putAll(IntDict dict) {
        for(int i=0; i<dict.size(); i++) {
            int key = dict.keys.get(i);
            int val = dict.vals.get(i);
            this.put(key, val, -1);
        }        
    }
    
    public boolean containsKey(int key) {
        return keys.contains(key);
    }
    
    public boolean containsValue(int val) {
        return vals.contains(val);
    }
    
    public int indexOfKey(int key) {
        return keys.indexOf(key);
    }
    
    public int indexOfValue(int val) {
        return vals.indexOf(val);
    }
    
    public boolean isEmpty() {
        return keys.isEmpty();
    }
}
