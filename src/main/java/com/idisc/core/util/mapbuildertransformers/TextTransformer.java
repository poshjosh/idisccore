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

import com.idisc.core.functions.GetPlainText;
import java.util.logging.Logger;
import com.idisc.pu.entities.Feed_;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 12:45:25 PM
 */
public class TextTransformer implements com.bc.util.MapBuilder.Transformer {
    
    private transient static final Logger LOG = Logger.getLogger(TextTransformer.class.getName());

    private final boolean plainTextOnly;
  
    private final int maxTextLength;
    
    private GetPlainText getPlainText;
    
    public TextTransformer(boolean plainTextOnly, int maxTextLength) {
        this.plainTextOnly = plainTextOnly;
        this.maxTextLength = maxTextLength;
    }    

    @Override
    public String transformKey(Object entity, String key) {
        return key;
    }
    
    /**
     * <b>Subclasses should call super</b>
     * @param entity The entity being transformed
     * @param oldKey The pre-transformation name of the field representing the value to be transformed
     * @param newKey The post-transformation name of the field representing the value to be transformed
     * @param value The value to be transformed
     * @return The transformed value
     */
    @Override
    public Object transformValue(Object entity, String oldKey, String newKey, Object value) {
        
        if(value != null) {

            final String sval = value.toString();

            if(Feed_.content.getName().equals(oldKey) || Feed_.description.getName().equals(oldKey)) {
                if(this.plainTextOnly) {
                    value = this.getPlainText(sval);
                }
                if(this.maxTextLength > 0) {
                    value = truncate(sval, this.maxTextLength);
                }
            } 

            if(Feed_.title.getName().equals(oldKey) || Feed_.keywords.getName().equals(oldKey)) {
                if(this.maxTextLength > 0) {
                    value = truncate(sval, this.maxTextLength);
                }
            } 
        }
        
        return value;
    }

    private String getPlainText(String s) {
        
        final String output = s == null || s.isEmpty() ? s : this.getPlainText.apply(s, s);
        
        LOG.finer(() -> "Input: " + s + ", output: " + output);
        
        return output;
    }
  
    private String truncate(String sval, int maxLen) {
        if (sval == null || sval.isEmpty()) {
            return sval;
        }
        if (sval.length() <= maxLen) {
            return sval;
        }
        return sval.substring(0, maxLen);
    }
}
