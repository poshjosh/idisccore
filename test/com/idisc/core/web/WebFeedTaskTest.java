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
package com.idisc.core.web;

import com.bc.jpa.JpaContext;
import com.idisc.core.IdiscTestBase;
import com.scrapper.config.JsonConfigFactory;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class WebFeedTaskTest extends IdiscTestBase {
    
    public WebFeedTaskTest() throws Exception { 
        super(Level.FINE);
    }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    /**
     * Test of getTaskNames method, of class WebFeedTask.
     */
    @Test
    public void testGetTaskNames() {
        
        final boolean acceptDuplicateLinks = false;
        final int maxConcurrent = 3;
        final int maxFailsAllowed = 9;

        final long webTimeoutSeconds = 600;
        final long webTimeoutEachSeconds = 180;
        
        final JpaContext jpaContext = this.getIdiscApp().getJpaContext();
        
        final JsonConfigFactory configFactory = this.getCapturerApp().getConfigFactory();
        
        final WebFeedTask webFeedTask = new WebFeedTask(
                jpaContext, configFactory,
                webTimeoutSeconds, TimeUnit.SECONDS, 
                webTimeoutEachSeconds, TimeUnit.SECONDS, 
                maxConcurrent, maxFailsAllowed, acceptDuplicateLinks);

        final Map<String, Integer> webFeedsUpdateCounts = webFeedTask.call();
        
System.out.println("Downloaded " + (webFeedsUpdateCounts==null?null:webFeedsUpdateCounts.size()) + " feeds");
    }
}
