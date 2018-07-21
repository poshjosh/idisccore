package com.idisc.core.extraction.rss;

import com.bc.net.impl.RequestBuilderImpl;
import java.util.logging.Logger;
import com.idisc.core.IdiscApp;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.SyndFeedOutput;
import com.rometools.rome.io.XmlReader;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import com.bc.net.RequestBuilder;
import com.bc.xml.XmlUtil;
import com.rometools.rome.feed.WireFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.WireFeedInput;
import java.net.URLConnection;
import java.util.Locale;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class RssMgr
  implements Serializable
{
  private transient static final Logger LOG = Logger.getLogger(RssMgr.class.getName());
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
        this.feednamesProps.put(key, subset.getString(key));
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
        if(LOG.isLoggable(Level.WARNING)){
            LOG.log(Level.WARNING,null,  e);
        }
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
      
      final SyndFeed feed;
      
      final boolean direct = true;
      if(direct) {
        SyndFeedInput input = new SyndFeedInput();
        input.setXmlHealerOn(true);
        feed = input.build(xmlReader); 
      }else{
        final InputSource in = new InputSource();
        in.setSystemId(path);
        in.setCharacterStream(xmlReader);
        in.setEncoding("UTF-8");
        final Document doc = new XmlUtil().load(in);

        final boolean validate = false;
        final WireFeed wireFeed = new WireFeedInput(validate, Locale.UK).build(doc);
        final boolean preserveWireFeed = false;
        feed = new SyndFeedImpl(wireFeed, preserveWireFeed);
      }
      
      if(LOG.isLoggable(Level.FINE)){
         LOG.log(Level.FINE, "Path: {0}, feed: {1}", new Object[]{ path,  feed.getTitle()});
      }
      
      return feed;
    }
    finally {
      if (xmlReader != null) {
        try { xmlReader.close();
        } catch (IOException e) {
          LOG.log(Level.WARNING, null, e);
        }
      }  
    }
  }

  private XmlReader getXmlReader(String path) throws IOException {
    XmlReader output = null;
    try {
      final InputStream in = this.getInputStream(path);
      output = new XmlReader(in);
    }catch (UnknownHostException | FileNotFoundException e) {
       LOG.warning(() -> e.toString());
    }catch (MalformedURLException e){
      if (path.startsWith("/")) {
        path = IdiscApp.getInstance().getAbsolutePath(path);
      }
      final File file = new File(path);
      try {
        output = new XmlReader(file);
      }catch (FileNotFoundException fnfe) {
        LOG.warning(() -> fnfe.toString());
      }
    }
    return output;
  }

  private boolean loggedCloudFlareChallenge;
  private InputStream getInputStream(String path) throws IOException {
    InputStream in;
    final URL url = new URL(path);
    final RequestBuilder req = this.getRequestBuilder();
    final com.bc.net.Response cloudFlareRes = new com.bc.net.cloudflare.CloudFlareResponse(
        req, url, 6_000, StandardCharsets.UTF_8){
        @Override
        public InputStream getInputStream(URLConnection urlConn) throws IOException {
            return urlConn.getInputStream();
        }
        @Override
        protected String readAll(InputStream in) throws IOException {
          final String content = super.readAll(in); 
          if(!loggedCloudFlareChallenge) {
            loggedCloudFlareChallenge = true;
            LOG.log(Level.INFO, "------- RESPONSE 403 CONTENT --------\n{0}\n-------------------------------------", content);
          }
          return content;
        }
    };
    in = cloudFlareRes.getInputStream();
    return in;
  }
  
  private InputStream getInputStream_old(String path) throws IOException {
    InputStream in;
    final URL url = new URL(path);
    try{
      in = url.openStream();
    }catch(IOException e) {
      if(e.toString().contains(" 403 ")) {
        try{
          final RequestBuilder req = this.getRequestBuilder();
          final com.bc.net.Response cloudFlareRes = new com.bc.net.cloudflare.CloudFlareResponse(
              req, url, 6_000, StandardCharsets.UTF_8){
              @Override
              protected String readAll(InputStream in) throws IOException {
                final String content = super.readAll(in); 
                if(!loggedCloudFlareChallenge) {
                  loggedCloudFlareChallenge = true;
                  LOG.log(Level.INFO, "------- RESPONSE 403 CONTENT --------\n{0}\n-------------------------------------", content);
                }
                return content;
              }
          };
          in = cloudFlareRes.getInputStream();
        }catch(IOException e_inner) {
          throw e;    
        }
      }else{
        throw e;
      }
    }
    return in;
  }

  public void publish(SyndFeed feed, String path)
  {
    Writer writer = null;
    
    try
    {
      if (path.startsWith("/")) {
        IdiscApp.getInstance().getAbsolutePath(path);
      }
      
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Feed:{0}. saved to path: {1}", new Object[]{ feed.getTitle(),  path});
      }

      writer = new FileWriter(path);
      
      SyndFeedOutput feedOutput = new SyndFeedOutput();
      
      feedOutput.output(feed, writer); 
    }
    catch (IOException | FeedException ex) {
      LOG.log(Level.WARNING, null, ex);
    } finally {
      if (writer != null) try { writer.close();
        }
        catch (IOException e) {}
    }
  }

  private transient RequestBuilder _reqBuilder;
  public RequestBuilder getRequestBuilder() {
      if(_reqBuilder == null) {
          _reqBuilder = new RequestBuilderImpl();
          _reqBuilder
                  .randomUserAgent(true)
                  .connectTimeout(20_000)
                  .readTimeout(60_000);
      }
      return _reqBuilder;
  }
}
