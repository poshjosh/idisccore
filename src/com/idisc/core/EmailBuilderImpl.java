package com.idisc.core;

import com.bc.mail.config.MailConfig;
import com.bc.mail.config.MailConnectionProperties;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailException;

/**
 * @author poshjosh
 */
public class EmailBuilderImpl implements EmailBuilder {
    
    @Override
    public <T extends Email> T from(T email, 
            String user, String pass, boolean ssl, boolean outgoing) 
            throws EmailException {
        
        MailConfig mailConfig = IdiscApp.getInstance().getMailConfig();
        
        return this.from(email, mailConfig, user, pass, ssl, outgoing);
    }

    @Override
    public <T extends Email> T from(
            T email, MailConfig mailConfig, 
            String user, String pass, boolean ssl, boolean outgoing) 
            throws EmailException {
        
        email.setSSLOnConnect(ssl);
        
        MailConnectionProperties mailConnProps = mailConfig.getConnectionProperties(user, ssl, outgoing);

        email.setHostName(mailConnProps.getHost());
        if(ssl) {
            email.setSslSmtpPort(""+mailConnProps.getPort());
        }else{
            email.setSmtpPort(mailConnProps.getPort());
        }

        email.setFrom(user);
        
        if(user != null && pass != null) {
            email.setAuthentication(user, pass);
        }
        
        return email;
    }
}
