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

package com.idisc.core.extraction;

import com.bc.jpa.context.JpaContext;
import com.idisc.core.IdiscApp;
import com.idisc.core.extraction.web.WebFeedTaskProvider;
import com.idisc.core.functions.GetSubList;
import com.scrapper.config.ScrapperConfigFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 10:48:27 AM
 */
public class ExtractionContextForWebPages extends ExtractionContextImpl {

    public ExtractionContextForWebPages(IdiscApp app) {
        this(
                app.getJpaContext(), 
                app.getCapturerApp().getConfigFactory(), 
                app.getConfiguration()
        );
    }
    
    public ExtractionContextForWebPages(
        JpaContext jpaContext, ScrapperConfigFactory configFactory, Configuration config) {
        super(Collections.unmodifiableList(
                        new ArrayList(configFactory.getConfigNamesLessDefaultConfig())    
                ),
                new GetSubList(),
                new WebFeedTaskProvider(jpaContext, configFactory, config)
        );
    }

    public ExtractionContextForWebPages(
        Collection<String> names, JpaContext jpaContext, 
        ScrapperConfigFactory configFactory, Configuration config) {
        super(names,
                new GetSubList(),
                new WebFeedTaskProvider(jpaContext, configFactory, config)
        );
    }

    public ExtractionContextForWebPages(
            Collection<String> names, 
            BiFunction<Collection<String>, Integer, List<String>> getSubList, 
            Function<String, Runnable> taskProvider) {
        super(names, getSubList, taskProvider);
    }
}