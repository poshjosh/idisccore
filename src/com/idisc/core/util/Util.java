package com.idisc.core.util;

import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Util {
    
  public static long getMillisUntil(
          String timePattern, String timeString, TimeZone timeZone) throws ParseException {
    SimpleDateFormat fmt = new SimpleDateFormat();
    fmt.applyPattern(timePattern);
    fmt.setTimeZone(timeZone);
    Date startHours = fmt.parse(timeString);
    Calendar cal = Calendar.getInstance();
    cal.setTimeZone(timeZone);
    cal.set(11, 0);
    cal.set(12, 0);
    cal.set(13, 0);
    cal.set(14, 0);
    long updatedTime = cal.getTimeInMillis() + startHours.getTime();
    cal.setTimeInMillis(updatedTime);
    startHours = cal.getTime();
    return getMillisUntil(startHours, 1L, TimeUnit.DAYS);
  }
  
  public static long getMillisUntil(Date targetDate, long interval, TimeUnit timeUnit) {
    return getMillisUntil(targetDate.getTime(), interval, timeUnit);
  }
  
  public static long getMillisUntil(long targetDate, long interval, TimeUnit timeUnit) {
    final long currTime = System.currentTimeMillis();
    return currTime < targetDate ? targetDate - currTime : targetDate - currTime + timeUnit.toMillis(interval);
  }
    
  public static String toString(List<Feed> feeds) {
    StringBuilder ids = new StringBuilder();
    for (Feed feed : feeds) {
      ids.append(feed.getFeedid()).append(',');
    }
    return ids.toString();
  }
  
  public static Date getEarliestDate(Collection<Map> feeds, String dateColumnName)
  {
    Date earliestDate = null;
    for (Map feed : feeds) {
      Date date = (Date)feed.get(dateColumnName);
      if (earliestDate == null) {
        earliestDate = date;
      }
      else if (date.before(earliestDate)) {
        earliestDate = date;
      }
    }
    
    return earliestDate;
  }
  
  public static <E extends Feed> Date getEarliestDate(Collection<E> feeds) {
    Date earliestDate = null;
    for (Feed feed : feeds) {
      Date date = feed.getFeeddate() == null ? feed.getDatecreated() : feed.getFeeddate();
      if (earliestDate == null) {
        earliestDate = date;
      }
      else if (date.before(earliestDate)) {
        earliestDate = date;
      }
    }
    
    return earliestDate;
  }
  
  public static String truncate(String s, int maxLen) {
    if ((s != null) && (maxLen > -1) && (s.length() > maxLen)) {
      s = s.substring(0, maxLen);
    }
    return s;
  }
  
  public static boolean isInDatabase(EntityController<Feed, Integer> ec, Feed params, Feed toFind) {
      
    boolean exists = Util.selectFirst(ec, params, toFind) != null;
      
    if(XLogger.getInstance().isLoggable(Level.FINER, Util.class)) {
      XLogger.getInstance().log(Level.FINER, "Feed exists: {0}, feed: {1}", Util.class, exists, toFind);
    }
      
    return exists;
  }
  
  public static Feed selectFirst(EntityController<Feed, Integer> ec, Feed params, Feed toFind) {
      
    params.setTitle(toFind.getTitle());
    params.setContent(toFind.getContent());
    params.setDescription(toFind.getDescription());
    params.setSiteid(toFind.getSiteid());
    
    try {
        
      Map paramMap = ec.toMap(params, false);
      
      return ec.selectFirst(paramMap);
      
    } catch (Exception e) {
        
      XLogger.getInstance().logSimple(Level.WARNING, Util.class, e); 
      
      return null;
    }
  }
  
  public static void appendQuery(Map<String, Object> params, StringBuilder appendTo, String charset)
  {
    Map<String, Object> update = new HashMap();
    
    Set<Map.Entry<String, Object>> entrySet = params.entrySet();
    
    for (Map.Entry<String, Object> entry : entrySet) {
      Object val = entry.getValue();
      try {
        val = URLEncoder.encode(val.toString(), charset);
      } catch (UnsupportedEncodingException e) {
        XLogger.getInstance().log(Level.WARNING, null, Util.class, e);
      } catch (RuntimeException e) {
        XLogger.getInstance().log(Level.WARNING, null, Util.class, e);
      } finally {
        update.put(entry.getKey(), entry.getValue());
      }
    }
    
    appendQuery(update, appendTo);
  }
  
  public static void appendQuery(Map<String, Object> params, StringBuilder appendTo)
  {
    if (appendTo == null) {
      throw new NullPointerException();
    }
    
    Iterator<Map.Entry<String, Object>> iter = params.entrySet().iterator();
    
    boolean doneFirst = false;
    
    while (iter.hasNext())
    {
      Map.Entry<String, Object> entry = (Map.Entry)iter.next();
      
      String key = (String)entry.getKey();
      Object val = entry.getValue();
      
      if ((key != null) && (val != null))
      {


        if (doneFirst) {
          appendTo.append('&');
        } else {
          doneFirst = true;
        }
        
        appendTo.append(key);
        appendTo.append('=');
        appendTo.append(val);
      }
    }
  }
  
  public static Map<String, String> getParameters(String input, String separator)
  {
    return getParameters(input, separator, false);
  }
  

  public static Map<String, String> getParameters(String input, String separator, boolean nullsAllowed)
  {
    XLogger.getInstance().log(Level.FINER, "Separator: {0}, Nulls allowed: {1}, Query: {2}", Util.class, separator, Boolean.valueOf(nullsAllowed), input);
    
    LinkedHashMap<String, String> result = new LinkedHashMap();
    
    String[] queryPairs = input.split(separator);
    
    for (int i = 0; i < queryPairs.length; i++)
    {
      XLogger.getInstance().log(Level.FINEST, "Pair[{0}]:{1}", Util.class, Integer.valueOf(i), queryPairs[i]);
      

      String[] paramPair = queryPairs[i].split("=");
      String key = null;String val = null;
      if (nullsAllowed) {
        if (paramPair.length == 0)
          continue;
        if (paramPair.length == 1) {
          key = paramPair[0];
          val = "";
        } else {
          key = paramPair[0];
          val = paramPair[1];
        }
      } else {
        if (paramPair.length < 2) {
          continue;
        }
        key = paramPair[0];
        val = paramPair[1];
      }
      
      result.put(key.trim(), val.trim());
    }
    
    XLogger.getInstance().log(Level.FINER, "Output: {0}", Util.class, result);
    return result;
  }
  
  public static String getStackTrace(Throwable t)
  {
    
    StringWriter sw = null;
    PrintWriter pw = null;
    try {
      sw = new StringWriter();
      pw = new PrintWriter(sw);
      
      t.printStackTrace(pw);
      
      StringBuffer stackTrace = sw.getBuffer();
      
      if (stackTrace.length() == 0) {
        stackTrace.append(t);
      }
      
      return stackTrace.toString();
    }
    catch (Exception ex)
    {
      Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", ex);
      
      return null;
    }
    finally {
      if (pw != null) { pw.close();
      }
      if (sw != null) try { sw.close();
        } catch (IOException e) { Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", e);
        }
    }
  }
  
  public static String getMessage(Throwable t) {
    if (t.getCause() != null) {
      t = t.getCause();
    }
    return t.toString();
  }
  
  public static InputStream getInputStream(String path) throws IOException {
    try {
      URL url = new URL(path);
      return url.openStream();
    } catch (MalformedURLException e) {}
    return new FileInputStream(path);
  }
  
  public static String getFileName(String path) {
    String output = getFileName(path, File.separatorChar);
    if (output == null) {
      output = getFileName(path, '/');
      if (output == null) {
        output = getFileName(path, '\\');
      }
    }
    return output;
  }
  
  private static String getFileName(String path, char separatorChar) {
    int index = path.lastIndexOf(separatorChar);
    if ((index == -1) || (index == 0)) return null;
    return path.substring(index + 1);
  }
}