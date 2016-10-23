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
package com.idisc.core.util;

import com.bc.jpa.JpaContext;
import com.bc.jpa.dao.BuilderForSelect;
import com.bc.jpa.util.EntityMapBuilder.Transformer;
import com.bc.jpa.util.EntityMapBuilderImpl;
import com.bc.util.JsonFormat;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.util.mapbuildertransformers.TransformerServiceImpl;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Sitetype;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.junit.Test;

/**
 * @author Josh
 */
public class EntityMapBuilderTest extends IdiscTestBase {
    
    public EntityMapBuilderTest() throws Exception {
        super(Level.FINE);
    }
    
    @Test
    public void testAll() {
        
        test(Feed.class, 2); // Feed, Site, Country
        test(Comment.class, 2);
        test(Installation.class, 2);
        test(Feeduser.class, 5);

//        this.testMetrics(Feed.class, 2);
        // Consumed memory: 236_434_280, time: 39_301
        // Consumed memory: -175024, time: 56
    }
    
    private void test(Class entityClass, int limit) {
        
        List found = this.getEntities(entityClass, limit);
        
        com.bc.jpa.util.EntityMapBuilder a = new EntityMapBuilderImpl(
        false, 3, 0,  null, new HashSet(Arrays.asList(Sitetype.class)));
        
        Transformer transformer = new TransformerServiceImpl(true, 20).get(entityClass);        
        
        JsonFormat jsonFmt = new JsonFormat(true);
        
        for(Object entity : found) {
System.out.println('\n');            
System.out.println("==============================="+entity);
System.out.println("-------------------------------"+a.getClass().getName());            
            Map map1 = a.build(entityClass, entity, transformer);
            String json1 = jsonFmt.toJSONString(new TreeMap(map1));
System.out.println(json1);    
        }    
    }

    private void testMetrics(Class entityClass, int limit) {
        
        List found = this.getEntities(entityClass, limit);
        
        com.bc.jpa.util.EntityMapBuilder a = new EntityMapBuilderImpl(
                false, 3, 0,  null, new HashSet(Arrays.asList(Sitetype.class)));
                
        Transformer transformer = new TransformerServiceImpl(true, 500).get(entityClass);        
        
        JsonFormat jsonFmt = new JsonFormat(true);

Runtime runtime = Runtime.getRuntime();
long mb4 = runtime.maxMemory() - runtime.totalMemory() - runtime.freeMemory();
long tb4 = System.currentTimeMillis();

        for(Object entity : found) {
System.out.println('\n');            

            Map map1 = new HashMap();
            a.build(entityClass, entity, map1, transformer);
            String json1 = jsonFmt.toJSONString(new TreeMap(map1));
System.out.println(json1);            
        }
long m = runtime.maxMemory() - runtime.totalMemory() - runtime.freeMemory();        
System.out.println("Method 1. Consumed memory: "+(mb4-m)+", time: "+(System.currentTimeMillis()-tb4));
    }
    
    private List getEntities(Class entityClass, int limit) {
        JpaContext jpaContext = this.getIdiscApp().getJpaContext();
        BuilderForSelect<Feed> dao = jpaContext.getBuilderForSelect(entityClass);
        List found = dao.getResultsAndClose(0, limit);
        return found;
    }
}
