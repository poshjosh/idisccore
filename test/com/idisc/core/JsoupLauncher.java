package com.idisc.core;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
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

    public static void main(String [] args) throws Exception {
        
        TimeZone timeZone = TimeZone.getTimeZone("GMT+0500");
        
System.out.println(timeZone);
System.out.println(TimeUnit.MILLISECONDS.toHours(timeZone.getOffset(System.currentTimeMillis())));
        
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+0100");
        
System.out.println(df.toLocalizedPattern());

        df.applyLocalizedPattern("yyyy-MM-dd'T'HH:mm:ss+0500");
        
System.out.println(df.toPattern());        
        
        Date date0 = df.parse("2016-09-14T14:32:00+0100");
        
System.out.println("Time: "+date0.getTime());        
        
        Date date1 = df.parse("2016-09-14T14:32:00+0500");
        
System.out.println("Time: "+date1.getTime());        
if(true) {
    return;
}        
        
        String code = "&#8211;";
System.out.println(code);

        code = com.bc.util.StringEscapeUtils.unescapeHtml(code);
System.out.println(code);   

if(true) {
    return;
}
        
        ArrayList list = new ArrayList();
        
        
        Connection jcon = Jsoup.connect("");
        
        Document jdoc = jcon.get();
        
        Elements jelems = jdoc.getElementsByTag("DIV");
    }
}
