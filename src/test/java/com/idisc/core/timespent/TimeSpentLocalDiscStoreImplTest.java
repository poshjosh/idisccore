/*
 * Copyright 2018 NUROX Ltd.
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
package com.idisc.core.timespent;

import com.bc.timespent.TimeSpent;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josh
 */
public class TimeSpentLocalDiscStoreImplTest {
    
    private final TimeSpentProvider provider;
    
    public TimeSpentLocalDiscStoreImplTest() {
        this.provider = new TimeSpentProvider();
    }

    /**
     * Test of various methods method, of class TimeSpentLocalDiscStore.
     */
    @Test
    public void test() {
        System.out.println("test");
        
        final String uniqueKey = this.getClass().getName() + "_sample_"+TimeSpent.class.getName()+"_" + Long.toHexString(System.currentTimeMillis()) + ".javaobject";
        
        TimeSpentLocalDiscStore instance = new TimeSpentLocalDiscStore();
        
        TimeSpent toPut = provider.getInstance(true);
        
        System.out.println("To put: " + toPut);
        
        try{
            
            TimeSpent existing = instance.put(uniqueKey, toPut);
            
            System.out.println("Existing: " + existing);
            
            assertNull(existing);
            
            TimeSpent foundAfterPut = instance.getOrDefault(uniqueKey, null);
            
            System.out.println("After put, found: " + foundAfterPut);
            
            assertEquals(toPut, foundAfterPut);
            
        }finally{
            
            TimeSpent removed = instance.remove(uniqueKey);
            
            System.out.println("Removed: " + removed);
            
            assertEquals(toPut, removed);
            
            TimeSpent foundAfterRemove = instance.getOrDefault(uniqueKey, null);
            
            System.out.println("After remove, found: " + foundAfterRemove);
            
            assertNull(foundAfterRemove);
        }
    }
}
