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

import com.idisc.core.ConfigNames;
import com.idisc.core.IdiscApp;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Site_;
import java.util.Map;
import java.util.Objects;
import javax.persistence.EntityManager;

/**
 * @deprecated
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2016 1:38:23 PM
 */
@Deprecated
public class EntityMapBuilder_appVersionCode8orBelow extends EntityMapBuilderDeprecated {

  private final Site defaultSite; 
          
  public EntityMapBuilder_appVersionCode8orBelow() { 
      this(false, 1000);
  }

  public EntityMapBuilder_appVersionCode8orBelow(boolean plainTextOnly, int maxTextLength) {
    super(plainTextOnly, maxTextLength);
    IdiscApp idiscApp = IdiscApp.getInstance();
    Integer defaultSiteId = IdiscApp.getInstance().getConfiguration().getInteger(ConfigNames.DEFAULTSITE_ID, null);
    Objects.requireNonNull(defaultSiteId);
    EntityManager em = idiscApp.getJpaContext().getEntityManager(Site.class);
    try{
      defaultSite = em.find(Site.class, defaultSiteId);
    }finally{
      em.close();
    }
  }

  @Override
  public Map toMap(Feed feed) {
// Original Format:        com.idisc.pu.entities.Site[ siteid=4 ]
// Format: News Minute [4]
// The siteId (i.e 4) is extracted
    Map map = super.toMap(feed);
    Site site = feed.getSiteid() == null ? defaultSite : feed.getSiteid();
    final Integer siteId = site.getSiteid();
    final String siteName = site.getSite();
    final String siteText = siteName + " [" + siteId + ']';
    map.put(Site_.siteid.getName(), siteText);
    map.put(Site_.site.getName(), siteText);
    return map;
  }
}
