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

import com.bc.util.MapBuilder.Transformer;
import com.idisc.pu.entities.Comment_;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser_;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 3:02:59 AM
 */
public class CommentMapEntryTransformer implements Transformer {

    public CommentMapEntryTransformer() { }

    @Override
    public String transformKey(Object entity, String key) {
        return key;
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
    public Object transformValue(Object entity, String oldKey, String newKey, Object value) {
//System.out.println("= = = = = = = Old key: "+oldKey+", new key: "+newKey+", value: "+value);         
        if(value != null) {
            if(Comment_.feedid.getName().equals(oldKey)) {
                
                value = new Feed(((Feed)value).getFeedid());
                
            }else if(Feeduser_.feeduserid.getName().equals(oldKey)) {
                
                value = null;
            }
        }
        return value;
    }
}
