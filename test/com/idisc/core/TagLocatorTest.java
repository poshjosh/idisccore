package com.idisc.core;

import com.bc.io.CharFileIO;
import com.bc.json.config.JsonConfig;
import com.scrapper.CapturerApp;
import com.scrapper.Crawler;
import com.scrapper.config.Config;
import com.scrapper.config.ScrapperConfigFactory;
import com.scrapper.context.CapturerContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import org.htmlparser.Tag;
import org.htmlparser.util.NodeList;
import org.htmlparser.visitors.NodeVisitor;
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
public class TagLocatorTest {

    @Test
    public void testTransverseFilter() throws Exception {

        String site = "bellanaija";
        String sampleUrl = "https://www.bellanaija.com/2015/06/09/designer-deola-sagoe-is-a-vision-in-gold-in-her-own-piece/";
        
        site = "leadership.ng";
        sampleUrl = "http://leadership.ng/news/439529/how-aig-mbu-efcc-detained-tortured-me-for-80-day";

        this.testTransverseFilter(site, 0, sampleUrl);
    }
    
    
    public void testTransverseFilter(String site, int index, String sampleUrl) throws Exception {
        
        final String key = "targetNode" + index;
        
        if(!IdiscApp.getInstance().isInitialized()) {
            IdiscApp.getInstance().init();
        }
        
        CapturerApp app = IdiscApp.getInstance().getCapturerApp();
        
//        app.init(false);
        
//        app.getLoggerManager().setLogLevel(Level.FINER, true);
        
        ScrapperConfigFactory factory = app.getConfigFactory();
        
        CapturerContext ctx = factory.getContext(site);
        
        JsonConfig config = ctx.getConfig();

        Crawler c = new Crawler(ctx);
        
        Object [] transverse = this.get(config, key);

        final com.bc.webdatex.locator.TagLocator tagLocator = new com.bc.webdatex.locator.TagLocator();
        
log("Scrapping: "+sampleUrl);

////////////////////////////////////////////////////
//Parser p = c.getParser();
//p.setInputHTML(this.getHtml());
//NodeList nodes = p.parse(null);
//////////////////////////////////////////////////
        NodeList nodes = c.parse(sampleUrl);
//System.out.println("------------------------------------------------------------");        
//System.out.println(nodes.toHtml(false));
//System.out.println("------------------------------------------------------------");

        NodeVisitor nv = new NodeVisitor(){
            @Override
            public void visitTag(Tag tag) {
System.out.println("Visiting: "+tag.toTagHtml());
                boolean b4 = tagLocator.isFoundTarget();
                tagLocator.visitTag(tag); 
                boolean accept = tagLocator.isProceed();
                if(!b4 && tagLocator.isFoundTarget()) {
log(key+", Found: "+tag.toTagHtml()+", Children: "+(tag.getChildren()==null?null:tag.getChildren().size()));                    
System.out.println("===========================================================");
System.out.println(tag.toHtml(false));
System.out.println("===========================================================");
                }
            }
        };
        
        tagLocator.setId(key);
        tagLocator.setPath(transverse);
        
        nodes.visitAllNodesWith(tagLocator);
    }

    private Object [] get(JsonConfig props, String propertyKey) {
        // DIV,,,DIV,,,DIV,,,SPAN P SPAN
        // Parents = DIV,DIV,DIV
        // Siblings = SPAN,P,SPAN (of which target is the last SPAN)
        //
        Object [] pathToValue = {propertyKey, Config.Extractor.transverse};
        
        if(pathToValue == null || pathToValue.length == 0) {
            throw new NullPointerException();
        }

        Object [] expectedAll = props.getArray(pathToValue);
        
        if(expectedAll == null) {
            throw new IllegalArgumentException("["+Arrays.toString(pathToValue) + "=null]");
        }
        
        return expectedAll;
    }
    
    private void log(String msg) {
System.out.println(this.getClass().getName()+" "+msg);        
    }
    
    private String getHtml() {
        try{
            File file = Paths.get(System.getProperty("user.home"), "Desktop", "leadership.html").toFile();
            CharSequence cs = new CharFileIO().readChars(file);
            return cs.toString();
        }catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
