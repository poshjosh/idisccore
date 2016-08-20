/*
 * Copyright 2016 NUROX Ltd.
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

package com.idisc.core.web;

import com.bc.webdatex.locator.TagLocator;
import com.scrapper.URLParser;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Tag;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 11, 2016 10:34:59 PM
 */
public class UrlNodeExtractor {

    private final URLParser urlParser;
    
    private final TagLocator tagLocator;
    
    public UrlNodeExtractor(String id) {
        this.urlParser = new URLParser();
        this.tagLocator = new TagLocator();
        this.tagLocator.setId(id);
    }

    public NodeList extract(String url, String nodeId, Tag outputIfNone) throws ParserException {
        NodeFilter filter = new HasAttributeFilter("id", nodeId);
        Parser parser = urlParser.getParser();
        parser.setURL(url);
        return parser.parse(filter); 
    }
    
    public Tag extract(String url, Object [] path, Tag outputIfNone) throws ParserException {
        
        NodeList nodes = urlParser.parse(url); 
        
        tagLocator.setPath(path);
        
        nodes.visitAllNodesWith(tagLocator);
        
        Tag tag = tagLocator.getTarget();
        
        return tag == null ? outputIfNone : tag;
    }
}
