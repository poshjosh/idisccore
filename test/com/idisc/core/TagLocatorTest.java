package com.idisc.core;

import com.bc.webdatex.locator.impl.TagLocatorImpl;
import com.bc.webdatex.URLParser;
import java.util.logging.Level;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.junit.Test;


/**
 * @(#)TestTransverseFilter.java   09-Jun-2015 21:22:11
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class TagLocatorTest extends ExtractionTestBase {

    public TagLocatorTest() throws Exception {
        super(Level.FINE);
    }

    @Test
    public void test() throws Exception {
        this.testTagLocator(NAIJ, "targetNode6");
    }
    
    public Tag testTagLocator(String site, String key) throws ParserException {
        
        final String url = this.getUrl(site);
        
        return this.testTagLocator(site, key, url);
    }
    
    public Tag testTagLocator(String site, String key, String url) throws ParserException {
        
        URLParser parser = new URLParser();
        
        final String [] path = this.getPath(site, key);
        
//this.log(this.getClass(), "Transverse: "+Arrays.toString(path));        

        final TagLocatorImpl tagLocator = new TagLocatorTraced(key, path);
        
        NodeList nodes = parser.parse(url);

//this.log(this.getClass(), "Nodes found: "+(nodes==null?null:nodes.size()));

        nodes.visitAllNodesWith(tagLocator);
        
        Tag target = tagLocator.getTarget();
log(this.getClass(), "Target:\n"+(target==null?null:target.toHtml(false)));
        return target;
    }
}
