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

package com.idisc.core.timespent;

import com.bc.timespent.TimeSpentImpl;
import com.bc.timespent.TimeSpent;
import com.idisc.core.extraction.TaskLifeCycleListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 9:31:28 PM
 */
public class TimeSpentListenerImpl implements TaskLifeCycleListener {

    private transient static final Logger LOG = Logger.getLogger(TimeSpentListenerImpl.class.getName());

    private long startTimeMillis;

    private long unitStartTimeMillis;

    private TimeSpent timeSpentOnCompletion;

    private final List<Long> timeSpentList;
    
    private final Consumer<TimeSpent> timeSpentConsumer;
    
    public TimeSpentListenerImpl(Consumer<TimeSpent> timeSpentConsumer) {
        this.timeSpentConsumer = Objects.requireNonNull(timeSpentConsumer);
        this.timeSpentList = new LinkedList<>();
    }
    
    @Override
    public void onUnitStarted(Object arg) {
        this.unitStartTimeMillis = System.currentTimeMillis();
    }
    
    @Override
    public void onUnitCompleted(Object arg) {
        final long timeSpent = System.currentTimeMillis() - this.unitStartTimeMillis;
        LOG.finer(() -> "Unit completed. Time spent: " + timeSpent);
        this.timeSpentList.add(timeSpent);
    }

    @Override
    public void onStarted(Object arg) {
        this.startTimeMillis = System.currentTimeMillis();
    }
    
    @Override
    public void onException(Object arg, Exception e) { 
        this.finish(arg);
    }
    
    @Override
    public void onSuccess(Object arg) { 
        this.finish(arg);
    }
    
    public void finish(Object arg) {
        this.timeSpentOnCompletion = this.initTimeSpent(null);
        LOG.info(() -> "Completed.\n" + arg + '\n' + String.valueOf(this.timeSpentOnCompletion));
        if(this.timeSpentOnCompletion != null) {
            this.timeSpentConsumer.accept(timeSpentOnCompletion);
        }
    }
    
    public Optional<TimeSpent> getTimeSpent() {
        final TimeSpent timeSpent = timeSpentOnCompletion != null ? 
                this.timeSpentOnCompletion : this.initTimeSpent(null);
        return Optional.ofNullable(timeSpent);
    }

    public TimeSpent initTimeSpent(TimeSpent outputIfNone) {
        final long [] timeSpentArray = this.getTimeSpentArray();
        final long totalTimeSpentMillis = System.currentTimeMillis() - startTimeMillis;
        return timeSpentArray.length == 0 ? outputIfNone : new TimeSpentImpl(
                startTimeMillis, totalTimeSpentMillis, timeSpentArray);
    }

    public long [] getTimeSpentArray() {
        final int len = this.timeSpentList.size();
        final long [] timeSpentArray = new long[len];
        int i = 0;
        for(Long aveTimeTillScrap : this.timeSpentList) {
            timeSpentArray[i++] = aveTimeTillScrap;
        }
        return timeSpentArray;
    }
}
