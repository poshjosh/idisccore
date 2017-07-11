package com.idisc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Paths;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author Josh
 */
public class IdiscAppImpl extends IdiscApp {

    private final boolean productionMode;
    
    public IdiscAppImpl(
            String corePropsFile, String scrapperPropsFile, 
            String persistenceFile, boolean productionMode) 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException {
        
        this.productionMode = productionMode;
        
        final URL propertiesURL = Paths.get(corePropsFile).toUri().toURL();

        IdiscAppImpl.this.init(propertiesURL, scrapperPropsFile, persistenceFile);
        
        IdiscApp.setInstance(IdiscAppImpl.this);
    }
    
    public final boolean isProductionMode() {
        return productionMode;
    }
}
