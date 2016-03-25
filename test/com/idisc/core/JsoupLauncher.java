package com.idisc.core;

import java.io.IOException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


/**
 * @(#)JsoupLauncher.java   09-Jun-2015 18:03:39
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
public class JsoupLauncher {

    public static void main(String [] args) throws IOException {
        
        Connection jcon = Jsoup.connect("");
        
        Document jdoc = jcon.get();
        
        Elements jelems = jdoc.getElementsByTag("DIV");
        
        
        
        
    }
}
