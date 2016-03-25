package com.idisc.core;


/**
 * @(#)FeedUpdateService.java   28-Nov-2014 01:07:47
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class FeedUpdateService extends Service<FeedUpdateTask> {

    @Override
    public FeedUpdateTask newTask() {
        return new FeedUpdateTask();
    }
    
    @Override
    public String toString() {
        return "Service for running " + FeedUpdateTask.class.getName();
    }
}
