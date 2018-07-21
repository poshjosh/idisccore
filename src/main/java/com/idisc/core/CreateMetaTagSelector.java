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

package com.idisc.core;

import com.bc.meta.ArticleMetaNames;
import com.bc.meta.impl.ArticleMetaNameIsMultiValue;
import com.bc.meta.selector.Selector;
import com.bc.meta.selector.SelectorBuilder;
import com.bc.meta.selector.util.SampleConfigPaths;
import com.idisc.core.extraction.metaselector.AttributeContextHtmlparser;
import com.idisc.core.extraction.metaselector.JsonParserImpl;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 21, 2018 7:16:08 PM
 */
public class CreateMetaTagSelector implements Serializable, 
        Function<Collection<String>, Selector<Node>>,
        Supplier<Selector<Node>> {

    private transient static final Logger LOG = Logger.getLogger(CreateMetaTagSelector.class.getName());

    private static class StreamProvider implements Function<String, InputStream> {
        @Override
        public InputStream apply(String location) {
            
            try{
                
                final ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
                InputStream in = cl.getResourceAsStream(location);
                
                if(LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "ClassLoader: {0}, location: {1}, input stream: {2}",
                            new Object[]{cl, location, in});
                }
                
                if(in == null) {
                    try{
                        in = new FileInputStream(location);
                        if(LOG.isLoggable(Level.FINE)) {
                            LOG.log(Level.FINE, "Location: {0}, file input stream: {1}",
                                    new Object[]{location, in});
                        }
                    }catch(IOException e) {
                        try{
                            in = new URL(location).openStream();
                            if(LOG.isLoggable(Level.FINE)) {
                                LOG.log(Level.FINE, "Location: {0}, url input stream: {1}",
                                        new Object[]{location, in});
                            }
                        }catch(MalformedURLException mue) {
                            throw e;
                        }
                    }
                }

                return in;

            }catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Selector<Node> get() {
        return this.apply(SampleConfigPaths.APP_ARTICLE_LIST);
    }

    @Override
    public Selector<Node> apply(Collection<String> paths) {
        try{
            return this.execute(paths);
        }catch(IOException | java.text.ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Selector<Node> execute(Collection<String> paths) 
          throws IOException, java.text.ParseException {
        final AttributeContextHtmlparser ac = new AttributeContextHtmlparser(false);
        final SelectorBuilder<Node, String, Object> builder = Selector.builder();
        return builder.filter()
                .attributeContext(ac)
                .configFilePaths(paths)
                .streamProvider(new CreateMetaTagSelector.StreamProvider())
                .jsonParser(new JsonParserImpl())
                .propertyNames(ArticleMetaNames.values())
                .back()
                .multiValueTest(new ArticleMetaNameIsMultiValue())
                .nodeValueExtractor(ac)
                .build();
    }
}
