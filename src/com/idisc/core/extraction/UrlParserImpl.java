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

import com.bc.dom.HtmlDocument;
import com.bc.dom.HtmlDocumentImpl;
import com.bc.webcrawler.UrlParser;
import com.bc.webdatex.ParserImpl;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 5:00:44 PM
 */
public class UrlParserImpl implements UrlParser<HtmlDocument> {

    private static final Logger logger = Logger.getLogger(UrlParserImpl.class.getName());

    private final ParserImpl parser;
    
    public UrlParserImpl() {
        this.parser = new ParserImpl();
    }

    @Override
    public HtmlDocument parse(String url) throws IOException {
        try{
            final NodeList nodeList = parser.parse(url);
            return new HtmlDocumentImpl(url, nodeList);
        }catch(ParserException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Map<String, String> getCookies() {
        final List<String> cookies = parser.getCookies();
        final Map<String, String> output;
        if(cookies == null || cookies.isEmpty()) {
            output = Collections.EMPTY_MAP;
        }else{
            output = new HashMap(cookies.size(), 1.0f);
            for(String cookie : cookies) {
                final String [] parts = cookie.trim().split("=");
                if(parts.length == 2) {
                    output.put(parts[0], parts[1]);
                }else{
                    logger.warning(() -> "Failed to parse into NAME-VALUE pair, cookie: " + cookie);
                }
            }
        }
        return output;
    }
}
