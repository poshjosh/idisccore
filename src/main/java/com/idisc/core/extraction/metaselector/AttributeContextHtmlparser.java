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

package com.idisc.core.extraction.metaselector;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Predicate;
import org.htmlparser.Node;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.HasAttributeRegexFilter;
import com.bc.meta.selector.AttributeTestProvider;
import java.util.function.BiFunction;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 15, 2018 10:39:50 AM
 */
public class AttributeContextHtmlparser implements AttributeTestProvider<Node>, BiFunction<String, Node, String> {
    
    private final boolean useCache;
    
    private final Map<String, Predicate<Node>> testCache;

    public AttributeContextHtmlparser(boolean useCache) {
        this.useCache = useCache;
        this.testCache = !useCache ? Collections.EMPTY_MAP : 
                Collections.synchronizedMap(new WeakHashMap<>());
    }

    @Override
    public Predicate<Node> getAttributeTest(String attributeName, String attributeValue) {
        return this.getAttributeTest(attributeName, attributeValue, false);
    }

    @Override
    public Predicate<Node> getAttributeRegexTest(String attributeName, String attributeValue) {
        return this.getAttributeTest(attributeName, attributeValue, true);
    }

    public Predicate<Node> getAttributeTest(String attributeName, String attributeValue, boolean regex) {
        Predicate<Node> test;
        final String key = !useCache ? "" : this.buildKey(attributeName, attributeValue);
        synchronized(testCache) {
            test = testCache.get(key);
            if(test == null) {
                test = this.createAttributeTest(attributeName, attributeValue, regex);
                if(useCache) {
                    testCache.put(key, test);
                }
            }
        }
        return test;
    }

    public Predicate<Node> createAttributeTest(String attributeName, String attributeValue, boolean regex) {
        final Predicate<Node> test = !regex ? new HasAttributeFilter(attributeName, attributeValue) :
                new HasAttributeRegexFilter(attributeName, attributeValue);
        return test;
    }

    @Override
    public String apply(String attributeName, Node node) {
        final String value;
        if(node instanceof Tag) {
            value = ((Tag)node).getAttributeValue("content");
        }else{
            value = null;
        }
        return value;
    }
    
    public String buildKey(String attributeName, String attributeValue) {
        return attributeName + '=' + attributeValue;
    }
}
