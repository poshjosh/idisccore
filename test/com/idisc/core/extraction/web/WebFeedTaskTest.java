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
package com.idisc.core.extraction.web;

import com.idisc.core.IdiscApp;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.SubmitTasks;
import com.idisc.core.extraction.ExtractionContext;
import com.idisc.core.extraction.ExtractionContextForWebPages;
import com.idisc.core.functions.GetSubList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.junit.Test;

/**
 * @author Josh
 */
public class WebFeedTaskTest extends IdiscTestBase {
    
    private final boolean acceptDuplicates = false;
    private final int timeoutPerSiteSeconds = 360;
    private final int maxFails = 9;
    private final int maxConcurrent = 2;
    private final int sitesPerBatch = 4;
    private final int timeoutSeconds = 720; //timeoutPerSite * sitesPerBatch / maxConcurrent;

    private final ExtractionContext webContext;
    
    private final IdiscApp app;
    
    public WebFeedTaskTest() throws Exception { 
        
        super(Level.FINE);
        
        this.app = this.getIdiscApp();
        
        webContext = new ExtractionContextForWebPages(
                app.getExtractionFactory().getNames("web"), 
                new GetSubList(),
                new WebFeedTaskProvider(
                        app.getJpaContext(), 
                        app.getCapturerApp().getConfigFactory(), 
                        0.1f,
                        timeoutPerSiteSeconds, TimeUnit.SECONDS,
                        maxFails, acceptDuplicates
                )
        );
    }
    
    /**
     * Test of call method, of class WebFeedTask.
     */
    @Test
    public void testCallMultipleTimes() {
        this.call();
    }
    
    private void call() {
        
        long tb4 = System.currentTimeMillis();
        long mb4 = com.bc.util.Util.availableMemory();
        
        System.out.println("  All names: " + webContext.getNames());
        
//        webContext.getNextNames(sitesPerBatch);
        
        final List<String> webNames = new ArrayList(webContext.getNextNames(sitesPerBatch));
        
        Collections.sort(webNames, app.getExtractionFactory().getNamesComparator("web"));
        
        System.out.println("Sorted names: " + webNames);
        
        new SubmitTasks(
                webNames, 
                webContext.getTaskProvider(),
                timeoutSeconds,
                TimeUnit.SECONDS,
                maxConcurrent
        ).call();
        
        System.out.println("Completed tasks: " + webNames + 
                "\n--------------------------------------\n"+
                "Consumed time: " + (System.currentTimeMillis()-tb4)+
                ", memory: "+com.bc.util.Util.usedMemory(mb4));
    }
}
