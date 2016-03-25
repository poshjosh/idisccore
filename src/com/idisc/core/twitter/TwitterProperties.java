package com.idisc.core.twitter;

/**
 * @(#)TwitterProperties.java   17-Oct-2014 19:56:48
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
public interface TwitterProperties {
    
    String LATITUDE = "latitude";
    String LONGITUDE = "longitude";
    String PLACE_ID = "placeId";
    String TRENDING_ITEM = "trendingItem";
    
    double getLatitude();

    double getLongitude();
    
    String getPlaceId();

    String getTrendingItem();
}
