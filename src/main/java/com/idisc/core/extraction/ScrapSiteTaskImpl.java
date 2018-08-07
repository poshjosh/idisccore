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

import com.bc.task.AbstractStoppableTask;
import com.idisc.core.FeedHandler;
import com.idisc.core.extraction.scrapconfig.ScrapConfig;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 12:56:23 PM
 * @param <SOURCE_DATA_TYPE>
 */
public class ScrapSiteTaskImpl<SOURCE_DATA_TYPE> 
        extends AbstractStoppableTask<Integer> 
        implements ScrapSiteTask<SOURCE_DATA_TYPE, Integer> {

    private transient static final Logger LOG = Logger.getLogger(ScrapSiteTaskImpl.class.getName());
    
    private final Iterator<SOURCE_DATA_TYPE> inputSource;

    private final Predicate<SOURCE_DATA_TYPE> scrapTest;
    
    private final FeedCreatorFromContext<SOURCE_DATA_TYPE> feedCreator;
    
    private final FeedHandler feedHandler;

    private final List<TaskLifeCycleListener> listeners;
    
    private final int limit;
    
    private final long timeoutMillis;
    
    private final int maxFailsAllowed;
    
    private int attempted;
    
    private int rejectCount;
    
    private int successCount;
    
    private int failCount;

    public ScrapSiteTaskImpl(
            Iterator<SOURCE_DATA_TYPE> inputSource,
            Predicate<SOURCE_DATA_TYPE> scrapTest, 
            FeedCreatorFromContext<SOURCE_DATA_TYPE> feedCreator,
            FeedHandler feedHandler,
            ScrapConfig scrapConfig) {
        this(inputSource, 
                scrapTest, 
                feedCreator, 
                feedHandler,
                scrapConfig.getScrapLimit(), 
                scrapConfig.getTimeoutPerSite(),
                scrapConfig.getTimeUnit(),
                scrapConfig.getMaxFailsAllowed());
    }
    
    public ScrapSiteTaskImpl(
            Iterator<SOURCE_DATA_TYPE> inputSource,
            Predicate<SOURCE_DATA_TYPE> scrapTest, 
            FeedCreatorFromContext<SOURCE_DATA_TYPE> feedCreator,
            FeedHandler feedHandler,
            int limit,
            long timeout,
            TimeUnit timeUnit,
            int maxFailsAllowed) {
        this.inputSource = Objects.requireNonNull(inputSource);
        this.scrapTest = Objects.requireNonNull(scrapTest);
        this.feedCreator = Objects.requireNonNull(feedCreator);
        this.feedHandler = Objects.requireNonNull(feedHandler);
        this.listeners = new ArrayList();
        this.limit = limit;
        this.timeoutMillis = timeUnit.toMillis(timeout);
        this.maxFailsAllowed = maxFailsAllowed;
        LOG.log(Level.FINE, "Done creating: {0}", this);
    }

    @Override
    public void addLifeCycleListener(TaskLifeCycleListener listener) {
        if(this.isStarted()) {
            throw new IllegalStateException("Cannot add listener after task has started");
        }
        this.listeners.add(listener);
    }

    @Override
    public List<TaskLifeCycleListener> getLifeCycleListeners() {
        return Collections.unmodifiableList(listeners);
    }

    @Override
    public FeedCreatorFromContext<SOURCE_DATA_TYPE> getFeedCreator() {
        return this.feedCreator;
    }
    
    @Override
    public Integer doCall() {
        
        try{
            
            this.attempted = 0;
            this.rejectCount = 0;
            this.successCount = 0;
            this.failCount = 0;

            final Date daysAgo = new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7));

            for(TaskLifeCycleListener listener : listeners) {
                listener.onStarted(this);
            }
            
            final Date NO_DATE = new Date(0);

            while(true) {
                
                for(TaskLifeCycleListener listener : listeners) {
                    listener.onUnitStarted(this);
                }
                
                if(!inputSource.hasNext()) {
                    break;
                }

                if(this.isStopRequested() || this.isTimedout() ||
                        !this.isWithinLimit() || !this.isWithinFailLimit()) {
                    
                    LOG.fine(() -> "Stop requested: " + this.isStopRequested() + 
                            ", timedout: " + this.isTimedout() +
                            ", exceeded limit: " + !this.isWithinLimit() + 
                            ", exceeded fail limit: " + !this.isWithinFailLimit());
                    
                    break;
                }

                ++attempted;

                final SOURCE_DATA_TYPE data = inputSource.next();
                
                if(data == null) {
                    ++failCount;
                    continue;
                }
                
                LOG.log(Level.FINER, "PageNodes: {0}", data);
                
                final boolean scrapp = scrapTest.test(data);
                
                LOG.finest(() -> "Scrapp: " + scrapp + ", data: " + data);

                if(!scrapp) {
                    ++rejectCount;
                    LOG.log(Level.FINER, "Not qualified for scrapp, data: {0}", data);
                    continue;
                }
                
                final Feed feed = this.feedCreator.createFeed(data);
                
                if(feed == null) {
                    ++rejectCount;
                    LOG.fine(() -> "Failed to create feed from: " + data);
                    continue;
                }
                
                if(feed.getFeeddate() == null && feed.getDatecreated() == null && feed.getTimemodified() == null) {
                    feed.setFeeddate(NO_DATE);
                }
                
                final FeedCreationContext feedCreationContext = this.feedCreator.getContext();
                
                if(!feedCreationContext.hasEnoughData(feed)) {
                    ++rejectCount;
                    LOG.fine(() -> "Not enough data: " + data + 
                            "\nFeed: " + feedCreationContext.toString(feed));
                    continue;
                }

                final Date date = feedCreationContext.getDate(feed);
                
                if(date == null) {
                    ++rejectCount;
                    LOG.fine(() -> "No date for data: " + data + 
                            "\nFeed: " + feedCreationContext.toString(feed));
                    continue;
                }

                if(date.before(daysAgo)) {
                    ++rejectCount;
                    LOG.fine(() -> "Is expired. Date: " + date + ", earliest date: " + daysAgo +
                            "\nFeed: " + feedCreationContext.toString(feed));
                    continue;
                }

                synchronized (feedHandler) {

                    LOG.log(Level.FINER, () -> "Inserting into database: " + feedCreationContext.toString(feed));

                    for(TaskLifeCycleListener listener : listeners) {
                        listener.onUnitCompleted(this);
                    }
                    
                    final boolean insertedIntoDatabase = feedHandler.process(feed);

                    if(insertedIntoDatabase) {
                        ++successCount;
                    }else{
                        LOG.fine(() -> "Failed to add Web Feed. " + feedCreationContext.toString(feed));
                    }
                }
            }
            
            for(TaskLifeCycleListener listener : listeners) {
                listener.onSuccess(this);
            }
            
        }catch(RuntimeException e) {
            
            LOG.log(Level.WARNING, "Unexpected Runtime Exception", e);
            
            for(TaskLifeCycleListener listener : listeners) {
                listener.onException(this, e);
            }
            
        }finally{
            
            LOG.fine(() -> "DONE:: " + this.toString());
        }
        
        return this.successCount;
    }

    @Override
    public String getTaskName() {
        return this.getClass().getSimpleName() + '[' + feedCreator.getContext().getConfig().getSite().getSite() + ']';
    }
    
    public boolean isWithinLimit() {
        return limit <= 0 || successCount < limit;
    }
    
    public boolean isWithinFailLimit() {
        return maxFailsAllowed <= 0 || failCount < maxFailsAllowed;
    }
    
    public boolean isTimedout() {
        return timeoutMillis > 0 && this.isTimedout(timeoutMillis);
    }

    public int getAttempted() {
        return attempted;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    @Override
    public String toString() {
        return super.toString() + 
                "\n{Description: " + this.getTaskName() + '\n' + inputSource + 
                "\nTimeout: " + TimeUnit.MILLISECONDS.toSeconds(timeoutMillis) + 
                " seconds, attempted: " + attempted + ", rejected: " + rejectCount +
                ", Succeeded: " + successCount + '/' + limit +
                ", Failed: " + failCount + '/' + maxFailsAllowed + '}';
    }
}
