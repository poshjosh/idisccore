package com.idisc.core;

import com.bc.util.XLogger;
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
import com.bc.jpa.context.JpaContext;
import com.bc.sql.MySQLDateTimePatterns;
import com.idisc.core.extraction.ExtractionFactory;
import com.idisc.core.extraction.ExtractionFactoryImpl;
import com.idisc.pu.IdiscJpaContext;
import java.util.Objects;

public class IdiscApp {
    
  private boolean initialized;
  private String propertiesFilename;
  private String scrapperPropertiesFilename;
  private String persistenceFilename;
  private Configuration config;
  private static IdiscApp instance;
  private JpaContext jpaContext;
  
  private ExtractionFactory extractionFactory;
  
  public IdiscApp(){
    this.propertiesFilename = "META-INF/properties/idisc.properties"; 
    this.scrapperPropertiesFilename = "META-INF/properties/idisccore_scrapper.properties";
    this.persistenceFilename = "META-INF/persistence.xml";
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
    
    URL propertiesFile = loader.getResource(this.propertiesFilename);
    
    this.init(propertiesFile, this.scrapperPropertiesFilename, this.persistenceFilename);  
  }

  public void init(URL propertiesFile, String scrapperPropertiesFilename, String persistenceFilename)
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException {
      
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    URL defaultPropertiesFile = loader.getResource("META-INF/properties/idiscdefaults.properties");
    
    init(defaultPropertiesFile, propertiesFile, scrapperPropertiesFilename, persistenceFilename);
    
    this.initialized = true;
  }

  private void init(URL defaultPropertiesFilename, URL propertiesFilename,
          String scrapperPropertiesFilename, String persistenceFilename)
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException, UnsupportedOperationException {
      
    if(this.isInitialized()) {
        throw new UnsupportedOperationException("App is already initialized!");
    }  
    
    XLogger.getInstance().log(Level.FINE, "Initializing: {0}", getClass(), getClass().getName());
    
    Objects.requireNonNull(propertiesFilename);
    
    Objects.requireNonNull(scrapperPropertiesFilename);
    
    this.config = loadConfig(defaultPropertiesFilename, propertiesFilename, ',');
    
    this.persistenceFilename = persistenceFilename == null ? "META-INF/persistence.xml" : persistenceFilename;
    
    this.jpaContext = this.initJpaContext(persistenceFilename);
    
    this.scrapperPropertiesFilename = scrapperPropertiesFilename;
    
    CapturerApp.getInstance().init(false, this.scrapperPropertiesFilename);
    
    this.extractionFactory = new ExtractionFactoryImpl(this);
    
    this.initialized = true;
    
    XLogger.getInstance().log(Level.INFO, "Done initializing app", getClass());
  }
  
  public JpaContext initJpaContext(String persistenceFilename) throws IOException {
      if(persistenceFilename == null) {
          persistenceFilename = "META-INF/persistence.xml";
      }
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

  public ExtractionFactory getExtractionFactory() {
    return extractionFactory;
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

  public String getPropertiesFilename() {
    return propertiesFilename;
  }
  
  public String getScrapperPropertiesFilename() {
    return this.scrapperPropertiesFilename;
  }
  
  public String getPersistenceFilename() {
    return this.persistenceFilename;
  }
}
/**
 * 
  public void init()
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException {
    this.init("META-INF/properties/idisccore_scrapper.properties");  
  }
  
  public void init(String scrapperPropertiesFilename)
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException {
    this.init(scrapperPropertiesFilename, null);  
  }
  
  public void init(String scrapperPropertiesFilename, String persistenceFilename)
    throws ConfigurationException, IOException, IllegalAccessException, 
          InterruptedException, InvocationTargetException {
      
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    
    URL defaultFileLoc = loader.getResource("META-INF/properties/idiscdefaults.properties");
    
    URL fileLoc = loader.getResource("META-INF/properties/idisc.properties");
    
    init(defaultFileLoc, fileLoc, scrapperPropertiesFilename, persistenceFilename);
    
    this.initialized = true;
  }

  public void init(URL propertiesFile, String scrapperPropertiesFilename)     
      throws ConfigurationException, IOException, IllegalAccessException, 
      InterruptedException, InvocationTargetException {

    this.init(propertiesFile, scrapperPropertiesFilename, null);
  }
  
 * 
 */