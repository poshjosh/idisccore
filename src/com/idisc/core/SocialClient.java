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

package com.idisc.core;

import com.bc.oauth.OAuthProperties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 31, 2016 12:19:45 PM
 * @param <E>
 */
public abstract class SocialClient<E extends OAuthProperties> {
    
    private final E oAuthProperties;
  
    public SocialClient(E oAuthProperties) {
      
        this.oAuthProperties = oAuthProperties;
    }
    
    public abstract boolean publish(String tweet, Set<String> att);
  
    protected InputStream getInputStream(String path) throws IOException {
        
        try {
            URL url = new URL(path);
            return url.openStream();
        } catch (MalformedURLException ignored) {}
        
        return new FileInputStream(path);
    }

    public final E getoAuthProperties() {
        return oAuthProperties;
    }
}
