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

import com.bc.jpa.util.MapBuilderForEntity;
import com.bc.util.MapBuilder;
import com.bc.util.MapBuilder.Transformer;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation_;
import com.idisc.pu.entities.Sitetype;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 3:43:20 AM
 */
public class DefaultEntityMapBuilder extends MapBuilderForEntity {

    private transient static final Logger LOG = Logger.getLogger(DefaultEntityMapBuilder.class.getName());

    public DefaultEntityMapBuilder() {
            this.methodFilter(MapBuilder.MethodFilter.ACCEPT_ALL)
            .nullsAllowed(false)
            .maxDepth(3)
            .maxCollectionSize(0)
            .typesToIgnore(new HashSet(Arrays.asList(Sitetype.class)));
    }

    @Override
    protected Map build(Class srcType, Object src, Transformer tx, int depth, Map tgt, Set<Class> alreadyBuilt, boolean addToAlreadyBuilt) {
        
        tgt = super.build(srcType, src, tx, depth, tgt, alreadyBuilt, addToAlreadyBuilt); 
        
        if(srcType == Feeduser.class) {
            
            final String name = Installation_.installationid.getName();
            
            final Object installation = tgt.get(name);
            
            if(installation != null) {
                
                final Map map = this.toMap(name, installation, Number.class, Collections.EMPTY_MAP);
                
                tgt.putAll(map);
            }
        }
        
        return tgt;
    }
    
    public Map toMap(String name, Object value, Class idType, Map outputIfNone) {
        final Map map;
        if(value instanceof Map) {
            map = (Map)value;
        }else if(idType.isAssignableFrom(value.getClass())){
            map = Collections.singletonMap(name, value);
        }else{
            map = outputIfNone;
            LOG.warning(() -> "Expected: java.util.Map or "+idType.getName()+", found: " + 
                    value.getClass().getName() + ' ' + value);
        }
        return map;
    }
}
