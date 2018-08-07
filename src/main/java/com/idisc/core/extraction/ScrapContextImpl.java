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

import com.bc.jpa.context.PersistenceUnitContext;
import com.idisc.core.comparator.site.SitenameComparatorLastFeeddate;
import com.idisc.core.extraction.scrapconfig.ScrapConfig;
import com.idisc.core.functions.GetSubList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 11:12:26 AM
 * @param <SOURCE_DATA_TYPE>
 * @param <TASK_RESULT>
 */
public class ScrapContextImpl<SOURCE_DATA_TYPE, TASK_RESULT> 
        implements ScrapContext<SOURCE_DATA_TYPE, TASK_RESULT> {

    private transient static final Logger LOG = Logger.getLogger(ScrapContextImpl.class.getName());

    private final Collection<String> names;
    
    private final BiFunction<Collection<String>, Integer, List<String>> getSubList;
    
    private final Comparator<String> namesComparator;
    
    private final ScrapConfig config;
    
    private final Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT>> taskProvider;
    
    public ScrapContextImpl(
        Collection<String> names, 
        PersistenceUnitContext puContext, 
        ScrapConfig config,
        Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT>> taskProvider) {
        this(names,
                new GetSubList(),
                new SitenameComparatorLastFeeddate(puContext),
                config,
                taskProvider);
    }
    
    public ScrapContextImpl(
        Collection<String> names, 
        BiFunction<Collection<String>, Integer, List<String>> getSubList,
        Comparator<String> namesComparator,
        ScrapConfig config,
        Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT>> taskProvider) {
        
        this.names = Collections.unmodifiableCollection(names);
        
        this.getSubList = Objects.requireNonNull(getSubList);
        
        this.namesComparator = Objects.requireNonNull(namesComparator);
        
        this.config = config;
        
        this.taskProvider = Objects.requireNonNull(taskProvider);
    }
    
    @Override
    public Collection<String> getNames() {
        return this.names;
    }

    @Override
    public List<String> getNextNames(int size) {

        final List<String> nextNames = new ArrayList(this.getSubList.apply(this.names, size));
        
        LOG.finer(() -> "SubList provider: " + this.getSubList + 
                "\nAll names: " + this.names + "\nSubNames: " + nextNames);

        Collections.sort(nextNames, namesComparator);
        
        LOG.finer(() -> "After sort, subNames: " + nextNames);

        return Collections.unmodifiableList(nextNames);
    }

    @Override
    public ScrapConfig getConfig() {
        return config;
    }

    @Override
    public Function<String, ScrapSiteTask<SOURCE_DATA_TYPE, TASK_RESULT>> getTaskProvider() {
        return taskProvider;
    }
}
