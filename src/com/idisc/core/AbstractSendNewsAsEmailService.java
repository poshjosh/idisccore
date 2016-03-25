package com.idisc.core;

import com.bc.mailservice.Message;


/**
 * @(#)SendNewsAsEmailService.java   28-Mar-2015 06:52:16
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
public abstract class AbstractSendNewsAsEmailService 
        extends Service<AbstractSendNewsAsEmailTask> implements SendNewsAsEmailProperties {

    public AbstractSendNewsAsEmailService() { }
    
    @Override
    public AbstractSendNewsAsEmailTask newTask() {
        return new AbstractSendNewsAsEmailTask(){
            @Override
            public int getBatchSize() {
                return AbstractSendNewsAsEmailService.this.getBatchSize();
            }
            @Override
            public int getSendInterval() {
                return AbstractSendNewsAsEmailService.this.getSendInterval();
            }
            @Override
            public String getSenderEmail() {
                return AbstractSendNewsAsEmailService.this.getSenderEmail();
            }
            @Override
            public char[] getSenderPassword() {
                return AbstractSendNewsAsEmailService.this.getSenderPassword();
            }
            @Override
            public Message getEmailMessage() {
                return AbstractSendNewsAsEmailService.this.getEmailMessage();
            }
        };
    }
    
    @Override
    public String toString() {
        return "Service for running " + AbstractSendNewsAsEmailTask.class.getName();
    }
}

