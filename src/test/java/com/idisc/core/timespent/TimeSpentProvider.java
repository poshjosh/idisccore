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

import com.bc.timespent.TimeSpent;
import com.bc.timespent.TimeSpentImpl;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 31, 2018 11:39:11 AM
 */
public class TimeSpentProvider {

    private final long totalTime;

    public TimeSpentProvider() {
        this(10_000);
    }
    
    public TimeSpentProvider(long totalTime) {
        this.totalTime = totalTime;
    }

    public TimeSpent getInstance(boolean ascending) {
        return this.getInstance(this.getStartTime(), ascending);
    }    
    
    public TimeSpent getInstance(long startTime, boolean ascending) {
        final long [] timeSpentArr = ascending ?
                new long[]{0, 1000, 2000, 3000, 4000} : 
                new long[]{4000, 3000, 2000, 1000, 0};
        TimeSpent timeSpent;
        try{
            timeSpent = new TimeSpentImpl(
                    null, null, startTime, totalTime, null
            );
            fail(TimeSpentImpl.class.getName() + " constructor should fail on NULL args");
        }catch(NullPointerException e) {
            timeSpent = new TimeSpentImpl(startTime, totalTime, timeSpentArr
            );
        }
        assertNotNull(timeSpent);
        return timeSpent;
    }
    
    public TimeSpent getEmptyInstance() {
        return this.getEmptyInstance(System.currentTimeMillis());
    }
    
    public TimeSpent getEmptyInstance(long startTime) {
        return new TimeSpentImpl(startTime, 0, new long[0]);
    }
    
    public TimeSpent [] getMulitple() {
        final TimeSpent ascen = this.getInstance(true);
        final TimeSpent desce = this.getInstance(false);
        final TimeSpent empty = this.getEmptyInstance();
        final TimeSpent custo = this.getInstance(System.currentTimeMillis() - 100_000, true);
        return new TimeSpent[]{ascen, desce, empty, custo};
    }

    public TimeSpent [] getMulitpleUniform(int count, boolean ascending, boolean empty) {
        return this.getMulitpleUniform(this.getStartTime(), count, ascending, empty);
    }
    
    public TimeSpent [] getMulitpleUniform(long startTime, int count, boolean ascending, boolean empty) {
        final TimeSpent [] output = new TimeSpent[count];
        for(int i=0; i<output.length; i++) {
            output[i] = empty ? this.getEmptyInstance(startTime) : this.getInstance(startTime, ascending);
        }
        return output;
    }
    
    public long getStartTime() {
        return System.currentTimeMillis() - totalTime;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
