package com.idisc.core;

import com.bc.htmlparser.ParseJob;
import com.bc.io.CharFileIO;
import com.bc.net.ConnectionManager;
import com.bc.process.ProcessManager;
import com.bc.util.SecurityTool;
import com.bc.util.XLogger;
import com.idisc.pu.References;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Gender;
import com.idisc.core.web.NewsCrawler;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.fk.EnumReferences;
import com.scrapper.util.EscapeChars;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


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
            String unexcaped = "'s";
            String excaped = "&#039;s";
            char chc = '\u0039';
System.out.println(unexcaped+" = "+EscapeChars.forHTML(unexcaped));            
System.out.println(excaped+" = "+StringUtil.unescapeHtml3(excaped));            
System.out.println(excaped+" = "+Character.toString(chc));            
if(true) {
    return;
}            
            

            BaseTest bt = new BaseTest();
            bt.testConfigSubset();
if(true) {
    return;
}            
            
            ConnectionManager cm = new ConnectionManager();
            Map params = new HashMap();
            params.put("installationkey", UUID.nameUUIDFromBytes("posh.bc@gmail.com".getBytes()));
            params.put("extractedemails", "{\"posh.bc@gmail.com\":\"1kjvdul-\"}");
            Date date = new Date();
            params.put("firstinstallationdate", date.getTime());
            params.put("lastinstallationdate", date.getTime());
            InputStream in = cm.getInputStreamForUrlEncodedForm(new URL("http://localhost:8080/idisc/addextractedemails"), params, "UTF-8", true);
            CharSequence cs = new CharFileIO().readChars(in);
System.out.println(cs);
if(true) {
    return;
}            
            
            final ScheduledExecutorService svc = Executors.newSingleThreadScheduledExecutor();
            Runnable command = new Runnable() {
                public synchronized void run(){

                    try{

                        for(int i=0; i<3; i++) {

                            this.wait(5000);

System.out.println("Index: "+i);
                        }
                    }catch(InterruptedException e) {
                        XLogger.getInstance().log(Level.WARNING, "Send mail task interrupted", this.getClass(), e);
                        // Preserve interrupted status
                        Thread.currentThread().interrupt();
                    }finally{
                        this.notifyAll();
                        ProcessManager.shutdownAndAwaitTermination(svc, 1, TimeUnit.SECONDS);
                    }
                }
            };
System.out.println("Scheduling");            
            svc.schedule(command, 3, TimeUnit.SECONDS);
System.out.println("Done scheduling");            
if(true) {
System.out.println("Returning");    
    return;
}            
            
            String a = "<span>      by .... abokina @ zo </span";
            String b = "<span by nony boy ...<span>";
            String c = "By the     .   guys at something";
if(true) {
    ParseJob parseJob = new ParseJob();
    FeedCreator fc = new FeedCreator();
System.out.println("A = " + fc.format(a, "null", 15, parseJob));    
System.out.println("B = " + fc.format(b, "null", 15, parseJob));    
System.out.println("C = " + fc.format(c, "null", 15, parseJob));    
    return;
}            
            
            String s = "chinomsoikwuagwu@yahoo.com";
            UUID uid = UUID.nameUUIDFromBytes(s.getBytes());
System.out.println(uid.toString());

            StringBuilder builder = new StringBuilder();
            
            for(int i=0; i<s.length(); i++) {
                char ch = s.charAt(i);
                int n = Character.getNumericValue(ch);
                builder.append(Math.abs(n));
            }
            builder.setLength(16);
            long l = Long.parseLong(builder.toString());
System.out.println("Long: "+l);
System.out.println(" Hex:"+Long.toHexString(l));

            if(true) {
                return;
            }
            
            SecurityTool sy = new SecurityTool("AES", "AcIcvwW2MU4sJkvBx103m6gKsePm");
            
if(true) {
System.out.println(sy.encrypt("chinomsoikwuagwu"));    
    return;
}            
            
            IdiscApp app = IdiscApp.getInstance();
            
            app.init();
            
            final ControllerFactory factory = app.getControllerFactory();
            
            EntityController<Feed, ?> ec = factory.getEntityController(Feed.class);
            
            ec.find();
            
            EnumReferences refs = factory.getEnumReferences();
            
            Gender gender = (Gender)refs.getEntity(References.gender.Female);
            
        }catch(Throwable t) {
            
            t.printStackTrace();
        }
    }
}
