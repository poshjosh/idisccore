package com.idisc.core;

import com.bc.mailservice.Message;
import java.util.Map;

public abstract interface SendNewsAsEmailProperties
{
  public abstract Map getParameters();
  
  public abstract Map getOrderBy();
  
  public abstract int getBatchSize();
  
  public abstract int getSendInterval();
  
  public abstract String getSenderEmail();
  
  public abstract char[] getSenderPassword();
  
  public abstract Message getEmailMessage();
}
