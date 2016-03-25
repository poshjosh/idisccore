package com.idisc.core;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;


/**
 * @(#)BaseTest.java   29-Nov-2014 06:52:18
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
public class BaseTest {

    @Test
    public void testConfigSubset() throws SQLException {
        
        try{
            
            final char listDelimiter = ',';
            final URL defaultFile = getUrl("META-INF/properties/idiscdefaults.properties");
            final URL file = getUrl("META-INF/properties/idisc.properties");

            IdiscApp app = IdiscApp.getInstance();
            
            Configuration defaults = app.loadConfig(defaultFile, file, listDelimiter);
            
            Configuration subset = defaults.subset("twitter");
            
//System.out.println(subset.toString());            

//            subset.setProperty("jesus", "isTheLionOfJudah");
//            defaults.setProperty("nawa", "oooooooooooooooooooooooooooooh");
            
System.out.println("Jesus: "+subset.getProperty("jesus"));
System.out.println("Nawa: "+defaults.getProperty("nawa"));

// Does crazy things            
//            app.saveConfig(defaults);

            List scopes = subset.getList("scope");
System.out.println(scopes);
if(true) {
    return;
}
            
            subset = defaults.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));
            
            Configuration props = app.loadConfig(null, file, listDelimiter);
            
            ((PropertiesConfiguration)defaults).copy(props);
            
            subset = defaults.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));

            Configuration cc = app.loadConfig(defaultFile, file, listDelimiter);
            
            subset = cc.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));
            
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    public void testOther() {
        try{
            
            PropertiesConfiguration config = new PropertiesConfiguration();

            config.setListDelimiter(',');

            final String basePath = "META-INF/properties";

            config.setBasePath(basePath);

            config.setFileName("/idiscdefaults.properties");

            config.load();

            Configuration subset = config.subset("twitter");

            List props = subset.getList("scope");
System.out.println(props);
if(true) {
    return;
}

            final int timeout = 1000;
            final int count = 10;
            
            Runnable rA = new Runnable() {
                @Override
                public synchronized void run() {
                    for(int i=0; i<count; i++) {
System.out.println("A: --- "+i);                        
                        try{
                            this.wait(timeout);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }finally{
                            this.notifyAll();
                        }
                    }
                }
            };
            
            Runnable rB = new Runnable() {
                @Override
                public synchronized void run() {
                    for(int i=0; i<count; i++) {
System.out.println("B: --- "+i);                        
                        try{
                            this.wait(timeout);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }finally{
                            this.notifyAll();
                        }
                    }
                }
            };
            
            FutureTask tA = new FutureTask(rA, rA);
            FutureTask tB = new FutureTask(rB, rB);
            
            ExecutorService execsvc = Executors.newCachedThreadPool();
            
            execsvc.submit(tB);
            execsvc.submit(tA);

System.out.println(tA.getClass()+"#get() returned: "+tA.get(3, TimeUnit.SECONDS));
            
        }catch(Throwable t) {
            t.printStackTrace();
        }
    }
    
    private URL getUrl(String subpath) throws IOException {
        URL output = null;
        Enumeration<URL> en = Thread.currentThread().getContextClassLoader().getResources(subpath);
        while(en.hasMoreElements()) {
            URL url = en.nextElement();
            if(accept(url)) {
                output = url;
                break;
            }
        }
        return output;
    }
    
    private boolean accept(URL url) {
        return true;
//        return !url.toExternalForm().contains("build");
    }
}
