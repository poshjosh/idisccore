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

import com.bc.task.AbstractStoppableTask;
import com.bc.task.StoppableTask;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.Test;

/**
 * @author Josh
 */
public class SubmitTasksTest {
    
    private static final String [] taskNames = {"Task A", "Task B", "Task C", "Task D", "Task E"};
    private static final int factor = 5;
    private static final int maxConcurrent = 2;
    private static final int timeout = factor/2 * (taskNames.length / maxConcurrent) + (taskNames.length % maxConcurrent > 0 ? 1 : 0);
    private static final TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private static Map<String, StoppableTask> tasks = new LinkedHashMap();
    
    public SubmitTasksTest() { }
    
    /**
     * Test of call method, of class SubmitTasks.
     */
    @Test
    public void testCall() {
        System.out.println("call");
System.out.println("Tasks: "+tasks.size()+", max concurrent: "+maxConcurrent+", timeout: "+timeout+", time unit: "+timeoutUnit);        
        for(int i=0; i<5; i++) {
System.out.println("============================= ("+i+") =============================");            
            SubmitTasks<Object, Integer> instance = new SubmitTasksImpl();
final long tb4 = System.currentTimeMillis();
            final Integer result = instance.call();
System.out.println("Task-group("+i+"), time spent: "+(System.currentTimeMillis()-tb4));            
System.out.println("Results: "+result);
        }
    }
    
    public static class SubmitTasksImpl extends SubmitTasks {
        
        
        public static class TaskProvider implements Function<String, Runnable> {

            @Override
            public StoppableTask apply(String taskName) {
                final StoppableTask task = new AbstractStoppableTask<Long>() {
                    @Override
                    protected Long doCall() throws Exception {
                        final int sleeptimeSeconds = factor/5 > 0 ? factor/5 : 1;
                        final long startTime = System.currentTimeMillis(); 
                        final double duration = Math.random() * factor;
System.out.println("Started: "+taskName+", to last "+ ((int)(duration*1000)) +" milliseconds");                        
                        double timeLeft = duration;
                        while(timeLeft > 0) {
                            Thread.sleep(sleeptimeSeconds * 1000);
                            timeLeft -= sleeptimeSeconds;
                        }
System.out.println("Concluded: "+taskName+", time spent: "+(System.currentTimeMillis()-this.getStartTime())+" milliseconds");
                        final Long output = System.currentTimeMillis() - startTime;
                        return output;
                    }
                    @Override
                    public String getTaskName() {
                        return taskName;
                    }
                };
                return task;
            }
        }
        
        public SubmitTasksImpl() {
            this(timeout, timeoutUnit);
        }

        public SubmitTasksImpl(long timeout, TimeUnit timeUnit) {
            super(Arrays.asList(taskNames), new TaskProvider(), timeout, timeUnit, maxConcurrent);
            tasks.clear();
        }

        @Override
        public String getTaskName() {
            return this.getClass().getName();
        }
    }
}
