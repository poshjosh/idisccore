package com.idisc.core;

/**
 * @(#)IOWrapper.java   23-Jan-2015 08:59:29
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @param <K>
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class IOWrapper<K> extends com.bc.io.IOWrapper<K> {

    public IOWrapper() { }
    
    public IOWrapper(K target, String filename) {
        super(target, filename);
    }

    @Override
    public String getPath(String fileName) {
        return IdiscApp.getInstance().getAbsolutePath(fileName);
    }
}
