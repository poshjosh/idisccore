package com.idisc.core.twitter;

import com.idisc.core.IdiscApp;
import com.bc.oauth.AbstractOAuthProperties;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

/**
 * @(#)TwitterOAuthProperties.java   17-Oct-2014 19:55:16
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
public class TwitterOAuthProperties extends AbstractOAuthProperties implements TwitterProperties {

    public static final String PROPERTIES_NAME = "twitter";
    
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
    
    @Override
    public String getPlaceId() {
        return this.getProperties().getProperty(PLACE_ID);
    }

    @Override
    public String getTrendingItem() {
        return this.getProperties().getProperty(TRENDING_ITEM);
    }

    @Override
    public double getLatitude() {
        String lat = this.getProperties().getProperty(LATITUDE);
        return Double.parseDouble(lat);
    }

    @Override
    public double getLongitude() {
        String lng = this.getProperties().getProperty(LONGITUDE);
        return Double.parseDouble(lng);
    }
}
