package com.idisc.core;

import com.idisc.core.util.BatchIterator;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Extractedemail;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.mail.MessagingException;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;

public abstract class AbstractSendNewsAsEmailTask
  extends BatchIterator<Extractedemail>
  implements Runnable, Serializable, SendNewsAsEmailProperties
{
  private transient EntityController<Extractedemail, Integer> _accessViaGetter;
  
  @Override
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
    
    if (XLogger.getInstance().isLoggable(Level.FINER, getClass())) {
      XLogger.getInstance().log(Level.FINER, "Recipients: {0}", getClass(), recipients == null ? null : Arrays.toString(recipients));
    }
    
    final boolean outgoing = true;
    final String senderEmail = getSenderEmail();
    final char[] senderPass = getSenderPassword();

    try{
        
        EmailBuilder emailBuilder = new EmailBuilderImpl();

        HtmlEmail htmlEmail = emailBuilder.from(new HtmlEmail(), senderEmail, String.valueOf(senderPass), senderPass != null, outgoing);

        int i = 0;

        for(; i<recipients.length; i++) {
            final String toAdd = recipients[i];
            try{
                htmlEmail.addTo(toAdd);
                break;
            }catch(EmailException e) {
                XLogger.getInstance().log(Level.WARNING, "Error adding recipient email: "+toAdd, this.getClass(), e);
            }
        }

        ++i;

        for(; i<recipients.length; i++) {
            htmlEmail.addBcc(recipients[i]);
        }

        htmlEmail.setSubject(this.getSubject());

        htmlEmail.setHtmlMsg(this.getMessage());

        htmlEmail.send();
    }catch(EmailException e) {
        
        throw new MessagingException("Error sending email", e);
    }
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
