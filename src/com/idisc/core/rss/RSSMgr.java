package com.idisc.core.rss;

import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;
import com.sun.syndication.io.XmlReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;






public class RSSMgr
  implements Serializable
{
  private transient Properties feednamesProps;
  
  public Properties getFeedNamesProperties()
  {
    if (this.feednamesProps == null) {
      Configuration config = IdiscApp.getInstance().getConfiguration();
      Configuration subset = config.subset("nigerian_newsmedia");
      this.feednamesProps = new Properties();
      Iterator<String> keys = subset.getKeys();
      while (keys.hasNext()) {
        String key = (String)keys.next();
        this.feednamesProps.put(key, subset.getProperty(key));
      }
    }
    return this.feednamesProps;
  }
  
  public List<SyndFeed> getSyndFeeds(Collection<String> localFeedFilePaths)
  {
    if ((localFeedFilePaths == null) || (localFeedFilePaths.isEmpty())) {
      return null;
    }
    
    ArrayList<SyndFeed> feeds = new ArrayList();
    
    SyndFeedFormatter feedFmt = new SyndFeedFormatter();
    
    for (String localFeedFilePath : localFeedFilePaths)
    {
      SyndFeed syndFeed = null;
      
      try
      {
        syndFeed = getSyndFeed(localFeedFilePath);
        
        if (syndFeed != null) {
          syndFeed = feedFmt.format(syndFeed);
        }
      }
      catch (Exception e)
      {
        XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
      }
      
      if (syndFeed != null)
      {
        feeds.add(syndFeed);
      }
    }
    
    return feeds;
  }
  
  public SyndFeed getSyndFeed(String path) throws IOException, FeedException
  {
    XmlReader xmlReader = null;
    
    try
    {
      xmlReader = getXmlReader(path);
      
      if (xmlReader == null)
      {

        return null;
      }
      
      SyndFeedInput input = new SyndFeedInput();
      
      input.setXmlHealerOn(true);
      
      SyndFeed feed = input.build(xmlReader);
      
      XLogger.getInstance().log(Level.FINE, "Path: {0}, Feed: {1}", getClass(), path, feed);
      
      return feed;
    }
    finally {
      if (xmlReader != null)
        try { xmlReader.close();
        } catch (IOException e) {}
    }
  }
  
  private XmlReader getXmlReader(String path) throws IOException {
    XmlReader output = null;
    try {
      URL url = new URL(path);
      output = new XmlReader(url);
    }
    catch (UnknownHostException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    }
    catch (FileNotFoundException e) {
      XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), e.toString());
    }
    catch (MalformedURLException e)
    {
      if (path.startsWith("/")) {
        path = IdiscApp.getInstance().getAbsolutePath(path);
      }
      File file = new File(path);
      try {
        output = new XmlReader(file);
      }
      catch (FileNotFoundException fnfe) {
        XLogger.getInstance().log(Level.WARNING, "{0}", getClass(), fnfe.toString());
      }
    }
    return output;
  }
  



  public void publish(SyndFeed feed, String path)
  {
    Writer writer = null;
    
    try
    {
      if (path.startsWith("/")) {
        IdiscApp.getInstance().getAbsolutePath(path);
      }
      
      XLogger.getInstance().log(Level.FINER, "Feed:{0}. saved to path: {1}", getClass(), feed.getTitle(), path);
      



      writer = new FileWriter(path);
      
      SyndFeedOutput feedOutput = new SyndFeedOutput();
      
      feedOutput.output(feed, writer); return;
    }
    catch (IOException ex) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), ex);
    } catch (FeedException ex) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), ex);
    } finally {
      if (writer != null) try { writer.close();
        }
        catch (IOException e) {}
    }
  }
}
