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

import com.bc.util.XLogger;
import com.idisc.core.comparator.Sorter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 3, 2016 5:44:21 AM
 */
public class SiteComparatorScrappcount implements Sorter<String>, Comparator<String>, IncrementableValues<String> {

    private final class NamedCount {
        private int updateCount;
        private int count;
        private String name;
        @Override
        public String toString() {
            return '('+name+'='+count+')';
        }
    }
    
    private final List<NamedCount> namedCounts;

    public SiteComparatorScrappcount() {
        this.namedCounts = Collections.synchronizedList(new ArrayList<NamedCount>());
    }
    
    @Override
    public List<String> sort(List<String> toSort) {

        Objects.requireNonNull(toSort);
        
        final List<String> sorted = new LinkedList<>();
        
        sorted.addAll(toSort);
        
        if(this.isToBeClearedBeforeSorting(toSort)) {

            this.clear();
        }
        
        Collections.sort(sorted, this);
        
        return sorted;
    }
    
    @Override
    public int compare(String name1, String name2) {
        final NamedCount one = get(name1, null);
        final NamedCount two = get(name2, null);
        if(one != null && two != null) {
            return Integer.compare(one.count, two.count);
        }else if (one == null && two == null) {
            return 0;
        }else if(one == null && two != null) {
            return -1;
        }else{
            return 1;
        }
    }
    
    public boolean isToBeClearedBeforeSorting(List<String> toDistribute) {
        return this.containsAllNames(toDistribute) && this.isAllUpdated();
    }
    
    public boolean containsAllNames(Collection<String> all) {
        return this.asListOfNames().containsAll(all);
    }
    
    public void clear() {
XLogger.getInstance().log(Level.FINE, "Clearing named counts: {0}", this.getClass(), namedCounts);
        namedCounts.clear();
    }
    
    public int indexOfName(String name) {
        int output = -1;
        synchronized(namedCounts) {
            for(int i=0; i<namedCounts.size(); i++) {
                NamedCount namedCount = namedCounts.get(i);
                if(namedCount.name.equals(name)) {
                    output = i;
                    break;
                }
            }
        }
        return output;
    }
    
    @Override
    public int incrementAndGet(String key, int additional) {
        int output = additional;
        synchronized(namedCounts) {
            boolean found = false;
            for(NamedCount namedCount:namedCounts) {
                if(namedCount.name.equals(key)) {
                    namedCount.count += additional;
                    namedCount.updateCount += 1;
                    output = namedCount.count;
                    found = true;
                    break;
                }
            }
            if(!found) {
                NamedCount namedCount = new NamedCount();
                namedCount.name = key;
                namedCount.count = additional;
                namedCount.updateCount += 1;
                output = namedCount.count;
                namedCounts.add(namedCount);
            }
        }

XLogger.getInstance().log(Level.FINE, "After incrementing by {0}, {1} = {2}", 
        this.getClass(), additional, key, output);

        return output;
    }
    
    public int getAverageCount(String name, int outputIfNone) {
        int output = outputIfNone; 
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                if(namedCount.name.equals(name)) {
                    output = namedCount.count/namedCount.updateCount;
                    break;
                }
            }
        }  
        return output;
    }
    
    public int getCount(String name, int outputIfNone) {
        int output = outputIfNone; 
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                if(namedCount.name.equals(name)) {
                    output = namedCount.count;
                    break;
                }
            }
        }  
        return output;
    }
    
    public int getTotalAverageCount(int outputIfNone) {
        int output = outputIfNone;
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                output += (namedCount.count/namedCount.updateCount);
            }
        }
        return output;
    }
    
    public int getTotalCount(int outputIfNone) {
        int output = outputIfNone;
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                output += namedCount.count;
            }
        }
        return output;
    }

    private boolean isAllUpdated() {
        boolean output = true;
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                if(!this.isUpdated(namedCount)) {
                    output = false;
                    break;
                }
            }
        }
        return output;
    }
    
    private boolean isUpdated(NamedCount namedCount) {
        return namedCount.updateCount >= 3 || namedCount.count >= 9;
    }
    
    private NamedCount get(String name, NamedCount outputIfNone) {
        NamedCount output = outputIfNone;
        synchronized(namedCounts) {
            for(int i=0; i<namedCounts.size(); i++) {
                NamedCount namedCount = namedCounts.get(i);
                if(namedCount.name.equals(name)) {
                    output = namedCount;
                    break;
                }
            }
        }
        return output;
    }

    private List<String> asListOfNames() {
        final List<String> output = new ArrayList(namedCounts.size());
        synchronized(namedCounts) {
            for(NamedCount namedCount:namedCounts) {
                output.add(namedCount.name);
            }
        }
        return output;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' + "namedCounts=" + namedCounts + '}';
    }
}
