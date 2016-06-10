package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Josh
 */
public class Setup {
    
    public static Feed getDummyFeed(Site site, int feedid, String title, String content) {
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
    
    public static final synchronized void setupApp() throws Exception {
        
        IdiscApp app = new IdiscApp();
        
        IdiscApp.setInstance(app);
        
        boolean devMode = true;
        String propertiesFilename = devMode ? 
// These are not working yet                    
//                    "META-INF/properties/idisc_scrapper_devmode.properties" : 
//                   "META-INF/properties/idisc_scrapper.properties";
                "META-INF/properties/idisccore_scrapper_devmode.properties" : 
               "META-INF/properties/idisccore_scrapper.properties";
        app.setScrapperPropertiesFilename(propertiesFilename);
        String persistenceFilename = devMode ?
                "META-INF/persistence_remote.xml" : "META-INF/persistence.xml";
        app.setPersistenceFilename(persistenceFilename);
        if(!app.isInitialized()) {
            
            String packageLoggerName = IdiscApp.class.getPackage().getName();
            XLogger.getInstance().transferConsoleHandler("", packageLoggerName, true);
            XLogger.getInstance().setLogLevel(packageLoggerName, Level.FINE);

            app.init();
        }
    }
}
