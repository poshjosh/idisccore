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

package com.idisc.core.comparator.site;

import com.idisc.core.comparator.Sorter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 3, 2016 6:01:56 AM
 */
public class SiteComparatorRandomParts implements Sorter<String>, Comparator<String> {
    
    /**
     * A value between 0.0 and 1.0.
     */
    private final float pivotFactor;

    private List<String> previousOrder;

    public SiteComparatorRandomParts() {
        this(0.5f);
    }
    
    public SiteComparatorRandomParts(float pivotFactor) {
        if(pivotFactor > 1.0) {
            throw new IllegalArgumentException();
        }
        this.pivotFactor = pivotFactor;
    }
    
    @Override
    public List<String> sort(List<String> toSort) {
        
        Objects.requireNonNull(toSort);
        
        final List<String> sorted = new LinkedList<>();
        
        sorted.addAll(toSort);
        
        Collections.sort(sorted, this); 
        
        previousOrder = sorted;
        
        return sorted;
    }

    @Override
    public int compare(String s1, String s2) {
        final int pivot = this.getPivot();
        final boolean b1;
        final boolean b2;
        if(pivot > 0) {
            b1 = this.isLastN(pivot, s1);
            b2 = this.isLastN(pivot, s2);
        }else{
            b1 = false;
            b2 = false;
        }
        final int output;
        if(b1 && !b2) {
            output = 1;
        }else if(b2 && !b1) {
            output = -1;
        }else{
            final double random = Math.random();
            output = Double.compare(random, 0.5);
        }
        return output;
    }
    
    private int getPivot() {
        int pivot;
        if(previousOrder != null) {
            pivot = (int)(previousOrder.size() * pivotFactor);
        }else{
            pivot = -1;
        }
        return pivot;
    }
    
    private boolean isLastN(int n, String s) {
        boolean output = false;
        if(previousOrder != null) {
            int index = previousOrder.indexOf(s);
            if(index != -1) {
                output = (previousOrder.size() - index) <= n;
            }
        } 
        return output;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' + "Previous order=" + previousOrder + '}';
    }
}
