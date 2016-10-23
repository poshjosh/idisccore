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

import com.bc.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * @author Josh
 */
public class SiteComparatorScrappcountTest {
    
    public SiteComparatorScrappcountTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

//    @Test
    public void test() {
System.out.println("\ntest()");

        final SiteComparatorScrappcount sorter = new SiteComparatorScrappcount();
        for(int i=0; i<3; i++) {
            sorter.incrementAndGet(this.getName(i), i);
        }
        final int indexToFind = 1;
        final String nameToFind = this.getName(indexToFind);
        int index = sorter.indexOfName(nameToFind);
        int count = sorter.getCount(nameToFind, -1);
System.out.println("To find: "+nameToFind+", found at index: "+index+", count at index: "+count);
        assertEquals("???", indexToFind, index);
        assertEquals("???", indexToFind, count);
        int addToCount = 0;
        int add = 1;
        sorter.incrementAndGet(nameToFind, add); addToCount += add;
        add = 2;
        sorter.incrementAndGet(nameToFind, add); addToCount += add;
        index = sorter.indexOfName(nameToFind);
        count = sorter.getCount(nameToFind, -1);
System.out.println("To find: "+nameToFind+", found at index: "+index+", count at index: "+count);
        assertEquals("???", indexToFind, index);
        assertEquals("???", indexToFind + addToCount, count);
    }

    @Test
    public void test1() {
        this.test(new SiteComparatorScrappcount());
        this.test(new SiteComparatorScrappcountAndRandom());
    }
    
    public void test(SiteComparatorScrappcount sorter) {
System.out.println("\ntest("+sorter.getClass().getSimpleName()+")");

        final int size = 5;
        
        List<String> names = new ArrayList<>();
        for(int i=0; i<size; i++) {
            names.add(this.getName(i));
        }

        for(int group=0; group<4; group++) {
            for(int i=0; i<size; i++) {
                final String name = this.getName(i);
                final int add = Util.randomInt(size);
//                if(group == 0) {
//                    add = i + 1;
//                }else{
//                    add = sorter.getAverageCount(name, 0) * 2;
//                }
                sorter.incrementAndGet(name, add);
            }
System.out.println(sorter);            
System.out.println(sorter.sort(names));
        }
    }
    
    private String getName(int i) {
        switch(i) {
            case 0: return "A";
            case 1: return "B";
            case 2: return "C";
            case 3: return "D";
            case 4: return "E";
            case 5: return "F";
            case 6: return "G";
            case 7: return "H";
            case 8: return "I";
            case 9: return "J";
            case 10: return "K";
            case 11: return "L";
            case 12: return "M";
            case 13: return "N";
            case 14: return "O";
            case 15: return "P";
            case 16: return "Q";
            case 17: return "R";
            case 18: return "S";
            case 19: return "T";
            case 20: return "U";
            case 21: return "V";
            case 22: return "W";
            case 23: return "X";
            case 24: return "Y";
            case 25: return "Z";
            default : throw new IllegalArgumentException("Only numbers 0-25 inclusive are legal");
        }
    }
}
