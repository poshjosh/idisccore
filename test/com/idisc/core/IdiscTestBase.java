package com.idisc.core;

import com.bc.util.XLogger;
import com.scrapper.CapturerApp;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author poshjosh
 */
public class IdiscTestBase {
    
    private final IdiscApp idiscApp;
    
    private final CapturerApp capturerApp;
    
    public IdiscTestBase() throws Exception {
        this(Level.FINER);
    }
    
    public IdiscTestBase(Level logLevel) throws Exception{
        
        idiscApp = this.createIdiscApp("META-INF/persistence_remote.xml");
        IdiscApp.setInstance(idiscApp);
        idiscApp.setScrapperPropertiesFilename("META-INF/properties/idisccore_scrapper_devmode.properties");
        
        idiscApp.init();
        
        capturerApp = idiscApp.getCapturerApp();

//        String [] toLoggers = {com.idisc.core.IdiscApp.class.getPackage().getName(), "com.bc.webdatex", "com.scrapper"};
        String [] toLoggers = {"com.idisc", "com.bc.webdatex", "com.scrapper"};
        XLogger.getInstance().transferConsoleHandler("", toLoggers, true);
        for(String toLogger:toLoggers) {
            XLogger.getInstance().setLogLevel(toLogger, logLevel);
        }
    }
    private IdiscApp createIdiscApp(String persistenceFilename) {
        if(persistenceFilename == null) {
            persistenceFilename = "META-INF/persistence_remote.xml";
        }
        IdiscApp app = new IdiscApp();
        app.setPersistenceFilename(persistenceFilename);
        return app;
    }

    public void log(String msg) {
        log(true, msg);
    }
    
    public void log(boolean title, String msg) {
        if(title) {
System.out.print(new Date()+" "+this.getClass().getName()+" ");            
        }
System.out.println(msg);        
    }

    public IdiscApp getIdiscApp() {
        return idiscApp;
    }

    public CapturerApp getCapturerApp() {
        return capturerApp;
    }
}
