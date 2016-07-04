package com.idisc.core;

import com.idisc.core.web.WebFeedCreator;


/**
 * @(#)Temp.java   29-Nov-2014 10:18:29
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
public class Temp {

    public static void main(String [] args) {
        try{
            String a = "<span>      by .... abokina @ zo </span";
            String b = "<span by nony boy ...<span>";
            String c = "By the     .   guys at something";
            WebFeedCreator fc = new WebFeedCreator();
            System.out.println("A = " + fc.format(a, "null", 15, true));    
            System.out.println("B = " + fc.format(b, "null", 15, true));    
            System.out.println("C = " + fc.format(c, "null", 15, true));    
        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
}
