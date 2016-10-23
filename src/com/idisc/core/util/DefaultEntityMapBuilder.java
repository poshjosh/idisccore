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

import com.bc.jpa.util.EntityMapBuilderImpl;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Installation_;
import com.idisc.pu.entities.Sitetype;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 3:43:20 AM
 */
public class DefaultEntityMapBuilder extends EntityMapBuilderImpl {

    public DefaultEntityMapBuilder() {
        super(false, 3, 0,  null, new HashSet(Arrays.asList(Sitetype.class)));
    }
    
    @Override
    public void build(Class entityType, Object entity, Map appendTo, 
            com.bc.jpa.util.EntityMapBuilder.Transformer transformer) {
        
        super.build(entityType, entity, appendTo, transformer);
        
        if(entityType == Feeduser.class) {
            
            Map installationMap = (Map)appendTo.get(Installation_.installationid.getName());
            
            if(installationMap != null) {
                
                appendTo.putAll(installationMap);
            }
        }
    }
}
