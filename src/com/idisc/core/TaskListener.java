package com.idisc.core;

/**
 * @(#)TaskListener.java   01-Nov-2014 15:22:27
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
public interface TaskListener {

    void taskCreated(TaskHasResult task);
}
