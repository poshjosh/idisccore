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

import com.bc.webdatex.extractors.Extractor;
import com.bc.webdatex.extractors.TextParser;
import com.idisc.pu.entities.Feed;
import java.util.Comparator;
import java.util.Date;
import org.htmlparser.NodeFilter;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 2:53:43 PM
 */
public interface FeedCreationContext {
    
    public static FeedCreationContextBuilder builder() {
        return new FeedCreationContextBuilderImpl();
    }

    Date getDate(Feed feed);

    Date getDate(String dateStr, Date outputIfNone);
    
    FeedCreationConfig getConfig();

    NodeFilter getImagesFilter();

    int getRecommendedSize(String columnName);

    boolean hasEnoughData(Feed feed);

    TextParser<String> getTitleFromUrlExtractor();
    
    String toString(Feed feed);

    String getValue(String col, boolean plainTextOnly, String... values);

    Extractor<String, Date> getDateExtractor();

    Extractor<String, Date> getDateFromUrlExtractor();

    Comparator<String> getImageSizeComparator();

    String format(String col, String val, boolean plainTextOnly);
}
