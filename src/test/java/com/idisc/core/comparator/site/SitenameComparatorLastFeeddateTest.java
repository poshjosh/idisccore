/*
 * Copyright 2017 NUROX Ltd.
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

import com.idisc.core.IdiscTestBase;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class SitenameComparatorLastFeeddateTest extends IdiscTestBase {
    
    public SitenameComparatorLastFeeddateTest() throws Exception { }
    
    /**
     * Test of compare method, of class SitenameComparatorLastFeeddate.
     */
    @Test
    public void testCompare() {
        System.out.println("compare");
        
        final Predicate<String> notdefault = (sitename) -> !"default".equals(sitename);
        final List<String> sitenames = Arrays.asList(this.getSitenames())
                .stream().filter(notdefault).collect(Collectors.toList());

        final SitenameComparatorLastFeeddate instance = new SitenameComparatorLastFeeddate(this.getIdiscApp().getJpaContext());
        
        this.log("BEFORE SORT\n===========\n" + sitenames);
        
        Collections.sort(sitenames, instance); 
        
        this.log(" AFTER SORT\n===========\n" + sitenames);
    }
}
