package com.idisc.core;

/**
 * @(#)TaskHasResult.java   18-Oct-2014 00:06:05
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * The result of the task represented by instances could be accessed 
 * while the task is running via the {@linkplain #getResult()} method
 * @author   chinomso bassey ikwuagwu
 * @version  0.1
 * @since    0.1
 */
public interface TaskHasResult<T> extends Runnable, HasResult<T> {

}
