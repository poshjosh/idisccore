package com.idisc.core.extraction.rss;

import com.bc.net.CloudFlareConnectionHandler;
import com.bc.net.ConnectionManager;
import com.bc.net.HttpStreamHandlerForBadStatusLine;
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
import java.io.InputStream;
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

public class RssMgr
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
    
    RssFeedFormatter feedFmt = new RssFeedFormatter();
    
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
      catch (IOException | FeedException e)
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
      URL url;
//      url = new URL(path);
      url = new URL(null, path, new HttpStreamHandlerForBadStatusLine());
      
// This will often lead to response code 403, i.e forbidden 
// probably due to user-agent not being set      
//      output = new XmlReader(url); 
      ConnectionManager connMgr = this.getConnectionManager();
      connMgr.setGenerateRandomUserAgent(true);
      connMgr.setConnectTimeout(20000);
      connMgr.setReadTimeout(60000);
      connMgr.setAddCookies(true);
      connMgr.setGetCookies(true);
      connMgr.setConnectionHandler(new CloudFlareConnectionHandler()); // Handle cloud flare javascript challenge
      
      InputStream in = connMgr.getInputStream(url);
      
      output = new XmlReader(in);
    }
    catch (UnknownHostException | FileNotFoundException e) {
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
      
      feedOutput.output(feed, writer); 
    }
    catch (IOException | FeedException ex) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), ex);
    } finally {
      if (writer != null) try { writer.close();
        }
        catch (IOException e) {}
    }
  }

  private transient ConnectionManager _cm;
  public ConnectionManager getConnectionManager() {
      if(_cm == null) {
          _cm = new ConnectionManager();
      }
      return _cm;
  }
}
