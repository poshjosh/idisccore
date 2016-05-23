package com.idisc.core;

import com.bc.jpa.EntityController;
import com.bc.util.XLogger;
import com.idisc.core.web.NewsCrawler;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
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
import org.htmlparser.Attribute;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Tag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;

public class Util
{
    
  public static void printFirstDateLastDateAndFeedIds(String key, List<Feed> feeds, Level level) {
    if ((feeds != null) && (feeds.size() > 1)) {
      Feed first = (Feed)feeds.get(0);
      Feed last = (Feed)feeds.get(feeds.size() - 1);
      XLogger.getInstance().log(level, "{0}. First feed, date: {1}. Last feed, date: {2}\n{3}", 
              Util.class, key, first.getFeeddate(), last.getFeeddate(), toString(feeds));
    }
  }
    
  public static String toString(List<Feed> feeds)
  {
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
  
  public static Date getEarliestDate(Collection<Feed> feeds) {
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
  
  public static NodeFilter createImagesFilter(final String baseUrl) {
    if (baseUrl == null) {
      return null;
    }
    NodeFilter imagesFilter = new NodeFilter()
    {
      @Override
      public boolean accept(Node node) {
        if ((node instanceof Tag)) {
          Tag tag = (Tag)node;
          if (((tag instanceof ImageTag)) || ("IMG".equalsIgnoreCase(tag.getTagName()))) {
            Attribute attr = tag.getAttributeEx("src");
            if (attr == null) {
              return false;
            }
            String value = attr.getValue();
            if (value == null) {
              return false;
            }
            return value.toLowerCase().startsWith(baseUrl);
          }
          return false;
        }
        
        return false;
      }
      
    };
    return imagesFilter;
  }
  
    public static String getFirstImageUrl(NodeList nodeList, NodeFilter imagesFilter) {
    
        try{
            
            if(imagesFilter != null) {

                String html = nodeList.toHtml();
                int start = html.indexOf("<img ");
                if(start == -1) {
                    start = html.indexOf("<IMG ");
                }
                if(start != -1) {
                    int end = start + 200 > html.length() ? html.length() : start + 200;
                    XLogger.getInstance().log(Level.FINE, "IMAGE part: {0}", NewsCrawler.class, html.substring(start, end));
                }

                ImageTag imageTag = (ImageTag)getFirst(nodeList, imagesFilter);

                if (imageTag != null) {
                    
                    String imageUrl = imageTag.getImageURL();
                    if(acceptImageUrl(imageUrl)) {
if(start != -1) {
XLogger.getInstance().log(Level.FINE, "IMAGE URL: {0}", NewsCrawler.class, imageUrl);
}                
                        return imageUrl;
                    }
                }
            }
        }catch(Exception e) {
            
            XLogger.getInstance().log(Level.WARNING, "Error extracting image url", Util.class, e);
        }
        
        return null;
    }
    
    private static Node getFirst(NodeList nodeList, NodeFilter filter) {
//        NodeList nodes = nodeList.extractAllNodesThatMatch(filter, true);        
        Node output = null;
        for (Node node:nodeList) {
            if (filter.accept (node)) {
                output = node;
                break;
            }
            NodeList children = node.getChildren();
            if (null != children) {
                output = getFirst(children, filter);
                if(output != null) {
                    break;
                }
            }    
        }
        return output;
    }
    
    private static boolean acceptImageUrl(String imageUrl) {
        
        if(imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }
        
//@todo unwanted formats. Make this a property                
// https://d5nxst8fruw4z.cloudfront.net/atrk.gif?account=rrH8k1a0CM00UH                
        int n = imageUrl.indexOf(".cloudfront.net/");
        if(n != -1) {
            return false;
        }
  
        try{
            URL url = new URL(imageUrl);
            return true;
        }catch(MalformedURLException e) {
            return false;
        }
// Potentially time consuming        
//        try{
//            return ConnectionManager.exists(imageUrl);
//        }catch(Exception e) {
//            return false;
//        }    
    }
  
  public static String truncate(Class tableClass, String columnName, String toTruncate)
  {
    if (toTruncate == null) {
      return null;
    }

    int len = getColumnDisplaySize(tableClass, columnName);
    
    return truncate(toTruncate, len);
  }
  
  public static int getColumnDisplaySize(Class tableClass, String columnName) { 
    int len;
    if (tableClass == Feed.class) { 
      if ((columnName.equals("rawid")) || (columnName.equals("author"))) {
        len = 100; } else { 
        if ((columnName.equals("title")) || (columnName.equals("extradetails"))) {
          len = 500; } else {
          if ((columnName.equals("keywords")) || (columnName.equals("categories")) || (columnName.equals("description")))
          {

            len = 1000; } else { 
            if (columnName.equals("content")) {
              len = 100000;
            } else
              len = -1;
          }
        }
      } } else { len = -1;
    }
    return len;
  }
  
  public static String truncate(String s, int maxLen) {
    if ((s != null) && (maxLen > -1) && (s.length() > maxLen)) {
      s = s.substring(0, maxLen);
    }
    return s;
  }
  



  public static boolean isInDatabase(EntityController<Feed, Integer> ec, Feed params, Feed toFind)
  {
    params.setTitle(toFind.getTitle());
    params.setContent(toFind.getContent());
    params.setDescription(toFind.getDescription());
    params.setSiteid(toFind.getSiteid());
    
    try
    {
      Map map = ec.toMap(params, false);
      
      boolean exists = ec.selectFirst(map) != null;
      
      XLogger.getInstance().log(Level.FINER, "Feed exists: {0}, parameters: {1}", Util.class, Boolean.valueOf(exists), map);
      
      return exists;
    }
    catch (Exception e) {
      XLogger.getInstance().logSimple(Level.WARNING, Util.class, e); }
    return false;
  }
  
  public static String getHeading(Feed feed)
  {
    String sval = feed.getTitle() == null ? null : feed.getTitle().trim();
    if ((sval == null) || (sval.isEmpty())) {
      sval = feed.getContent() == null ? null : feed.getContent().trim();
      if ((sval == null) || (sval.isEmpty())) {
        sval = feed.getDescription() == null ? null : feed.getDescription().trim();
      }
    }
    
    return truncate(Feed.class, "title", sval);
  }
  



  public static Site findSite(String sitename, Sitetype sitetype, boolean createIfNotExists)
  {
    EntityController<Site, ?> ec = IdiscApp.getInstance().getControllerFactory().getEntityController(Site.class);

    Map map = new HashMap(2, 1.0F);
    if (sitename != null) {
      map.put("site", sitename);
    }
    if (sitetype != null) {
      map.put("sitetypeid", sitetype);
    }
    
    XLogger.getInstance().log(Level.FINER, "Parameters: {0}", Util.class, map);
    
    Site site = (Site)ec.selectFirst(map);
    
    if ((site == null) && (createIfNotExists)) {
      site = new Site();
      site.setDatecreated(new Date());
      site.setSite(sitename);
      site.setSitetypeid(sitetype);
      try {
        ec.create(site);
      } catch (Exception e) {
        site = null;
        XLogger.getInstance().log(Level.WARNING, "Failed to create entity type: " + Site.class.getName() + " using: " + map, Util.class, e);
      }
    }
    
    return site;
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
  
  public static String getMessage(Throwable t)
  {
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
  






  public static String getFileName(String path)
  {
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
