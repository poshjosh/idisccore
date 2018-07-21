package com.idisc.core;

import com.bc.jpa.context.JpaContext;
import com.bc.sql.MySQLDateTimePatterns;
import com.bc.webdatex.context.CapturerContextFactoryImpl;
import com.idisc.pu.IdiscJpaContext;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Site;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.LogManager;
import org.apache.commons.configuration.ConfigurationException;
import com.bc.webdatex.context.CapturerContextFactory;

/**
 * @author poshjosh
 */
public class IdiscTestBase implements SiteNames {
    
    private final SimpleDateFormat loggerDateFormat = new SimpleDateFormat("HH:mm:ss");
    
    private static IdiscApp idiscApp;
    
    public IdiscTestBase() {

        final String res = "META-INF/logging.properties";
        
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        
        try(final InputStream in = loader.getResourceAsStream(res)) {
        
            System.out.println("Reading configuration from: " + loader.getResource(res));
            
            LogManager.getLogManager().readConfiguration(in);
            
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String [] getSitenames() {
        return this.getContextFactory().getConfigService().getConfigNames().toArray(new String[0]);
    }
    
    private CapturerContextFactory cf;
    public CapturerContextFactory getContextFactory() {
        if(cf == null) {
            cf = new CapturerContextFactoryImpl(
                    Paths.get(this.getContextFactoryUri()).toFile());
        }
        return cf;
    }
    
    public URI getContextFactoryUri() {
        try{
            final URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/configs");
            log(this.getClass(), "ContextFactory URL: " + url);
            return url.toURI();
        }catch(URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
        if(idiscApp == null) {
            
System.out.println(this.getClass().getName()+"= = = = = = = = = = = = =  Initializing IdiscApp");

            idiscApp = this.createIdiscApp();
            
            JpaContext jpaContext = idiscApp.getJpaContext();
            
System.out.println(this.getClass().getName()+"= = = = = = = = = = = = =  JpaContext type: "+jpaContext==null?null:jpaContext.getClass().getName());
            
//            jpaContext.getQueryBuilder(Feed.class);
        }
        return idiscApp;
    }

    private IdiscApp createIdiscApp() {
        try{
            final String userHome = System.getProperty("user.home");
            IdiscApp app = new IdiscAppImpl(
                new File(userHome+"/Documents/NetBeansProjects/idisccore/src/main/resources/META-INF/properties/idisc.properties").toURI().toURL(),
                userHome+"/Documents/NetBeansProjects/idisccore/src/main/resources/META-INF/properties/idisccore_scrapper_devmode.properties",
                this.getJpaUri(), 
                false);
            return app;
        }catch(ConfigurationException | IOException | IllegalAccessException | 
                InterruptedException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
    
    public JpaContext createJpaContext() throws IOException {
        return new IdiscJpaContext(this.getJpaUri(), new MySQLDateTimePatterns());
    }
    
    public String getJpaUri() {
        return System.getProperty("user.home")+"/Documents/NetBeansProjects/idiscpu/src/test/resources/META-INF/persistence.xml";
    }
}
