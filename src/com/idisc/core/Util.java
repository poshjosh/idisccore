package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.idisc.pu.entities.Sitetype;
import com.bc.jpa.EntityController;
import com.idisc.core.web.NewsCrawler;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;

/**
 * @(#)Util.java   17-Oct-2014 20:17:42
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.1
 * @since    0.1
 */
public class Util {

// This only works well if there is only one persistence unit and only one 
// database in that persistence unit. If there are more than one, then the 
// result may be misleading.
//    
//    public static String truncate(String tableName, String columnName, String toTruncate) {
//        PersistenceMetaData metaData = IdiscApp.getInstance().getControllerFactory().getMetaData();
//        String puName = metaData.getPersistenceUnitNames()[0];
//        Class anyEntityClass = metaData.getEntityClasses(puName)[0];
//        String databaseName = metaData.getDatabaseName(anyEntityClass);
//        Class tableClass = metaData.getEntityClass(databaseName, tableName);
//        return truncate(tableClass, columnName, toTruncate);
//    }
    
    public static String getFirtImageUrl(NodeList nodeList, NodeFilter imagesFilter) {
        
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

            String imageUrl = imageTag.getImageURL();
            if(acceptImageUrl(imageUrl)) {
if(start != -1) {
XLogger.getInstance().log(Level.FINE, "IMAGE URL: {0}", NewsCrawler.class, imageUrl);
}                
                return imageUrl;
            }
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
    
//@update moved this method here from NewsCrawler.class    
    public static final String format_DOESNT_WORK_WELL(
            String s, String defaultValue, int maxLen, int spaces, boolean removeHtml) {
        if(s == null || s.trim().isEmpty()) {
            s = defaultValue;
        }
        if(s == null) {
            return s;
        }
        if(maxLen < 0 || maxLen > s.length()) {
            maxLen = s.length();
        }
        boolean disabled = false;
        int spacecount = 0;
        StringBuilder builder = new StringBuilder(maxLen);
        StringBuilder temp = new StringBuilder();
        for(int i=0; i<maxLen; i++) {
            char ch = s.charAt(i);
            boolean add = false;
            if(!disabled && Character.isSpaceChar(ch)) {
                ++spacecount;
                if(spacecount <= spaces) {
                    add = true;
                }
            }else{
                spacecount = 0;
                add = true;
                if(removeHtml) {
                    if(ch == '<') {
                        add = false;
                        disabled = true;
                    }else if(ch == '>'){
                        add = false;
                        disabled = false;
                    }
                }
            }
            if(add) {
                if(disabled) {
                    temp.append(ch);
                }else{
                    temp.setLength(0);
                    builder.append(ch);
                }
            }
        }
        if(disabled && temp.length() > 0) {
            builder.append(temp);
        }
        return builder.toString();
    }    
    
    public static String truncate(Class tableClass, String columnName, String toTruncate) {
        
        if(toTruncate == null) {
            return null;
        }
        
        int len;
// This is not suitable. Always returned a length of 255        
//            Column column = tableClass.getField(columnName).getAnnotation(Column.class);

//            len = column.length();
            
        len = getColumnDisplaySize(tableClass, columnName);
        
        return truncate(toTruncate, len);
    }
    
    public static int getColumnDisplaySize(Class tableClass, String columnName) {
        int len;
        if(tableClass == Feed.class) {
            if(columnName.equals("rawid") || columnName.equals("author")) {
                len = 100;
            }else if(columnName.equals("title") || columnName.equals("extradetails")) {
                len = 500;
            }else if(columnName.equals("keywords") ||
                    columnName.equals("categories") ||
                    columnName.equals("description")) {
                len = 1000;
            }else if(columnName.equals("content")) {
                len = 100000;
            }else{
                len = -1;
            }
        }else{
            len = -1;
        }
        return len;
    }
    
    public static String truncate(String s, int maxLen) {
        if(s != null && maxLen > -1 && s.length() > maxLen) {
            s = s.substring(0, maxLen);
        }
        return s;
    }
    
    public static boolean isInDatabase(
            EntityController<Feed, Integer> ec, Feed params, Feed toFind) {
        
//        params.setUrl(toFind.getUrl()); // 
        // title, content or description in that order
        params.setTitle(toFind.getTitle());
        params.setContent(toFind.getContent());
        params.setDescription(toFind.getDescription());
        params.setSiteid(toFind.getSiteid());
        
        try{
            
            Map map = ec.toMap(params, false);

            boolean exists = ec.selectFirst(map) != null;
            
XLogger.getInstance().log(Level.FINE, "Feed exists: {0}, parameters: {1}", Util.class, exists, map);            

            return exists;
            
        }catch(Exception e) {
            XLogger.getInstance().logSimple(Level.WARNING, Util.class, e);
            return false;
        }
    }
    
    public static String getHeading(Feed feed) {
        String sval = feed.getTitle() == null ? null : feed.getTitle().trim();
        if(sval == null || sval.isEmpty()) {
            sval = feed.getContent() == null ? null : feed.getContent().trim();
            if(sval == null || sval.isEmpty()) {
                sval = feed.getDescription() == null ? null : feed.getDescription().trim();
            }
        }
        // We use the title display size of the database to truncate this
        return Util.truncate(Feed.class, "title", sval);
    }
    
    public static Site findSite(
            String sitename,
            Sitetype sitetype,
            boolean createIfNotExists) {
        
        EntityController<Site, ?> ec = 
                IdiscApp.getInstance().getControllerFactory().getEntityController(Site.class);
        
        Map map = new HashMap(2, 1.0f);
        if(sitename != null) {
            map.put("site", sitename);
        }
        if(sitetype != null) {
            map.put("sitetypeid", sitetype);
        }

XLogger.getInstance().log(Level.FINER, "Parameters: {0}", Util.class, map);

        Site site = ec.selectFirst(map);
        
        if(site == null && createIfNotExists) {
            site = new Site();
            site.setDatecreated(new Date());
            site.setSite(sitename);
            site.setSitetypeid(sitetype);
            try{
                ec.create(site);
            }catch(Exception e) {
                site = null;
                XLogger.getInstance().log(Level.WARNING, 
                "Failed to create entity type: "+Site.class.getName()+" using: "+map, Util.class, e);
            }
        }
        return site;
    }
    
    /**
     * Query of form <tt>key_1=val_1&key_2=val_2&key_3=val_3
     * @param params Contains key-value pairs to be used in generating a query String
     * @param appendTo The StringBuilder to append the query created to
     * @param charset If a value is provided an attempt is made to encode the query values using this charset
     * @throws NullPointerException if the StringBuilder appendTo is null
     */
    public static void appendQuery(Map<String, Object> params, 
            StringBuilder appendTo, final String charset) {

        Map<String, Object> update = new HashMap<String, Object>();
        
        Set<Entry<String, Object>> entrySet = params.entrySet();
        
        for(Entry<String, Object> entry:entrySet) {
            Object val = entry.getValue();
            try{
                val = URLEncoder.encode(val.toString(), charset);
            }catch(UnsupportedEncodingException e) {
                XLogger.getInstance().log(Level.WARNING, null, Util.class, e);
            }catch(RuntimeException e) {
                XLogger.getInstance().log(Level.WARNING, null, Util.class, e);
            }finally{
                update.put(entry.getKey(), entry.getValue());
            }
        }
        
        appendQuery(update, appendTo);
    }
    
    /**
     * @param params Contains key-value pairs to be used in generating a query String
     * @return Query of form <tt>key_1=val_1&key_2=val_2&key_3=val_3
     * @throws NullPointerException if the StringBuilder appendTo is null
     */
    public static void appendQuery(Map<String, Object> params, 
            StringBuilder appendTo) {
        
        if(appendTo == null) {
            throw new NullPointerException();
        }
        
        Iterator<Entry<String, Object>> iter = params.entrySet().iterator();
        
        boolean doneFirst = false;
        
        while(iter.hasNext()) {
            
            Entry<String, Object> entry = iter.next();
            
            String key = entry.getKey();
            Object val = entry.getValue();
            
            if(key == null || val == null) {
                continue;
            }
            
            if(doneFirst) {
                appendTo.append('&');
            }else{
                doneFirst = true; 
            }
            
            appendTo.append(key);
            appendTo.append('=');
            appendTo.append(val);
        }
    }

    /**
     * Expected <tt>arg0</tt> format:
     * <tt>'key=value{x}key1=value1{x}key2=value2'</tt><br/><br/>
     * Where {x} referers to the separator used between key-value pairs<br/>
     * For the above input this method will return a Map with the key-value
     * pairs contained in the <tt>arg0</tt> input.
     * @param String
     * @param separator
     * @return
     */
    public static Map<String, String> getParameters(
            String input, String separator) {
        return getParameters(input, separator, false);
    }
    
    public static Map<String, String> getParameters(String input, 
            String separator, boolean nullsAllowed) {

XLogger.getInstance().log(Level.FINER, 
        "Separator: {0}, Nulls allowed: {1}, Query: {2}", 
        Util.class, separator, nullsAllowed, input); 

        LinkedHashMap<String, String> result = new LinkedHashMap<String, String>();

        String [] queryPairs = input.split(separator);

        for(int i=0; i<queryPairs.length; i++) {

XLogger.getInstance().log(Level.FINEST, "Pair[{0}]:{1}", 
        Util.class, i, queryPairs[i]);

            String [] paramPair = queryPairs[i].split("=");
            String key = null; String val = null;
            if(nullsAllowed) {
                if(paramPair.length == 0) {
                    continue;
                }else if(paramPair.length == 1) {
                    key = paramPair[0];
                    val = "";
                }else {
                    key = paramPair[0];
                    val = paramPair[1];
                }
            }else{
                if(paramPair.length < 2) {
                    continue;
                }else{
                    key = paramPair[0];
                    val = paramPair[1];
                }
            }
            result.put(key.trim(), val.trim());
        }

XLogger.getInstance().log(Level.FINER, "Output: {0}", Util.class, result);        
        return result;
    }

    public static String getStackTrace(Throwable t) {
        
        StringBuffer stackTrace = null;
        
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);

            t.printStackTrace(pw);

            stackTrace = sw.getBuffer();

            if(stackTrace.length() == 0) {
                stackTrace.append(t);
            }

        }catch(Exception ex) {

            Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", ex);
        }finally{

            if(pw != null) pw.close();

            if(sw != null) try { sw.close(); }catch(IOException e) {
                Logger.getLogger(Util.class.getName()).log(Level.WARNING, "", e);
            }
        }
        return stackTrace.toString();
    }
    
    public static String getMessage(Throwable t) {
        if(t.getCause() != null) {
            t = t.getCause();
        }
//        String msg = t.getMessage();
//        return msg == null ? t.toString() : msg;
// Some messages make sense only when the Throwable class is viewed
// E.g: java.lang.NoClassDefFoundError: com/scrapper/config/DefaultCapturerConfig        
        return t.toString();
    }
    
    public static InputStream getInputStream(String path) throws IOException {
        try{
            URL url = new URL(path);
            return url.openStream();
        }catch(MalformedURLException e) {
            return new FileInputStream(path);
        }
    }
    
    /**
     * Mirrors logic of method {@link java.io.File#getName()}.
     * Use this method if its not necessary to create a new File object.
     * @param path The path to the file whose name is required
     * @return The name of the file at the specified path
     */
    public static String getFileName(String path) {
        String output = getFileName(path, File.separatorChar);
        if(output == null) {
            output = getFileName(path, '/');
            if(output == null) {
                output = getFileName(path, '\\');
            }
        }
        return output;
    }
    
    private static String getFileName(String path, char separatorChar) {
	int index = path.lastIndexOf(separatorChar);
	if (index == -1 || index == 0) return null;
	return path.substring(index + 1);
    }
}
