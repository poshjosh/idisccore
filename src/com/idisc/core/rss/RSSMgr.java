package com.idisc.core.rss;

import com.bc.util.XLogger;
import com.idisc.core.AppProperties;
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
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Level;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.configuration.Configuration;

/**
 * 2 Required Jar files
 * rome-0.9.jar retrieved from http://rome.dev.java.net/
 */
public class RSSMgr implements java.io.Serializable {
    
    private transient Properties feednamesProps;
    
    public RSSMgr( ) {  }

    public Properties getFeedNamesProperties() {
        if(feednamesProps == null) {
            Configuration config = IdiscApp.getInstance().getConfiguration();
            Configuration subset = config.subset(AppProperties.NIGERIAN_NEWSMEDIA);
            feednamesProps = new Properties();
            Iterator<String> keys = subset.getKeys();
            while(keys.hasNext()) {
                String key = keys.next();
                feednamesProps.put(key, subset.getProperty(key));
            }
        }
        return feednamesProps;
    }
    
    public List<SyndFeed> getSyndFeeds(Collection<String> localFeedFilePaths) {

        if(localFeedFilePaths == null || localFeedFilePaths.isEmpty()) {
            return null;
        }

        ArrayList<SyndFeed> feeds = new ArrayList<SyndFeed>();

        SyndFeedFormatter feedFmt = new SyndFeedFormatter();
        
        for(String localFeedFilePath:localFeedFilePaths) {

            SyndFeed syndFeed = null;

            try {

                syndFeed = this.getSyndFeed(localFeedFilePath);

                if(syndFeed != null) {
                    syndFeed = feedFmt.format(syndFeed);
                }

            }catch(Exception e) {
                // This exception may be thrown every hour... so we don't log the full trace
                XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
            }

            if(syndFeed != null) {

                feeds.add(syndFeed);
            }
        }
        
        return feeds;
    }

    public SyndFeed getSyndFeed(String path) throws IOException, FeedException {

        XmlReader xmlReader = null;
                
        try{
            
            xmlReader = this.getXmlReader(path);
            
            if(xmlReader == null) {
                
                
                return null;
            }

            SyndFeedInput input = new SyndFeedInput();

            input.setXmlHealerOn(true);

            SyndFeed feed = input.build(xmlReader);
            
XLogger.getInstance().log(Level.FINE, "Path: {0}, Feed: {1}", 
        this.getClass(), path, feed);
            return feed;
            
        }finally{
            if(xmlReader != null) {
                try{xmlReader.close();}catch(IOException e) {}
            }
        }
    }

    private XmlReader getXmlReader(String path) throws IOException {
        XmlReader output = null;
        try{
            URL url = new URL(path);
            output = new XmlReader(url);
        }catch(UnknownHostException e) {
//            logger.warning(e.toString());
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }catch(FileNotFoundException e) {
//            logger.warning(e.toString());
            XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), e.toString());
        }catch(MalformedURLException e) {
            // May be a file
            //
            if(path.startsWith("/")) {
                path = IdiscApp.getInstance().getAbsolutePath(path);
            }
            File file = new File(path);
            try{
                output = new XmlReader(file);
            }catch(FileNotFoundException fnfe) {
//                logger.warning(fnfe.toString());
                XLogger.getInstance().log(Level.WARNING, "{0}", this.getClass(), fnfe.toString());
            }
        }
        return output;
    }

    /**
     * Publishes the <tt>SyndFeed</tt> to the local file system
     */
    public void publish(SyndFeed feed, String path) {

        Writer writer = null;

        try {

            if(path.startsWith("/")) {
                IdiscApp.getInstance().getAbsolutePath(path);
            }        

XLogger.getInstance().log(Level.FINER, 
"Feed:{0}. saved to path: {1}", 
this.getClass(), feed.getTitle(), path);
            
//            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));            
            writer = new FileWriter(path);
            
            SyndFeedOutput feedOutput = new SyndFeedOutput();
            
            feedOutput.output(feed, writer);

        }catch (IOException ex) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), ex);
        }catch(FeedException ex) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), ex);
        }finally{
            if(writer != null) try { writer.close(); }catch(IOException e) {  }
        }
    }
}
