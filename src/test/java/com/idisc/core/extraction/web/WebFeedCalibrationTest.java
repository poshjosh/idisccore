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

package com.idisc.core.extraction.web;

import com.idisc.core.IdiscTestBase;
import com.idisc.core.SiteNames;
import com.idisc.core.SubmitTasks;
import com.idisc.core.extraction.scrapconfig.ScrapConfigBean;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactory;
import com.idisc.core.extraction.scrapconfig.ScrapConfigFactoryImpl;
import com.idisc.core.timespent.TimeSpentLocalDiscStore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.Test;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 11:18:25 AM
 */
public class WebFeedCalibrationTest extends IdiscTestBase {
    
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();
    
    private final Configuration config;
    
    private final ScrapConfigFactory scrapConfigFactory;
    
    private final ScrapConfigBean scrapConfig;

    private final WebFeedTaskProvider taskProvider;
    
    public WebFeedCalibrationTest() 
            throws IOException, MalformedURLException, ConfigurationException {
        config = this.loadConfiguration();
        scrapConfigFactory = this.getScrapConfigFactory(config);
        scrapConfig = this.getScrapConfig();
        taskProvider = new WebFeedTaskProvider(
                this.createJpaContext(), 
                this.getExtractionContextFactory(), 
                scrapConfigFactory
        ); 
        
    }
    
    private ScrapConfigFactory getScrapConfigFactory(Configuration config) {
        return new ScrapConfigFactoryImpl(config, new TimeSpentLocalDiscStore());
    }
    
    private ScrapConfigBean getScrapConfig() {
        final ScrapConfigBean output = new ScrapConfigBean();
        final int timeoutPerSiteSeconds = 60 * 5;
        output.setMaxConcurrentUnits(availableProcessors);
        output.setTimeUnit(TimeUnit.SECONDS);
//        output.setTimeout(???);
        output.setTimeoutPerSite(timeoutPerSiteSeconds);
        output.setScrapLimit(7);
        return output;
    }
    
    /**
     * Test of run method, of class WebFeedTask.
     */
    @Test
    public void testRun() {
        System.out.println("run");
        final List<String> names = this.getExtractionContextFactory().getConfigService().getConfigNamesLessDefaultConfig();
        System.out.println("Names: " + names);
        this.run(Collections.singletonList(SiteNames.BELLANAIJA));
//        this.run(names);
    }
    
    private void run(List<String> names) {
        
        System.out.println("Selected Names: " + names);

        scrapConfig.setMaxConcurrentUnits(names.size());
        
        final int factor = names.size() / availableProcessors < 1 ? 1 : names.size() / availableProcessors;
        scrapConfig.setTimeout(scrapConfig.getTimeoutPerSite() * factor);

        this.submitTasks(names);

        this.submitTasks(names);
    }

    private void submitTasks(List<String> names) {
        
        long tb4 = System.currentTimeMillis();
        long mb4 = com.bc.util.Util.availableMemory();
        
        new SubmitTasks<>(
                names, 
                taskProvider,
                scrapConfig.getTimeout(), 
                scrapConfig.getTimeUnit(), 
                scrapConfig.getMaxConcurrentUnits()
        ).call();
        
        System.out.println("Completed calibration tasks: " + names + 
                "\nConsumed time: " + (System.currentTimeMillis()-tb4)+
                ", memory: "+com.bc.util.Util.usedMemory(mb4));
    }
}
