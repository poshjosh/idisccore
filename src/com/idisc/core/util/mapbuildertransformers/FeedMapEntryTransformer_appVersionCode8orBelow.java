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

import com.idisc.core.ConfigNames;
import com.idisc.core.IdiscApp;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Site_;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 2:47:43 PM
 */
public class FeedMapEntryTransformer_appVersionCode8orBelow extends FeedMapEntryTransformer {

    private final Site defaultSite; 
    
    public FeedMapEntryTransformer_appVersionCode8orBelow() { 
        final IdiscApp idiscApp = IdiscApp.getInstance();
        final Integer defaultSiteId = idiscApp.getConfiguration().getInteger(ConfigNames.DEFAULTSITE_ID, null);
        Objects.requireNonNull(defaultSiteId);
        defaultSite = idiscApp.getJpaContext().getDao(Site.class).findAndClose(Site.class, defaultSiteId);
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
    public Object transformValue(Feed entity, String oldKey, String newKey, Object value) {
        
        value = super.transformValue(entity, oldKey, newKey, value);
        
        if(Feed_.siteid.getName().equals(oldKey)) {
            
            // Original Format:        com.idisc.pu.entities.Site[ siteid=4 ]
            // Format: News Minute [4]
            // The siteId (i.e 4) is extracted
            Map map = new HashMap();
            Site site = entity.getSiteid() == null ? defaultSite : entity.getSiteid();
            final Integer siteId = site.getSiteid();
            final String siteName = site.getSite();
            final String siteText = siteName + " [" + siteId + ']';
            map.put(Site_.siteid.getName(), siteText);
            map.put(Site_.site.getName(), siteText);
            value = map;
        }
        
        return value;
    }
}
