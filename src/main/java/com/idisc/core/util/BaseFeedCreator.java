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

import java.util.logging.Logger;
import com.idisc.core.ConfigNames;
import com.idisc.core.IdiscApp;
import com.idisc.pu.SiteDao;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.idisc.pu.entities.Timezone;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 12, 2016 3:12:52 PM
 */
public class BaseFeedCreator {

  private static final Logger LOG = Logger.getLogger(BaseFeedCreator.class.getName());

  private final String defaultCategories;
  private final Site site;
  private final TimeZone inputTimeZone;
  private final TimeZone outputTimeZone;

  public BaseFeedCreator(String defaultCategories){
    this(IdiscApp.getInstance().getConfiguration().getInteger(ConfigNames.DEFAULTSITE_ID, null), defaultCategories);
  }
  
  public BaseFeedCreator(Integer siteid, String defaultCategories){
    this(IdiscApp.getInstance().getJpaContext().getEntityController(Site.class, Integer.class).find(siteid), defaultCategories);
  }
  
  public BaseFeedCreator(String sitename, Sitetype sitetype, String defaultCategories){
    this(new SiteDao(IdiscApp.getInstance().getJpaContext()).from(sitename, sitetype, true), defaultCategories);
  }
  
  public BaseFeedCreator(Site site, String defaultCategories){
    this.site = Objects.requireNonNull(site);
    this.defaultCategories = defaultCategories;
    final Timezone timeZoneEntity = site.getTimezoneid();
    final TimeZones timeZones = new TimeZones();
    final String dbTimeZoneId = timeZones.getDatabaseTimeZoneId();
    final String inputTimeZoneId = timeZoneEntity == null ? dbTimeZoneId : timeZoneEntity.getTimezonename();
    inputTimeZone = TimeZone.getTimeZone(inputTimeZoneId);
    outputTimeZone = TimeZone.getTimeZone(dbTimeZoneId);
    if(LOG.isLoggable(Level.FINE)){
      LOG.log(Level.FINE, 
        "Site: {0}, In TimeZone: {1}, Out TimeZone: {2}", new Object[]{ site.getSite(),  inputTimeZone.getID(),  outputTimeZone.getID()});
    }
  }  

  public void updateFeed(Feed feed, Integer feedid, String title, String contents) {
    this.updateFeed(feed, feedid, site.getSite(), new Date(), title, contents, this.defaultCategories);
  }
  
  public void updateFeed(Feed feed, Integer feedid, 
          String author, Date date, String title, String contents, Object category) {
    
    Objects.requireNonNull(feedid);  
    Objects.requireNonNull(contents); 
    
    String categories = category == null ? defaultCategories : category.toString();
    feed.setFeedid(feedid);
    feed.setAuthor(author);
    feed.setCategories(categories);
    feed.setContent(contents);
    feed.setDatecreated(date);
    feed.setFeeddate(date);
    feed.setKeywords(categories);
      
    feed.setSiteid(site);
    feed.setTitle(title);
  }
  
  public final TimeZone getOutputTimeZone() {
    return outputTimeZone;
  }

  public final TimeZone getInputTimeZone() {
    return inputTimeZone;
  }
  
  public final String getDefaultCategories() {
    return defaultCategories;
  }

  public final Site getSite() {
    return site;
  }
}
