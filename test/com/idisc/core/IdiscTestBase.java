package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.scrapper.CapturerApp;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author poshjosh
 */
public class IdiscTestBase {
    
    private static IdiscApp idiscApp;
    
    private static CapturerApp capturerApp;
    
    public IdiscTestBase() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
        this(Level.FINER);
    }
    
    public IdiscTestBase(Level logLevel) 
        throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
        
        if(idiscApp == null) {
            
            idiscApp = this.createIdiscApp("META-INF/persistence_remote.xml");
            IdiscApp.setInstance(idiscApp);
            idiscApp.setScrapperPropertiesFilename("META-INF/properties/idisccore_scrapper_devmode.properties");

            idiscApp.init();

            capturerApp = idiscApp.getCapturerApp();

    //        String [] toLoggers = {com.idisc.core.IdiscApp.class.getPackage().getName(), "com.bc.webdatex", "com.scrapper"};
            String [] toLoggers = {"com.idisc", "com.bc.webdatex", "com.scrapper"};
            XLogger.getInstance().transferConsoleHandler("", toLoggers, true);
            for(String toLogger:toLoggers) {
                XLogger.getInstance().setLogLevel(toLogger, logLevel);
            }
        }
    }

    public String [] getSitenames() {
        return capturerApp.getSiteNames();
    }
    
    public Feed getDummyFeed(Site site, int feedid, String title, String content) {
        if(title == null || content == null) {
            throw new NullPointerException();
        }
        Date date = new Date();
        Feed feed = new Feed();
        feed.setAuthor("Google Fakenews "+feedid);
        feed.setCategories("fakenews,google");
        feed.setContent(content);
        feed.setDatecreated(date);
        feed.setDescription(title);
        feed.setFeeddate(date);
        feed.setFeedid(feedid);
        feed.setImageurl("https://lh3.googleusercontent.com/OZHymXKRuF2oMCJ1fk6YktYvanegh-msV2Ied-tXSIY7DEsXXSIE2DTMhhshWvJQYw=h900");
        feed.setKeywords("google,fakenews"); 
        feed.setSiteid(site); 
        feed.setTimemodified(date);
        feed.setTitle(title);
        feed.setUrl("https://www.google.com/fakenews/fakelink"+feedid+".jsp");
        return feed;
    }
    
    private IdiscApp createIdiscApp(String persistenceFilename) {
        if(persistenceFilename == null) {
            persistenceFilename = "META-INF/persistence_remote.xml";
        }
        IdiscApp app = new IdiscApp();
        app.setPersistenceFilename(persistenceFilename);
        return app;
    }

    public int sizeOf(List list) {
        return list == null ? 0 : list.size();
    }
    
    public String truncate(String str, int maxLen, boolean ellipsize) {
        String output;
        if(str == null || str.isEmpty() || str.length() <= maxLen) {
            output = str;
        }else {
            final String prefix = ellipsize ? "..." : "";
            output = str.substring(0, maxLen-prefix.length()) + prefix;
        }
        return output;
    }
    
    public void log(String format, Object... format_args) {
        log(true, String.format(format, format_args));
    }

    public void log(Object msg) {
        log(true, msg);
    }
    
    public void log(boolean title, Object msg) {
        if(title) {
System.out.print(new Date()+" "+this.getClass().getName()+" ");            
        }
System.out.println(msg);        
    }

    public IdiscApp getIdiscApp() {
        return idiscApp;
    }

    public CapturerApp getCapturerApp() {
        return capturerApp;
    }
}
