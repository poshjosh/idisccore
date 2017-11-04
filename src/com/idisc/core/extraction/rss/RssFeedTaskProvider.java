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

package com.idisc.core.extraction.rss;

import com.bc.jpa.context.JpaContext;
import com.bc.task.StoppableTask;
import com.bc.util.XLogger;
import com.idisc.core.ConfigNames;
import com.idisc.core.FeedHandler;
import com.idisc.core.IdiscApp;
import com.idisc.core.InsertFeedToDatabase;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 30, 2017 7:46:17 PM
 */
public class RssFeedTaskProvider implements Function<String, Runnable> {
    
  private final boolean acceptDuplicateUrls;
  
  private final long timeout;
  
  private final TimeUnit timeunit;
  
  private final JpaContext jpaContext;
  
  private final Properties feedProperties;

    public RssFeedTaskProvider(IdiscApp app) {
      this(app.getJpaContext(), app.getConfiguration());
    }
  
    public RssFeedTaskProvider(
      JpaContext jpaContext, Configuration config) {
      this(
              jpaContext, 
              new RssMgr().getFeedNamesProperties(), 
              config.getLong(ConfigNames.RSS_TIMEOUT_PER_SITE_SECONDS, 90), 
              TimeUnit.SECONDS, 
              config.getBoolean(ConfigNames.WEB_ACCEPT_DUPLICATE_LINKS, false));  
    }
  
    public RssFeedTaskProvider(
      JpaContext jpaContext, Properties feedProperties,
      long timeout, TimeUnit timeunit, boolean acceptDuplicateUrls) {
        this.jpaContext = Objects.requireNonNull(jpaContext);
        this.feedProperties = Objects.requireNonNull(feedProperties);
        this.timeout = timeout;
        this.timeunit = Objects.requireNonNull(timeunit);
        this.acceptDuplicateUrls = acceptDuplicateUrls;
    }
  
    @Override
    public StoppableTask apply(final String feedName) {
    
        XLogger.getInstance().entering(this.getClass(), "createNewTask(String)", feedName);

        final FeedHandler feedHandler = new InsertFeedToDatabase(jpaContext);

        final StoppableTask task = new RssFeedDownloadTask(
                feedName, this.feedProperties.getProperty(feedName), 
                this.timeout, this.timeunit, 
                this.acceptDuplicateUrls, feedHandler);

        return task;
    }
}
