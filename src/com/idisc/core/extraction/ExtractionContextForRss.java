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
import com.idisc.core.extraction.rss.RssFeedTaskProvider;
import com.idisc.core.extraction.rss.RssMgr;
import com.idisc.core.functions.GetSubList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 11:39:30 AM
 */
public class ExtractionContextForRss extends ExtractionContextImpl {

    public ExtractionContextForRss(IdiscApp app) {
        this(
                app.getJpaContext(), 
                app.getConfiguration()
        );
    }
    
    public ExtractionContextForRss(
        JpaContext jpaContext, Configuration config) {
        this(new RssMgr().getFeedNamesProperties().stringPropertyNames(),
                jpaContext,
                config
        );
    }

    public ExtractionContextForRss(
        Collection<String> names, JpaContext jpaContext, Configuration config) {
        super(names,
                new GetSubList(),
                new RssFeedTaskProvider(jpaContext, config)
        );
    }

    public ExtractionContextForRss(
            Collection<String> names, 
            BiFunction<Collection<String>, Integer, List<String>> getSubList, 
            Function<String, Runnable> taskProvider) {
        super(names, getSubList, taskProvider);
    }
}