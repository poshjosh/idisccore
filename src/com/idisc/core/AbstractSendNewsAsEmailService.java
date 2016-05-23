package com.idisc.core;

public abstract class AbstractSendNewsAsEmailService
  extends Service<AbstractSendNewsAsEmailTask>
  implements SendNewsAsEmailProperties
{
  public AbstractSendNewsAsEmailTask newTask()
  {
    return new AbstractSendNewsAsEmailTask()
    {
      public int getBatchSize() {
        return AbstractSendNewsAsEmailService.this.getBatchSize();
      }
      
      public int getSendInterval() {
        return AbstractSendNewsAsEmailService.this.getSendInterval();
      }
      
      public String getSenderEmail() {
        return AbstractSendNewsAsEmailService.this.getSenderEmail();
      }
      
      public char[] getSenderPassword() {
        return AbstractSendNewsAsEmailService.this.getSenderPassword();
      }
      
      public String getMessage() {
        return AbstractSendNewsAsEmailService.this.getMessage();
      }

        @Override
        public String getSubject() {
            return AbstractSendNewsAsEmailService.this.getSubject();
        }
    };
  }
  
  public String toString()
  {
    return "Service for running " + AbstractSendNewsAsEmailTask.class.getName();
  }
}
