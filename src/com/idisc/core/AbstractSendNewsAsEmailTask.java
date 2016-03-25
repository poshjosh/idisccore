package com.idisc.core;

import com.bc.mailservice.EmailAccess;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Extractedemail;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.mailservice.MailConfig;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


/**
 * @(#)SendMailTask.java   28-Mar-2015 09:00:03
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
public abstract class AbstractSendNewsAsEmailTask extends BatchIterator<Extractedemail>
        implements Runnable, Serializable, SendNewsAsEmailProperties{
    
    public AbstractSendNewsAsEmailTask() { }
    
    @Override
    public void run() {
        try{
            doRun();
        }catch(RuntimeException e) {
            XLogger.getInstance().log(Level.WARNING, "Unexpected exception.", this.getClass(), e);
        }
    }
    private synchronized void doRun(){

        try{

            while(this.hasNext()) {

                try{
                    
                    Collection<Extractedemail> next = this.next();
                    
                    this.sendNext(next);
                    
                    this.wait(this.getSendInterval());

                }catch(MessagingException e) {

                    XLogger.getInstance().log(Level.WARNING, "Error sending emails.", this.getClass(), e);
                }
            }
        }catch(InterruptedException e) {

            XLogger.getInstance().log(Level.WARNING, "Send mail task interrupted", this.getClass(), e);

            // Preserve interrupted status
            //
            Thread.currentThread().interrupt();

        }finally{
            
            this.notifyAll();
        }
    }
    
    private void sendNext(Collection<Extractedemail> next) throws MessagingException {
        
XLogger.getInstance().log(Level.FINER, "Batch size: {0}", this.getClass(), next==null?null:next.size());
                
        Iterator<Extractedemail> iter = next.iterator();

        String [] recipients = new String[next.size()];
        for(int i=0; iter.hasNext(); i++) {
            recipients[i] = iter.next().getEmailAddress();
        }

        if(recipients.length == 0) {
            return;
        }

        String senderEmail = this.getSenderEmail();
        char [] senderPass = this.getSenderPassword();
        com.bc.mailservice.Message msg = this.getEmailMessage();

if(XLogger.getInstance().isLoggable(Level.FINER, this.getClass())) {
XLogger.getInstance().log(Level.FINER, "Recipients: {0}", this.getClass(), recipients==null?null:Arrays.toString(recipients));
}                

        MailConfig mailConfig = IdiscApp.getInstance().getMailConfig();

        boolean outgoing = true;

        java.util.Properties props = mailConfig.getProperties(senderEmail, senderPass != null, outgoing);
        
        if(props == null) {
            throw new NullPointerException();
        }

        EmailAccess emailAccess = new EmailAccess();

        Session session = emailAccess.getSession(senderEmail, senderPass, props);

        MimeMessage mimeMessage = new MimeMessage(session);

        // From
        InternetAddress addressFrom = new InternetAddress(senderEmail);
        mimeMessage.setFrom(addressFrom);

        // To
        InternetAddress addressTo = new InternetAddress(recipients[0]);
        mimeMessage.setRecipient(Message.RecipientType.TO, addressTo);

        if(recipients.length > 1) {
            // BCC
            InternetAddress[] addresses = new InternetAddress[recipients.length - 1];
            for (int i = 1; i < recipients.length; i++) {
                addresses[i-1] = new InternetAddress(recipients[i]);
            }
            mimeMessage.setRecipients(Message.RecipientType.BCC, addresses);
        }

        // Set the Subject
        mimeMessage.setSubject(msg.getSubject());

        mimeMessage.setContent(msg.getMessage(), msg.getContentType());

        Transport.send(mimeMessage);
    }

    @Override
    protected List<Extractedemail> loadNextBatch() {
        return this.getEntityController().select(this.getParameters(), this.getOrderBy(), "AND", this.getBatchSize(), this.getOffset());
//        return this.ec.find(this.getBatchSize(), this.getOffset());
    }
    
    @Override
    public Map getParameters() {
        return null;
    }

    @Override
    public Map getOrderBy() {
        return null;
    }

    private transient EntityController<Extractedemail, Integer> _accessViaGetter;
    public EntityController<Extractedemail, Integer> getEntityController() {
        if(this._accessViaGetter == null) {
            ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
            this._accessViaGetter = factory.getEntityController(Extractedemail.class, Integer.class);
        }
        return _accessViaGetter;
    }
}
