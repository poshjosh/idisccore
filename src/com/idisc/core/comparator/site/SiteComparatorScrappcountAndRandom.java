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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 3, 2016 6:10:20 AM
 */
public class SiteComparatorScrappcountAndRandom extends SiteComparatorScrappcount {

    @Override
    public List<String> sort(List<String> toSort) {

        Objects.requireNonNull(toSort);
        
        final List<String> sorted = new LinkedList<>();
        
        sorted.addAll(toSort);
        
        if(this.isToBeClearedBeforeSorting(toSort)) {

            this.clear();
            
            Collections.shuffle(sorted);
            
        }else{
        
            Collections.sort(sorted, this);
        }
        
        return sorted;
    }
}
