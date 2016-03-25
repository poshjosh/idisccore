package com.idisc.core;

import com.bc.mailservice.DefaultMailConfig;
import com.bc.mailservice.XMLMailConfig;
import com.bc.util.XLogger;
import com.idisc.pu.IdiscControllerFactory;
import com.bc.jpa.ControllerFactory;
import com.bc.mailservice.MailConfig;
import com.scrapper.CapturerApp;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @(#)IdiscApp.java   16-Oct-2014 10:08:15
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.1
 * @since    0.1
 */
public class IdiscApp {
    
    private boolean initialized;
    
    private String persistenceFilename;
    
    private String scrapperPropertiesFilename;
    
    private XMLMailConfig mailConfig;
    
    private Configuration config;
    
    private static IdiscApp instance;
    
    protected IdiscApp() { 
        persistenceFilename = "META-INF/persistence.xml";
        scrapperPropertiesFilename = "META-INF/properties/idisccore_scrapper.properties";
    }
    
    public static IdiscApp getInstance() {
        if(instance == null) {
            instance = new IdiscApp();
        }
        return instance;
    }
    
    public static void setInstance(IdiscApp app) {
        instance = app;
    }
    
    public void init() throws Exception {
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        
        URL fileLoc = loader.getResource("META-INF/properties/idisc.properties");
        
        this.init(fileLoc);
    }

    public void init(URL fileLoc) throws Exception {
        
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        
        URL defaultFileLoc = loader.getResource("META-INF/properties/idiscdefaults.properties");
        
        this.init(defaultFileLoc, fileLoc);
    }
    
    public void init(URL defaultConfigFile, URL configFile) throws Exception {

XLogger.getInstance().setRootOnly(true);
XLogger.getInstance().setRootLoggerName(IdiscApp.class.getName());

        XLogger.getInstance().log(Level.INFO, "Initializing: {0}", this.getClass(), this.getClass().getName());
        
        if(configFile == null) {
            throw new NullPointerException();
        }

        // Enable list delimiter handling using a comma as delimiter character
        config = this.loadConfig(defaultConfigFile, configFile, ',');
        
        com.scrapper.AppProperties.load(this.scrapperPropertiesFilename);
        
        CapturerApp.getInstance().init(false);

        initialized = true;
XLogger.getInstance().log(Level.INFO, "Done initializing app", this.getClass());            
    }

    public Configuration loadConfig(
            URL defaultFileLocation, URL fileLocation, char listDelimiter) 
            throws ConfigurationException {
        
XLogger.getInstance().log(Level.INFO, 
"Loading properties configuration. List delimiter: {0}\nDefault file: {1}\nFile: {2}", 
this.getClass(), listDelimiter, defaultFileLocation, fileLocation);

        if(fileLocation == null) {
            throw new NullPointerException();
        }
        
        Configuration output; 
        
        if(defaultFileLocation != null) {
            
            CompositeConfiguration composite = new CompositeConfiguration();

            PropertiesConfiguration cfg = this.loadConfig(
                    fileLocation, listDelimiter);
            composite.addConfiguration(cfg, true);
            
            PropertiesConfiguration defaults = this.loadConfig(
                    defaultFileLocation, listDelimiter);
            composite.addConfiguration(defaults);
            
            output = composite;
            
        }else{
            
            output = this.loadConfig(fileLocation, listDelimiter);
        }
        
        return output;
    }
    
    public boolean saveConfig() {
        return this.saveConfig(config);
    }
    
    public boolean saveConfig(Configuration cfg) {
        
XLogger.getInstance().log(Level.FINER, "Saving {0}", this.getClass(), cfg);

        boolean saved = false;
        if(cfg  instanceof CompositeConfiguration) {
            CompositeConfiguration cc = ((CompositeConfiguration)cfg);
            Configuration imc = cc.getInMemoryConfiguration();
            if(imc != null) {
                this.saveConfig(imc);
                saved = true;
            }
        }else if(cfg instanceof AbstractFileConfiguration) {
            AbstractFileConfiguration afc = ((AbstractFileConfiguration)cfg);
            File file = afc.getFile();
            try{
                afc.save();
XLogger.getInstance().log(Level.FINE, "Saved "+cfg.getClass().getName()+" to "+file, this.getClass());
                return true;
            }catch(ConfigurationException e) {
                XLogger.getInstance().log(Level.WARNING, "Error saving "+cfg.getClass().getName()+" to "+file, this.getClass(), e);
                return false;
            }
        }
        return saved;
    }

    private PropertiesConfiguration loadConfig(
            URL fileLocation, char listDelimiter) 
            throws ConfigurationException {
        PropertiesConfiguration cfg = new PropertiesConfiguration();
        cfg.setListDelimiter(listDelimiter);
        cfg.setURL(fileLocation);
        cfg.load();
        return cfg;
    }
    
    public String getAbsolutePath(String relativePath) {
        return new File(relativePath).getAbsolutePath();
    }
    
    public MailConfig getMailConfig() {
        if(mailConfig == null) {
            String baseURL = this.getConfiguration().getString(AppProperties.BASE_URL);
            try{
                mailConfig = new DefaultMailConfig(new URL(baseURL));
            }catch(MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return mailConfig;
    }
    
    public Configuration getConfiguration() {
        return config;
    }
    
    private ControllerFactory _accessViaGetter_ControllerFactory;
    public ControllerFactory getControllerFactory() {
        if(_accessViaGetter_ControllerFactory == null) {
            try{
                _accessViaGetter_ControllerFactory = new IdiscControllerFactory(this.getPersistenceFilename(), null);
            }catch(IOException e) {
                throw new RuntimeException(e);
            }
        }
        return _accessViaGetter_ControllerFactory;
    }
    
    public boolean isInitialized() {
        return initialized;
    }

    public CapturerApp getCapturerApp() {
        return CapturerApp.getInstance();
    }

    public String getPersistenceFilename() {
        return persistenceFilename;
    }

    public void setPersistenceFilename(String persistenceFilename) {
        if(persistenceFilename == null) {
            throw new NullPointerException();
        }
        this.persistenceFilename = persistenceFilename;
        this._accessViaGetter_ControllerFactory = null;
    }

    public String getScrapperPropertiesFilename() {
        return scrapperPropertiesFilename;
    }

    public void setScrapperPropertiesFilename(String scrapperPropertiesFilename) {
        this.scrapperPropertiesFilename = scrapperPropertiesFilename;
    }
}
