/*
 * Copyright 2018 NUROX Ltd.
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

package com.idisc.core.extraction;

import com.idisc.core.IdiscApp;
import com.idisc.core.functions.GetDefaultSite;
import com.idisc.core.util.TimeZones;
import com.idisc.pu.entities.Feed_;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Timezone;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 2:13:16 PM
 */
public class FeedCreationConfigImpl implements Serializable, FeedCreationConfig {

  private transient static final Logger LOG = Logger.getLogger(FeedCreationConfigImpl.class.getName());

  private final Map defaults;
  private final Site site;
  private final TimeZone inputTimeZone;
  private final TimeZone outputTimeZone;

  public FeedCreationConfigImpl(IdiscApp app, Map defaults){
    this(new GetDefaultSite().apply(app.getJpaContext(), app.getConfiguration()), defaults);
  }
  
  public FeedCreationConfigImpl(Site site, Map defaults){
    this.site = Objects.requireNonNull(site);
    this.defaults = Objects.requireNonNull(defaults);
    final Timezone timeZoneEntity = site.getTimezoneid();
    final TimeZones timeZones = new TimeZones();
    final String dbTimeZoneId = timeZones.getDatabaseTimeZoneId();
    final String inputTimeZoneId = timeZoneEntity == null ? dbTimeZoneId : timeZoneEntity.getTimezonename();
    inputTimeZone = TimeZone.getTimeZone(inputTimeZoneId);
    outputTimeZone = TimeZone.getTimeZone(dbTimeZoneId);
    if(LOG.isLoggable(Level.FINE)){
      LOG.log(Level.FINE, 
        "Site: {0}, In TimeZone: {1}, Out TimeZone: {2}", 
        new Object[]{ site.getSite(),  inputTimeZone.getID(),  outputTimeZone.getID()});
    }
  }  

    @Override
  public final TimeZone getOutputTimeZone() {
    return outputTimeZone;
  }

    @Override
  public final TimeZone getInputTimeZone() {
    return inputTimeZone;
  }
  
    @Override
  public final String getDefaultCategories() {
    final String cat = (String)defaults.get(Feed_.categories.getName());
    return cat;
  }
  
  @Override
  public final String getDefaultTitle() {
    final String cat = (String)defaults.get(Feed_.title.getName());
    return cat;
  }

  @Override
  public final Object getDefaultValue(String key) {
    final Object value = defaults.get(key);
    return value;
  }

  public final Map getDefaults() {
    return Collections.unmodifiableMap(defaults);
  }

  @Override
  public final Site getSite() {
    return site;
  }
}
