package com.idisc.core;

/**
 * @(#)HasResult.java   05-Nov-2014 07:57:38
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
public interface HasResult<T> {
    
    /**
     * Returns the result immediately. Does not wait for the task to complete
     * @return 
     */
    T getResult();
}
