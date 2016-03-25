package com.idisc.core;

import java.util.Map;


/**
 * @(#)SendNewsAsEmailProperties.java   28-Mar-2015 09:03:04
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
public interface SendNewsAsEmailProperties {

    public Map getParameters();
    
    public Map getOrderBy();
    
    public int getBatchSize();
    
    public int getSendInterval();

    public String getSenderEmail();

    public char[] getSenderPassword();

    public com.bc.mailservice.Message getEmailMessage();
}
