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
import com.idisc.core.IdiscTestBase;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import org.junit.Test;

/**
 * @author Josh
 */
public class EntityJsonBuilderTest  extends IdiscTestBase {
    
    public EntityJsonBuilderTest() throws Exception {
        super(Level.FINE);
    }

    /**
     * Test of appendJSONString method, of class EntityJsonBuilder.
     * @throws java.io.IOException
     */
    @Test
    public void testAppendJSONString() throws IOException {

        this.appendJsonString(Feed.class, "feeds", 3);
        this.appendJsonString(Comment.class, "comments", 3);
    }

    private <E> void appendJsonString(Class<E> entityType, String key, int limit) throws IOException {
        
        System.out.println("appendJSONString");
        
        final JpaContext jpaContext = this.getIdiscApp().getJpaContext();
        
        final String idColumn = jpaContext.getMetaData().getIdColumnName(entityType);
        
        final List<E> found = jpaContext.getBuilderForSelect(entityType)
                .descOrder(idColumn).getResultsAndClose(0, limit);
System.out.println(key + ":\n"+found);         
        
        final Map toAppend = Collections.singletonMap(key, found);
        
        final StringBuilder appendTo = new StringBuilder();
        
        final EntityJsonBuilder instance = new EntityJsonBuilder(true, false,  20);
        
        instance.appendJSONString(toAppend, appendTo);
        
System.out.println(key + " Json:\n"+appendTo);        
    }
}
