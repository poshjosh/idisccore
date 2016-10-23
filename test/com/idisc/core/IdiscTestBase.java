package com.idisc.core;

import com.bc.jpa.JpaContext;
import com.bc.util.XLogger;
import com.idisc.pu.IdiscJpaContext;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import com.scrapper.CapturerApp;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.configuration.ConfigurationException;

/**
 * @author poshjosh
 */
public class IdiscTestBase {
    
    private final SimpleDateFormat loggerDateFormat = new SimpleDateFormat("HH:mm:ss");
    
    String NAIJ = "naij";
    String DAILY_TRUST = "dailytrust";
    String PUNCH_NG = "punchng";
    String CHANNELSTV_HEADLINES = "channelstv_headlines";
    String BELLANAIJA = "bellanaija";
    String LINDAIKEJI = "lindaikeji.blogspot";
    String THISDAY = "thisday";
    String NGRGUARDIANNEWS = "ngrguardiannews";
    String THENATIONONLINENG = "thenationonlineng";
    String VANGUARDNGR = "vanguardngr";
    String THENEWSMINUTE = "thenewsminute";
   
    private static IdiscApp idiscApp;
    
    private static CapturerApp capturerApp;
    
    public IdiscTestBase() 
            throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
        this(Level.FINE);
    }
    
    public IdiscTestBase(Level logLevel) 
        throws ConfigurationException, IOException, IllegalAccessException, 
            InterruptedException, InvocationTargetException{
        
System.out.println(this.getClass().getName()+"= = = = = = = = = = = = =  Log level: "+logLevel);
        
        if(idiscApp == null) {
            
System.out.println(this.getClass().getName()+"= = = = = = = = = = = = =  Initializing IdiscApp");

            idiscApp = this.createIdiscApp();
            IdiscApp.setInstance(idiscApp);
            idiscApp.setScrapperPropertiesFilename("META-INF/properties/idisccore_scrapper_devmode.properties");

            idiscApp.init();
            
            JpaContext jpaContext = idiscApp.getJpaContext();
            
System.out.println(this.getClass().getName()+"= = = = = = = = = = = = =  JpaContext type: "+jpaContext==null?null:jpaContext.getClass().getName());
            
//            jpaContext.getQueryBuilder(Feed.class);

            capturerApp = idiscApp.getCapturerApp();
            
            XLogger xlog = XLogger.getInstance();
            String [] toLoggers = {"com.idisc", "com.bc.webdatex", "com.scrapper"};
//            xlog.transferConsoleHandler("", toLoggers, true);
            for(String toLogger:toLoggers) {
                xlog.setLogLevel(toLogger, logLevel);
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
    
    private IdiscApp createIdiscApp() {
        try{
            final URI uri = new URI("file:/C:/Users/Josh/Documents/NetBeansProjects/idiscpu/test/META-INF/persistence.xml");
            IdiscApp app = new IdiscApp(){
                @Override
                public JpaContext initJpaContext(String persistenceFilename) throws IOException {
                    return new IdiscJpaContext(uri);
                }
            };
            return app;
        }catch(Exception e) {
            throw new RuntimeException(e);
        }
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
        log(title?this.getClass():null, msg);
    }

    public void log(Class title, Object msg) {
System.out.print(loggerDateFormat.format(new Date()) +" "+(title==null?' ':title.getSimpleName())+' ');            
System.out.println(msg);        
    }
    
    public IdiscApp getIdiscApp() {
        return idiscApp;
    }

    public CapturerApp getCapturerApp() {
        return capturerApp;
    }
}
