package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.mailservice.EmailAccess;
import com.bc.mailservice.MailConfig;
import com.bc.mailservice.Message;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Extractedemail;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;











public abstract class AbstractSendNewsAsEmailTask
  extends BatchIterator<Extractedemail>
  implements Runnable, Serializable, SendNewsAsEmailProperties
{
  private transient EntityController<Extractedemail, Integer> _accessViaGetter;
  
  public void run()
  {
    try
    {
      doRun();
    } catch (RuntimeException e) {
      XLogger.getInstance().log(Level.WARNING, "Unexpected exception.", getClass(), e);
    }
  }
  
  private synchronized void doRun()
  {
    try {
      while (hasNext())
      {
        try
        {
          Collection<Extractedemail> next = next();
          
          sendNext(next);
          
          wait(getSendInterval());
        }
        catch (MessagingException e)
        {
          XLogger.getInstance().log(Level.WARNING, "Error sending emails.", getClass(), e);
        }
      }
    }
    catch (InterruptedException e) {
      XLogger.getInstance().log(Level.WARNING, "Send mail task interrupted", getClass(), e);
      


      Thread.currentThread().interrupt();
    }
    finally
    {
      notifyAll();
    }
  }
  
  private void sendNext(Collection<Extractedemail> next) throws MessagingException
  {
    XLogger.getInstance().log(Level.FINER, "Batch size: {0}", getClass(), next == null ? null : Integer.valueOf(next.size()));
    
    Iterator<Extractedemail> iter = next.iterator();
    
    String[] recipients = new String[next.size()];
    for (int i = 0; iter.hasNext(); i++) {
      recipients[i] = ((Extractedemail)iter.next()).getEmailAddress();
    }
    
    if (recipients.length == 0) {
      return;
    }
    
    String senderEmail = getSenderEmail();
    char[] senderPass = getSenderPassword();
    Message msg = getEmailMessage();
    
    if (XLogger.getInstance().isLoggable(Level.FINER, getClass())) {
      XLogger.getInstance().log(Level.FINER, "Recipients: {0}", getClass(), recipients == null ? null : Arrays.toString(recipients));
    }
    
    MailConfig mailConfig = IdiscApp.getInstance().getMailConfig();
    
    boolean outgoing = true;
    
    Properties props = mailConfig.getProperties(senderEmail, senderPass != null, outgoing);
    
    if (props == null) {
      throw new NullPointerException();
    }
    
    EmailAccess emailAccess = new EmailAccess();
    
    Session session = emailAccess.getSession(senderEmail, senderPass, props);
    
    MimeMessage mimeMessage = new MimeMessage(session);
    

    InternetAddress addressFrom = new InternetAddress(senderEmail);
    mimeMessage.setFrom(addressFrom);
    

    InternetAddress addressTo = new InternetAddress(recipients[0]);
    mimeMessage.setRecipient(RecipientType.TO, addressTo);
    
    if (recipients.length > 1)
    {
      InternetAddress[] addresses = new InternetAddress[recipients.length - 1];
      for (int i = 1; i < recipients.length; i++) {
        addresses[(i - 1)] = new InternetAddress(recipients[i]);
      }
      mimeMessage.setRecipients(RecipientType.BCC, addresses);
    }
    

    mimeMessage.setSubject(msg.getSubject());
    
    mimeMessage.setContent(msg.getMessage(), msg.getContentType());
    
    Transport.send(mimeMessage);
  }
  
  protected List<Extractedemail> loadNextBatch()
  {
    return getEntityController().select(getParameters(), getOrderBy(), "AND", getBatchSize(), getOffset());
  }
  

  public Map getParameters()
  {
    return null;
  }
  
  public Map getOrderBy()
  {
    return null;
  }
  
  public EntityController<Extractedemail, Integer> getEntityController()
  {
    if (this._accessViaGetter == null) {
      ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
      this._accessViaGetter = factory.getEntityController(Extractedemail.class, Integer.class);
    }
    return this._accessViaGetter;
  }
}
