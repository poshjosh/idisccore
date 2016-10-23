package com.idisc.core;

import com.bc.util.XLogger;
import com.scrapper.AppProperties;
import com.scrapper.CapturerApp;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.logging.Level;
import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import com.bc.jpa.JpaContext;
import com.bc.sql.MySQLDateTimePatterns;
import com.idisc.pu.IdiscJpaContext;

public class IdiscApp {
    
  private boolean initialized;
  private String persistenceFilename;
  private String scrapperPropertiesFilename;
  private Configuration config;
  private static IdiscApp instance;
  private JpaContext jpaContext;
  
  protected IdiscApp(){
    this.persistenceFilename = "META-INF/persistence.xml";
    this.scrapperPropertiesFilename = "META-INF/properties/idisccore_scrapper.properties";
  }
  
  public static IdiscApp getInstance() {
    if (instance == null) {
      instance = new IdiscApp();
    }
    return instance;
  }
  
  public static void setInstance(IdiscApp app) {
    instance = app;
  }
  
  public void init()
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException {
      
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    URL defaultFileLoc = loader.getResource("META-INF/properties/idiscdefaults.properties");
    
    URL fileLoc = loader.getResource("META-INF/properties/idisc.properties");
    
    init(defaultFileLoc, fileLoc);
    
    this.initialized = true;
  }
  
  public void init(URL propertiesFile)
    throws ConfigurationException, IOException, IllegalAccessException, InterruptedException, InvocationTargetException
  {
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    URL defaultFileLoc = loader.getResource("META-INF/properties/idiscdefaults.properties");
    
    init(defaultFileLoc, propertiesFile);
    
    this.initialized = true;
  }

  private void init(URL defaultConfigFile, URL configFile)
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException, UnsupportedOperationException {
      
    if(this.isInitialized()) {
        throw new UnsupportedOperationException("App is already initialized!");
    }  
    
    XLogger.getInstance().log(Level.INFO, "Initializing: {0}", getClass(), getClass().getName());
    
    if (configFile == null) {
      throw new NullPointerException();
    }

    this.config = loadConfig(defaultConfigFile, configFile, ',');
    
    AppProperties.load(this.scrapperPropertiesFilename);
    
    this.jpaContext = this.initJpaContext(persistenceFilename);
    
    CapturerApp.getInstance().init(false);
    
    this.initialized = true;
    
    XLogger.getInstance().log(Level.INFO, "Done initializing app", getClass());
  }
  
  public JpaContext initJpaContext(String persistenceFilename) throws IOException {
      
      return new IdiscJpaContext(persistenceFilename, new MySQLDateTimePatterns());
  }

  public Configuration loadConfig(
          URL defaultFileLocation, URL fileLocation, char listDelimiter)
          throws ConfigurationException {
      
    XLogger.getInstance().log(Level.INFO, "Loading properties configuration. List delimiter: {0}\nDefault file: {1}\nFile: {2}", getClass(), Character.valueOf(listDelimiter), defaultFileLocation, fileLocation);

    if (fileLocation == null) {
      throw new NullPointerException();
    }
    
    Configuration output;
    if (defaultFileLocation != null)
    {
      CompositeConfiguration composite = new CompositeConfiguration();
      
      PropertiesConfiguration cfg = loadConfig(fileLocation, listDelimiter);
      
      composite.addConfiguration(cfg, true);
      
      PropertiesConfiguration defaults = loadConfig(defaultFileLocation, listDelimiter);
      
      composite.addConfiguration(defaults);
      
      output = composite;
    }
    else
    {
      output = loadConfig(fileLocation, listDelimiter);
    }
    
    return output;
  }
  
  public boolean saveConfig() {
    return saveConfig(this.config);
  }
  
  public boolean saveConfig(Configuration cfg)
  {
    XLogger.getInstance().log(Level.FINER, "Saving {0}", getClass(), cfg);
    
    boolean saved = false;
    if ((cfg instanceof CompositeConfiguration)) {
      CompositeConfiguration cc = (CompositeConfiguration)cfg;
      Configuration imc = cc.getInMemoryConfiguration();
      if (imc != null) {
        saveConfig(imc);
        saved = true;
      }
    } else if ((cfg instanceof AbstractFileConfiguration)) {
      AbstractFileConfiguration afc = (AbstractFileConfiguration)cfg;
      File file = afc.getFile();
      try {
        afc.save();
        XLogger.getInstance().log(Level.FINE, "Saved " + cfg.getClass().getName() + " to " + file, getClass());
        return true;
      } catch (ConfigurationException e) {
        XLogger.getInstance().log(Level.WARNING, "Error saving " + cfg.getClass().getName() + " to " + file, getClass(), e);
        return false;
      }
    }
    return saved;
  }
  
  private PropertiesConfiguration loadConfig(URL fileLocation, char listDelimiter)
    throws ConfigurationException
  {
    PropertiesConfiguration cfg = new PropertiesConfiguration();
    cfg.setListDelimiter(listDelimiter);
    cfg.setURL(fileLocation);
    cfg.load();
    return cfg;
  }
  
  public String getAbsolutePath(String relativePath) {
    return new File(relativePath).getAbsolutePath();
  }
  
  public Configuration getConfiguration() {
    return this.config;
  }
  
  public JpaContext getJpaContext() {
    return this.jpaContext;
  }
  
  public boolean isInitialized() {
    return this.initialized;
  }
  
  public CapturerApp getCapturerApp() {
    return CapturerApp.getInstance();
  }
  
  public String getPersistenceFilename() {
    return this.persistenceFilename;
  }
  
  public void setPersistenceFilename(String persistenceFilename) {
    if (persistenceFilename == null) {
      throw new NullPointerException();
    }
    this.persistenceFilename = persistenceFilename;
    this.jpaContext = null;
  }
  
  public String getScrapperPropertiesFilename() {
    return this.scrapperPropertiesFilename;
  }
  
  public void setScrapperPropertiesFilename(String scrapperPropertiesFilename) {
    this.scrapperPropertiesFilename = scrapperPropertiesFilename;
  }
}
