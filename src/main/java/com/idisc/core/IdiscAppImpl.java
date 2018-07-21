package com.idisc.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author Josh
 */
public class IdiscAppImpl extends IdiscApp {

    private final boolean productionMode;
    
    public IdiscAppImpl(
            URL corePropsFile, String scrapperPropsFile, 
            String persistenceFile, boolean productionMode) 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException {
        
        this.productionMode = productionMode;
        
        IdiscAppImpl.this.init(corePropsFile, scrapperPropsFile, persistenceFile);
        
        IdiscApp.setInstance(IdiscAppImpl.this);
    }
    
    public final boolean isProductionMode() {
        return productionMode;
    }
}
