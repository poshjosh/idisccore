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

package com.idisc.core.util.mapbuildertransformers;

import com.bc.jpa.util.EntityMapBuilder.Transformer;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Feeduser_;
import com.idisc.pu.entities.Installation;
import com.idisc.pu.entities.Installation_;
import java.util.List;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 3:38:43 AM
 */
public class FeeduserMapEntryTransformer implements Transformer<Feeduser> {

    public FeeduserMapEntryTransformer() { }
    
    @Override
    public String transformKey(Feeduser entity, String key) {
        return Feeduser_.installationList.getName().equals(key)?
            Installation_.installationid.getName() : key;
    }
    
    /**
     * <b>Subclasses should call super and assign the return value</b>
     * @param entity The entity being transformed
     * @param oldKey The pre-transformation name of the field representing the value to be transformed
     * @param newKey The post-transformation name of the field representing the value to be transformed
     * @param value The value to be transformed
     * @return The transformed value
     */
    @Override
    public Object transformValue(Feeduser entity, String oldKey, String newKey, Object value) {
//System.out.println("= = = = = = = Old key: "+oldKey+", new key: "+newKey+", value: "+value);                    
        if(value != null) {

            if(Feeduser_.installationList.getName().equals(oldKey) && 
                    Installation_.installationid.getName().equals(newKey)) {
                
                value = this.getLastInstallation((List)value);
            }
        }
        
        return value;
    }
    
    private Installation getLastInstallation(List installations) {
        
        Installation output;

        if ((installations != null) && (!installations.isEmpty())) {

          output = (Installation)installations.get(installations.size() - 1);

        } else {
            
          output = null;
        }
        
        return output;
    }
}
