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

import com.bc.jpa.context.JpaContext;
import com.bc.util.StringEscapeUtils;
import com.bc.util.Util;
import com.idisc.core.IdiscTestBase;
import com.idisc.pu.entities.Bookmarkfeed;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Commentreplynotice;
import com.idisc.pu.entities.Favoritefeed;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Site;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/**
 * @author Josh
 */
public class EntityJsonBuilderTest  extends IdiscTestBase {
    
    private final boolean unescapeHtml = true;
    
    public EntityJsonBuilderTest(){ }

    /**
     * Test of appendJSONString method, of class EntityJsonBuilder.
     * @throws java.io.IOException
     */
    @Test
    public void testAppendJSONString() throws IOException {

        final long tb4 = System.currentTimeMillis();
        final long mb4 = Util.availableMemory();
        
        this.appendJsonString(Feed.class, "feeds", 50);
        
        System.out.println("Done. Consumed. Memory: " + Util.usedMemory(mb4) +
                ", time: " + (System.currentTimeMillis() - tb4));
//Done. Consumed. Memory: 27467048, time: 310421 - unescapeHtml = false   
//Done. Consumed. Memory: 62002312, time: 267039 - unescapeHtml = true
        if(true) {
            return;
        }
        
        this.appendJsonString(Comment.class, "comments", 3);
        this.appendJsonString(Feeduser.class, "feedusers", 3);
        this.appendJsonString(Installation.class, "installs", 3);
        this.appendJsonString(Site.class, "sites", 3);
        this.appendJsonString(Bookmarkfeed.class, "bookmarks", 3);
        this.appendJsonString(Commentreplynotice.class, "commentreplynotices", 3);
        this.appendJsonString(Favoritefeed.class, "favorites", 3);
    }

    private <E> void appendJsonString(Class<E> entityType, String key, int limit) throws IOException {
        
        System.out.println("appendJSONString");
        
        final JpaContext jpaContext = this.createJpaContext();
        
        final String idColumn = jpaContext.getMetaData().getIdColumnName(entityType);
        
        final List<E> found = jpaContext.getDaoForSelect(entityType)
                .from(entityType)
                .descOrder(idColumn).getResultsAndClose(0, limit);
System.out.println(key + ":\n"+found);         
        
        final Map toAppend = Collections.singletonMap(key, found);
        
        final StringBuilder appendTo = new StringBuilder();
        
        final EntityJsonBuilder instance = new EntityJsonBuilder(true, false,  20){
            @Override
            public void escape(CharSequence s, Appendable appendTo) throws IOException {
                if(unescapeHtml) {
                    s = StringEscapeUtils.unescapeHtml(s.toString());
                }
                super.escape(s, appendTo);
            }
        };
        
        instance.appendJSONString(toAppend, appendTo);
        
System.out.println(appendTo);        
    }
}
