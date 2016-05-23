package com.idisc.core;

import com.bc.mail.config.MailConfig;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * @author poshjosh
 */
public interface EmailBuilder {

    <T extends Email> T from(T email, String user, String pass, boolean ssl, boolean outgoing) throws EmailException;

    <T extends Email> T from(T email, MailConfig mailConfig, String user, String pass, boolean ssl, boolean outgoing) throws EmailException;
    
}
