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

package com.idisc.core.extraction;

import com.bc.jpa.context.PersistenceUnitContext;
import com.bc.jpa.metadata.PersistenceUnitMetaData;
import com.bc.webdatex.extractors.Extractor;
import com.bc.webdatex.extractors.TextParser;
import com.idisc.pu.entities.Site;
import java.util.Comparator;
import java.util.Date;
import java.util.function.BiFunction;
import org.htmlparser.NodeFilter;
import com.bc.webdatex.context.ExtractionConfig;
import com.idisc.pu.entities.Sitetype;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 12:49:36 AM
 */
public interface FeedCreationContextBuilder {

    FeedCreationContext build();

    FeedCreationContextBuilder columnDisplaySizes(int[] columnDisplaySizes);

    FeedCreationContextBuilder columnNames(String[] columnNames);

    FeedCreationContextBuilder dateExtractor(Extractor<String, Date> dateExtractor);

    FeedCreationContextBuilder dateFromUrlExtractor(Extractor<String, Date> dateFromUrlExtractor);

    FeedCreationContextBuilder datePatterns(String[] datePatterns);

    FeedCreationContextBuilder feedCreationConfig(FeedCreationConfig feedCreationConfig);

    FeedCreationContextBuilder feedCreationConfig(Site site, ExtractionConfig config);

    FeedCreationContextBuilder imageSizeComparator(Comparator<String> imageSizeComparator);

    FeedCreationContextBuilder imagesFilter(ExtractionConfig config);

    FeedCreationContextBuilder imagesFilter(NodeFilter imagesFilter);

    FeedCreationContextBuilder imagesFilter(String url);

    FeedCreationContextBuilder persistenceMetaData(PersistenceUnitMetaData metaData);

    FeedCreationContextBuilder plainTextExtractor(BiFunction<String, String, String> extractor);

    FeedCreationContextBuilder titleFromUrlExtractor(TextParser<String> titleFromUrlExtractor);

    FeedCreationContextBuilder urlDatePatterns(String[] urlDatePatterns);

    FeedCreationContextBuilder with(
            PersistenceUnitContext puContext, Sitetype siteType, ExtractionConfig config);
        
    FeedCreationContextBuilder with(
            PersistenceUnitContext puContext, String siteType, ExtractionConfig config);
        
    FeedCreationContextBuilder with(Site site, ExtractionConfig config);
}
