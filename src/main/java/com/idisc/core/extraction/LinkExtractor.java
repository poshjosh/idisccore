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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.dom.HtmlDocument;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.FrameTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 6:11:21 PM
 */
public class LinkExtractor implements Function<HtmlDocument, Set<String>> {

    private static final Logger logger = Logger.getLogger(LinkExtractor.class.getName());

    private final NodeFilter linkNodeFilter;

    public LinkExtractor() {
        this(new OrFilter(new TagNameFilter("A"), new TagNameFilter("FRAME")));
    }

    public LinkExtractor(NodeFilter linkNodeFilter) {
        this.linkNodeFilter = Objects.requireNonNull(linkNodeFilter);
    }
    
    @Override
    public Set<String> apply(HtmlDocument nodeList) {
        
        final NodeList linkNodes = nodeList.extractAllNodesThatMatch(linkNodeFilter, true);
        
        final Set<String> links = new HashSet(linkNodes.size());
        
        for(Node node : linkNodes) {
            final String link;
            if(node instanceof LinkTag) {
                link = ((LinkTag)node).getLink();
            }else if(node instanceof FrameTag) {
                link = ((FrameTag)node).getFrameLocation();
            }else{
                link = null;
            }
            if(link != null) {
                links.add(link);
            }
        }
        
        final String url = nodeList.isEmpty() ? null : nodeList.get(0).getPage().getUrl();
        
        logger.finer(() -> "  URL: " + url + ", Links collected: " + links.size());
        logger.finest(() -> "  URL: " + url + ", Links collected: \n" + toString().replace(", ", "\n"));
        
        return links;
    }
}
