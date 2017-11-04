/*
 * Copyright 2017 NUROX Ltd.
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

package com.idisc.core.extraction.web;

import com.bc.jpa.context.JpaContext;
import com.bc.json.config.JsonConfig;
import com.idisc.core.ConfigNames;
import com.idisc.core.FeedHandler;
import com.idisc.core.IdiscApp;
import com.idisc.core.InsertFeedToDatabase;
import com.scrapper.config.ScrapperConfigFactory;
import java.text.MessageFormat;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 30, 2017 3:38:45 PM
 */
public class WebFeedTaskProviderOld implements Function<String, Runnable> {

  private static final Logger logger = Logger.getLogger(WebFeedTaskProviderOld.class.getName());
    
  private final boolean acceptDuplicateUrls;
  
  private final long timeout;
  
  private final TimeUnit timeunit;
  
  private final int maxFailsAllowed;
  
  private final JpaContext jpaContext;
  
  private final ScrapperConfigFactory configFactory;

    public WebFeedTaskProviderOld(IdiscApp app) {
        this(
                app.getJpaContext(), 
                app.getCapturerApp().getConfigFactory(),
                app.getConfiguration()
        );
    }

  public WebFeedTaskProviderOld(
      JpaContext jpaContext, 
      ScrapperConfigFactory configFactory,
      Configuration config) {
      this(jpaContext, configFactory,
              config.getLong(ConfigNames.WEB_TIMEOUT_PER_SITE_SECONDS, 180),
              TimeUnit.SECONDS,
              config.getInt(ConfigNames.MAX_FAILS_ALLOWED, 9),
              config.getBoolean(ConfigNames.WEB_ACCEPT_DUPLICATE_LINKS, false)
      );
  }
    
  public WebFeedTaskProviderOld(
      JpaContext jpaContext, ScrapperConfigFactory configFactory,
      long timeout, TimeUnit timeunit, 
      int maxFailsAllowed, boolean acceptDuplicateUrls) {
    this.jpaContext = Objects.requireNonNull(jpaContext);
    this.configFactory = Objects.requireNonNull(configFactory);
    this.timeout = timeout;
    this.timeunit = Objects.requireNonNull(timeunit);
    this.maxFailsAllowed = maxFailsAllowed;
    this.acceptDuplicateUrls = acceptDuplicateUrls;
  }
  
  @Override
  public WebFeedCrawler apply(final String site) {
    
    final JsonConfig config = this.configFactory.getConfig(site);
    
    Objects.requireNonNull(config, JsonConfig.class.getSimpleName()+" for site: "+site+" is null");
    
    final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);
    
    final WebFeedCrawler crawler = new WebFeedCrawler(
            config, this.timeout, this.timeunit, this.maxFailsAllowed, 
            feedHandler, false, !acceptDuplicateUrls){
            @Override
            protected Integer doCall() {
//                System.out.println("On: " + new Date() + ", Executing task: " + getTaskName());
                final Integer output = super.doCall(); 
//                System.out.println("On: " + new Date() + ", DONE Executing task: " + getTaskName() + "\n" + this);
                return output;
            }
    };

    String url = config.getString(new Object[] { "url", "start" });
    
    crawler.setStartUrl(url);
    
    logger.finer(() -> MessageFormat.format("Created task {0} for {1}", 
        crawler.getClass().getName(), site));

    return crawler;
  }
}
