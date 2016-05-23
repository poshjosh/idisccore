package com.idisc.core;

import com.bc.util.XLogger;
import java.util.logging.Level;

/**
 * @author Josh
 */
public class Setup {
    
    public static final synchronized void setupApp() throws Exception {
        
        IdiscApp app = new IdiscApp();
        
        IdiscApp.setInstance(app);
        
        boolean devMode = true;
        String propertiesFilename = devMode ? 
// These are not working yet                    
//                    "META-INF/properties/idisc_scrapper_devmode.properties" : 
//                   "META-INF/properties/idisc_scrapper.properties";
                "META-INF/properties/idisccore_scrapper_devmode.properties" : 
               "META-INF/properties/idisccore_scrapper.properties";
        app.setScrapperPropertiesFilename(propertiesFilename);
        String persistenceFilename = devMode ?
                "META-INF/persistence_remote.xml" : "META-INF/persistence.xml";
        app.setPersistenceFilename(persistenceFilename);
        if(!app.isInitialized()) {
            
            String packageLoggerName = IdiscApp.class.getPackage().getName();
            XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
            XLogger.getInstance().setLogLevel(packageLoggerName, Level.FINE);

            app.init();
        }
    }
}
