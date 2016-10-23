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

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 1:13:28 PM
 */
public class TransformerChain implements Transformer {

    private final Transformer [] chain;
    
    public TransformerChain(Transformer... chain) {
        this.chain = chain;
    }

    @Override
    public String transformKey(Object entity, String key) {
        if(chain == null || chain.length == 0) {
            return key;
        }else{
            for(Transformer t : chain) {
                key = t.transformKey(entity, key);
            }
        } 
        return key;
    }

    @Override
    public Object transformValue(Object entity, String oldKey, String newKey, Object value) {
        if(chain == null || chain.length == 0) {
            return value;
        }else{
            for(Transformer t : chain) {
                value = t.transformValue(entity, oldKey, newKey, value);
            }
        } 
        return value;
    }
}
