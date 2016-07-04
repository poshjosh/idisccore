package com.idisc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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
public class ConfigurationTest extends IdiscTestBase {

    public ConfigurationTest() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{ 
    }

    @BeforeClass
    public static void setUpClass() { }
    @AfterClass
    public static void tearDownClass() { }
    @Before
    public void setUp() { }
    @After
    public void tearDown() { }
    
    @Test
    public void testConfigurationSubset() throws SQLException {
        
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
            
            subset = defaults.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));
            
            Configuration props = app.loadConfig(null, file, listDelimiter);
            
            ((PropertiesConfiguration)defaults).copy(props);
            
            subset = defaults.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));

            Configuration cc = app.loadConfig(defaultFile, file, listDelimiter);
            
            subset = cc.subset("nigerian_newsmedia");
            
System.out.println(subset.getProperty("Vanguard Nigeria"));
            
        }catch(IOException | ConfigurationException t) {
            
            t.printStackTrace();
        }
    }
    
    @Test
    public void testConfiguration() {
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
