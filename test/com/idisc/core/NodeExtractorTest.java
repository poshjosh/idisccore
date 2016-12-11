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

package com.idisc.core;

import com.bc.webdatex.URLParser;
import java.util.logging.Level;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 19, 2016 3:57:26 PM
 */
public class NodeExtractorTest extends ExtractionTestBase {
    
    private final TagLocatorTest tagLocatorTest;

    public NodeExtractorTest() throws Exception {
        super(Level.FINE);
        this.tagLocatorTest = new TagLocatorTest();
    }

    @Test
    public void test() {
        
        String [] sites = {NAIJ, PUNCH_NG, CHANNELSTV_HEADLINES};
        sites = new String[]{"sunnewsonline"};
        sites = new String[]{CHANNELSTV_HEADLINES};
        
        for(String site : sites) {
            
            final String url = this.getUrl(site);
            
System.out.println("------------------------ "+site+" ------------------------"); 
System.out.println("----------------------------------------------------------");
            for(int i = 0; i< 10; i++) {
                
                final String key = "targetNode"+i;
System.out.println("------------------------ "+key+" ------------------------"); 
                
                try{
                    this.testNodeExtractor(site, key, url);
                }catch(Exception e) {
                    System.err.println(e);
                }
                try{
                    this.tagLocatorTest.testTagLocator(site, key, url);
                }catch(Exception e) {
                    System.err.println(e);
                }
            }
            
        }
    }

    public StringBuilder testNodeExtractor(String site, String key) throws ParserException {
        
        final String url = this.getUrl(site);

        return this.testNodeExtractor(site, key, url);
    }
    
    public StringBuilder testNodeExtractor(String site, String key, String url) throws ParserException {
        
        com.bc.webdatex.extractor.node.NodeExtractor nodeExtractor = this.getNodeExtractor(site, key);
        
        final URLParser urlParser = new URLParser();
        
        NodeList nodes = urlParser.parse(url);

        nodes.visitAllNodesWith(nodeExtractor);
        
        final StringBuilder extract = nodeExtractor.getExtract();
            
log(this.getClass(), "EXTRACT:\n" + extract);

        return extract;
    }
}
