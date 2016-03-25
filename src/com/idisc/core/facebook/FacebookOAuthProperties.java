package com.idisc.core.facebook;

import com.idisc.core.IdiscApp;
import com.bc.oauth.AbstractOAuthProperties;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 * @(#)FacebookOAuthProperties.java   17-Oct-2014 22:39:27
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
public class FacebookOAuthProperties extends AbstractOAuthProperties  {
    
    public static final String PROPERTIES_NAME = "facebook";
    
    private Properties props;
    
    @Override
    public Properties getProperties() {
        if(props == null) {
            Configuration config = IdiscApp.getInstance().getConfiguration();
            Configuration subset = config.subset(PROPERTIES_NAME);
            props = new Properties();
            Iterator<String> keys = subset.getKeys();
            while(keys.hasNext()) {
                String key = keys.next();
                props.put(key, subset.getProperty(key));
            }
        }
        return props;
    }
}
