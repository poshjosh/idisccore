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
import com.idisc.core.comparator.site.SiteComparatorScrappcount;
import com.idisc.pu.entities.Feed;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Josh
 */
public class ConcurrentTaskListTest {
    
    private static final String [] taskNames = {"Task A", "Task B", "Task C", "Task D", "Task E"};
    private static final int factor = 5;
    private static final int maxConcurrent = 2;
    private static final int timeout = factor/2 * (taskNames.length / maxConcurrent) + (taskNames.length % maxConcurrent > 0 ? 1 : 0);
    private static final TimeUnit timeoutUnit = TimeUnit.SECONDS;
    private static Map<String, StoppableTask> tasks = new LinkedHashMap();
    
    public ConcurrentTaskListTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    /**
     * Test of getTaskNames method, of class ConcurrentTaskList.
     */
    @Test
    public void testGetTaskNames() {
        System.out.println("getTaskNames");
        ConcurrentTaskList instance = new ConcurrentTaskListImpl();
        List<String> expResult = new ArrayList(tasks.keySet());
        List<String> result = instance.getTaskNames();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMaxConcurrent method, of class ConcurrentTaskList.
     */
    @Test
    public void testGetMaxConcurrent() {
        System.out.println("getMaxConcurrent");
        ConcurrentTaskList instance = new ConcurrentTaskListImpl();
        int expResult = maxConcurrent;
        int result = instance.getMaxConcurrent();
        assertEquals(expResult, result);
    }

    /**
     * Test of getResult method, of class ConcurrentTaskList.
     */
    @Test
    public void testGetResult() {
        System.out.println("getResult");
        ConcurrentTaskList instance = new ConcurrentTaskListImpl();
        Collection<Feed> result = instance.getResult();
        Assert.assertNotNull(result);
    }

    /**
     * Test of getTimeout method, of class ConcurrentTaskList.
     */
    @Test
    public void testGetTimeout() {
        System.out.println("getTimeout");
        ConcurrentTaskList instance = new ConcurrentTaskListImpl();
        long expResult = timeout;
        long result = instance.getTimeout();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTimeoutUnit method, of class ConcurrentTaskList.
     */
    @Test
    public void testGetTimeoutUnit() {
        System.out.println("getTimeoutUnit");
        ConcurrentTaskList instance = new ConcurrentTaskListImpl();
        TimeUnit expResult = timeoutUnit;
        TimeUnit result = instance.getTimeoutUnit();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of call method, of class ConcurrentTaskList.
     */
    @Test
    public void testCall() {
        System.out.println("call");
System.out.println("Tasks: "+tasks.size()+", max concurrent: "+maxConcurrent+", timeout: "+timeout+", time unit: "+timeoutUnit);        
        for(int i=0; i<5; i++) {
System.out.println("============================= ("+i+") =============================");            
            ConcurrentTaskList<String> instance = new ConcurrentTaskListImpl();
final long tb4 = System.currentTimeMillis();
            Collection<String> result = instance.call();
System.out.println("Task-group("+i+"), time spent: "+(System.currentTimeMillis()-tb4));            
System.out.println("Results: "+result);
        }
    }
    
    public static class ConcurrentTaskListImpl extends ConcurrentTaskList<String> {
        
        private static final SiteComparatorScrappcount sorter = new SiteComparatorScrappcount();

        public ConcurrentTaskListImpl() {
            this(timeout, timeoutUnit, maxConcurrent);
        }

        public ConcurrentTaskListImpl(long timeout, TimeUnit timeUnit, int maxConcurrent) {
            super(timeout, timeUnit, maxConcurrent);
            final int sleeptimeSeconds = factor/5 > 0 ? factor/5 : 1;
            tasks.clear();
            final Collection<String> result = this.getResult();
            for(final String taskName:taskNames) {
                StoppableTask task = new AbstractStoppableTask<String>() {
                    @Override
                    protected String doCall() throws Exception {
                        final double duration = Math.random() * factor;
System.out.println("Started: "+taskName+", to last "+ ((int)(duration*1000)) +" milliseconds");                        
                        double timeLeft = duration;
                        while(timeLeft > 0) {
                            Thread.sleep(sleeptimeSeconds * 1000);
                            timeLeft -= sleeptimeSeconds;
                            final int count = sorter.incrementAndGet(taskName, 1);
                            synchronized(result) {
                                result.add(taskName+'_'+count);
                            }
                        }
System.out.println("Concluded: "+taskName+", time spent: "+(System.currentTimeMillis()-this.getStartTime())+" milliseconds");
                        return taskName;
                    }
                    @Override
                    public String getTaskName() {
                        return taskName;
                    }
                };
                tasks.put(taskName, task);
            }
        }

        @Override
        public List<String> sort(List<String> values) {

            List<String> output = sorter.sort(values);
    
System.out.println("Input: "+values+"\nOutput: "+output);

          return output;
        }
        
        @Override
        public String getTaskName() {
            return this.getClass().getName();
        }
        
        @Override
        public StoppableTask createNewTask(String taskname) {
            return tasks.get(taskname);
        }

        @Override
        public List<String> getTaskNames() {
            return new ArrayList<>(tasks.keySet());
        }
    }
    
}
