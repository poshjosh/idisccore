package com.idisc.core;

import com.bc.config.CompositeConfig;
import com.bc.config.Config;
import com.bc.config.ConfigService;
import com.bc.config.SimpleConfigService;
import java.util.logging.Logger;
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
import com.bc.webdatex.context.CapturerContextFactoryImpl;
import com.idisc.core.extraction.ExtractionFactory;
import com.idisc.core.extraction.ExtractionFactoryImpl;
import com.idisc.pu.IdiscJpaContext;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;
import com.bc.webdatex.context.CapturerContextFactory;

public class IdiscApp {
    private transient static final Logger LOG = Logger.getLogger(IdiscApp.class.getName());
    
  private boolean initialized;
  private String propertiesFilename;
  private String scrapperPropertiesFilename;
  private String persistenceFilename;
  private Configuration config;
  private static IdiscApp instance;
  private JpaContext jpaContext;
  
  private ExtractionFactory extractionFactory;
  
  private CapturerContextFactory scrapperContextFactory;

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
    
    if(LOG.isLoggable(Level.FINE)){
      LOG.log(Level.FINE, "Initializing: {0}", getClass().getName());
    }
    
    Objects.requireNonNull(propertiesFilename);
    
    Objects.requireNonNull(scrapperPropertiesFilename);
    
    this.config = loadConfig(defaultPropertiesFilename, propertiesFilename, ',');
    
    this.persistenceFilename = persistenceFilename == null ? "META-INF/persistence.xml" : persistenceFilename;
    
    this.jpaContext = this.initJpaContext(persistenceFilename);
    
    this.scrapperPropertiesFilename = scrapperPropertiesFilename;
    
    final ConfigService<Properties> scrapperConfigSvc = new SimpleConfigService(
            null, this.scrapperPropertiesFilename);
    
    final Config<Properties> scrapperConfig = new CompositeConfig(scrapperConfigSvc);
      
    this.scrapperContextFactory = new CapturerContextFactoryImpl(
            Paths.get(getConfigsDir(scrapperConfig, false)).toFile(), 
            this.getDefaultConfigname(scrapperConfig)
    );
            
    this.extractionFactory = new ExtractionFactoryImpl(this);
    
    this.initialized = true;
    
    if(LOG.isLoggable(Level.INFO)){
      LOG.log(Level.INFO, "Done initializing app", getClass());
    }
  }

  public URI getConfigsDir(Config config, boolean remote) {
    String propName = remote ? "configsDirRemote" : "configsDir";

    String configsDir = config.get(propName).trim();
    URI uri;
    try {
      URL url = Thread.currentThread().getContextClassLoader().getResource(configsDir);
      if (url == null) {
        uri = Paths.get(configsDir).toUri();
      } else {
        uri = url.toURI();
      }
    } catch (URISyntaxException e) {
      throw new RuntimeException("Failed to load: " + configsDir, e);
    }
if(LOG.isLoggable(Level.FINE)){
LOG.log(Level.FINE, "{0} = {1}", new Object[]{ propName,  uri});
}
    return uri;
  }
  
  public String getDefaultConfigname(Config config) {
    return config.get("defaultConfigName").trim();
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
      
    if(LOG.isLoggable(Level.INFO)){
      LOG.log(Level.INFO, "Loading properties configuration. List delimiter: {0}\nDefault file: {1}\nFile: {2}", new Object[]{ Character.valueOf(listDelimiter),  defaultFileLocation,  fileLocation});
    }

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
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Saving {0}", cfg);
    }
    
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
        if(LOG.isLoggable(Level.FINE)){
            LOG.log(Level.FINE, "Saved " + cfg.getClass().getName() + " to " + file, getClass());
        }
        return true;
      } catch (ConfigurationException e) {
        if(LOG.isLoggable(Level.WARNING)){
            LOG.log(Level.WARNING, "Error saving " + cfg.getClass().getName() + " to " + file, e);
        }
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
  
  public CapturerContextFactory getScrapperContextFactory() {
    return this.scrapperContextFactory;
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