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

package com.idisc.core.extraction.scrapconfig;

import com.bc.timespent.TimeSpent;
import com.bc.timespent.TimeSpentAverage;
import com.bc.timespent.TimeSpentCummulativeAverage;
import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;
import com.idisc.core.timespent.TimeSpentStore;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 6:27:00 PM
 */
public class ScrapConfigFactoryImpl implements ScrapConfigFactory, Serializable {

    private transient static final Logger LOG = Logger.getLogger(ScrapConfigFactoryImpl.class.getName());
    
    private final Configuration config;
    
    private final TimeSpentStore timeSpentService;
    
    public ScrapConfigFactoryImpl(
            Configuration config, TimeSpentStore timeSpentService) {
        this.config = Objects.requireNonNull(config);
        this.timeSpentService = Objects.requireNonNull(timeSpentService);
    }
    
    @Override
    public ScrapConfig get(String type) {
        switch(type) {
            case ScrapConfig.TYPE_WEB:
                return new ScrapWebConfig(config);
            case ScrapConfig.TYPE_RSS:
                return new ScrapRssConfig(config);
            default:
                throw new IllegalArgumentException("Unexpected type: " + type);
        }
    }

    @Override
    public ScrapConfig get(String type, String name) {
        final ScrapConfigBean bean = (ScrapConfigBean)this.get(type);
        final String fileName = this.buildFileName(type, name);
        final TimeSpent timeSpent = this.loadOrDefault(fileName, null);
        LOG.finer(() -> "Loaded: " + fileName + "\n" + timeSpent);
        if(timeSpent != null) {
            final long averageTimeSpent = timeSpent.getTotalTimeSpent() / timeSpent.getSubunitCount();
            LOG.info(() -> "Updating timeout pers site to: " + averageTimeSpent + 
                    ' ' + bean.getTimeUnit().name() + ", from: " + fileName + '=' + timeSpent);
            bean.setTimeoutPerSite(bean.getTimeUnit().convert(averageTimeSpent, timeSpent.getTimeUnit()));
        }
        return bean;
    }

    @Override
    public boolean updateTimeSpent(String type, String name, TimeSpent timeSpent) {
        return this.updateTimeSpent(this.buildFileName(type, name), timeSpent);
    }
    
    public boolean updateTimeSpent(String path, TimeSpent timeSpent) {
        final TimeSpent toSave = this.cummulate(path, timeSpent);
        LOG.fine(() -> "Saving: " + toSave);
        try{
            timeSpentService.put(path, toSave);
            return true;
        }catch(Exception e) {
            LOG.log(Level.WARNING, timeSpentService.getClass().getName() + 
                    " encountered exception saving to: " + path + "\nObject: " + toSave, e);
            return false;
        }
    }

    public TimeSpent cummulate(String type, String name, TimeSpent timeSpent) {
        return this.cummulate(this.buildFileName(type, name), timeSpent);
    }
    
    public TimeSpent cummulate(String path, TimeSpent timeSpent) {
        final TimeSpent loaded = this.loadOrDefault(path, null);
        final TimeSpent output;
        if(loaded == null) {
            output = timeSpent;
        }else{
            final TimeSpentAverage timeSpentAverage = new TimeSpentCummulativeAverage();
            output = timeSpentAverage.get(timeSpent.getTimeUnit(), loaded, timeSpent);
        }
        LOG.finer(() -> "\nLHS: " + loaded + "\nRHS: " + timeSpent + "\nAVE: " + output);
        return output;
    }

    public TimeSpent loadOrDefault(String type, String name, TimeSpent outputIfNone) {
        return this.loadOrDefault(this.buildFileName(type, name), outputIfNone);
    }
    
    public TimeSpent loadOrDefault(String path, TimeSpent outputIfNone) {
        TimeSpent output;
        try{
            output = this.timeSpentService.getOrDefault(path, null);
        }catch(Exception e) {
            LOG.log(Level.WARNING, this.timeSpentService.getClass().getName() + 
                    " encountered exception loading: " + path, e);
            output = null;
        }
        return output == null ? outputIfNone : output;
    }
    
    private String buildFileName(String type, String name) {
        return "com.bc.timespent.TimeSpent_" + type + '_' + name + ".javaobject";
    }
}
