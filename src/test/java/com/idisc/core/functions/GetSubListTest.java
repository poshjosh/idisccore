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
package com.idisc.core.functions;

import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class GetSubListTest {
    
    public GetSubListTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of apply method, of class GetSubList.
     */
    @Test
    public void testApply() {
        
        System.out.println("apply");
        
        final GetSubList instance = new GetSubList();
        
        final int limit = 5;
        
        final List<String> list = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8");
        
        System.out.println(" All names: " + list);
        
        for(int i=0; i<10; i++) {
            
            System.out.println("    Offset: " + instance.getOffset());
            
            final List next = instance.apply(list, limit);
            
            System.out.println("Next names: " + next);
        }
    }
}
