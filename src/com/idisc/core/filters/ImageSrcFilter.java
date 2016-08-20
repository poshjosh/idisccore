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

package com.idisc.core.filters;

import com.scrapper.Filter;
import java.util.regex.Pattern;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 10:13:40 AM
 */
public class ImageSrcFilter implements Filter<String> {
    
    private final Pattern toReject;
    
    private final Pattern toAccept;
    
    private final String baseUrl;

    public ImageSrcFilter(String baseUrl) {
    
        this(baseUrl, null, null);
    }
    
    public ImageSrcFilter(String baseUrl, String regexToAccept, String regexToReject) {
    
        this.baseUrl = baseUrl;
        
        this.toAccept = getPattern(regexToAccept);
        
        this.toReject = getPattern(regexToReject);
    }
    
    private Pattern getPattern(String regex) {
         return regex == null ? null : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    @Override
    public boolean accept(String imageSrc) {

        if(baseUrl != null && !imageSrc.startsWith(baseUrl)) {
            return false;
        }
        
        if(toReject != null && toReject.matcher(imageSrc).find()) {
            return false;
        }
        
        if(toAccept != null && !toAccept.matcher(imageSrc).find()) {
            return false;
        }
        
        return true;
    }
}
