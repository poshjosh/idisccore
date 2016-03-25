package com.idisc.core;

/**
 * @(#)AppProperties.java   16-Oct-2014 10:27:18
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
public interface AppProperties {

    String NIGERIAN_NEWSMEDIA = "nigerian_newsmedia";
    
    String BASE_URL = "baseURL";
    
    String MAX_FEED_AGE = "maxFeedAge";
    
    String ARCHIVE_BATCH_SIZE = "archiveBatchSize";
    
    String REQUESTHANDLER_PROVIDER = "requesthandlerFactory";
    
    String TIMEOUT_PER_TASK_SECONDS = "timeoutPerTaskSeconds";

    String TIMEOUT_PER_SITE_SECONDS = "timeoutPerSiteSeconds";
    
    String MAXCONCURRENT = "maxConcurrentSites";
    
    String TOLERANCE = "dataComparisonTolerance";

    String MAX_FAILS_ALLOWED = "maxFailsAllowedPerSite";

}
