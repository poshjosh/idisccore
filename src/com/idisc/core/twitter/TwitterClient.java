package com.idisc.core.twitter;

import com.bc.util.XLogger;
import com.idisc.core.Util;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import twitter4j.GeoLocation;
import twitter4j.GeoQuery;
import twitter4j.Location;
import twitter4j.Place;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Trend;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;















public class TwitterClient
{
  private final Twitter twitter;
  private final GeoLocation defaultGeoLocation;
  private final TwitterOAuthProperties oAuth;
  
  public TwitterClient()
  {
    this(new TwitterOAuthProperties());
  }
  
  public TwitterClient(TwitterOAuthProperties oAuth)
  {
    this.oAuth = oAuth;
    
    this.twitter = newTwitter();
    
    this.defaultGeoLocation = new GeoLocation(oAuth.getLatitude(), oAuth.getLongitude());
  }
  








  public final Twitter newTwitter()
  {
    ConfigurationBuilder config = new ConfigurationBuilder();
    boolean debug = XLogger.getInstance().isLoggable(Level.FINE, getClass());
    config.setDebugEnabled(debug);
    config.setOAuthConsumerKey(getConsumerKey());
    config.setOAuthConsumerSecret(getConsumerSecret());
    config.setOAuthAccessToken(this.oAuth.getAccessToken());
    config.setOAuthAccessTokenSecret(this.oAuth.getAccessTokenSecret());
    Twitter twtr = new TwitterFactory(config.build()).getInstance();
    

    return twtr;
  }
  
  public GeoLocation getGeoLocation(String ipAddress) throws TwitterException
  {
    GeoQuery geoQuery = null;
    try
    {
      geoQuery = new GeoQuery(ipAddress);
      
      ResponseList<Place> places = this.twitter.searchPlaces(geoQuery);
      
      if ((places != null) || (!places.isEmpty()))
      {
        Place place = (Place)places.get(0);
        
        XLogger.getInstance().log(Level.FINER, "Ip address: {0}, place: {1}, country: {2}", getClass(), ipAddress, place.getName(), place.getCountryCode());
        

        GeoLocation[][] coords = place.getGeometryCoordinates();
        
        if (coords == null)
        {
          return this.defaultGeoLocation;
        }
        
        return coords[0][0];
      }
      

      return this.defaultGeoLocation;
    }
    catch (TwitterException e) {
      StringBuilder msg = new StringBuilder("Exception encountered searching places for ipAddress: ");
      msg.append(ipAddress).append(", using GeoQuery: ").append(geoQuery);
      
      msg.append(' ').append(e);
    }
    



    return this.defaultGeoLocation;
  }
  
  public List<Trend> getLocationTrends(String ipAddress)
    throws TwitterException
  {
    GeoLocation geoLocation = getGeoLocation(ipAddress);
    
    return getLocationTrends(geoLocation);
  }
  
  public List<Trend> getLocationTrends(GeoLocation geoLocation) throws TwitterException
  {
    ArrayList<Trend> localTrends = null;
    
    ResponseList<Location> locs = null;
    try
    {
      if (geoLocation != null) {
        locs = this.twitter.getClosestTrends(geoLocation);
      }
    }
    catch (NoSuchMethodError ignored) {}
    if ((locs == null) || (locs.isEmpty())) {
      locs = this.twitter.getAvailableTrends();
    }
    
    if ((locs != null) && (!locs.isEmpty()))
    {
      Iterator<Location> iter = locs.iterator();
      


      while (iter.hasNext())
      {
        Location loc = (Location)iter.next();
        
        XLogger.getInstance().log(Level.FINE, "Country: {0}, Place: {1}, URL: {2}.", getClass(), loc.getCountryName(), loc.getPlaceName(), loc.getURL());
        

        String country = loc.getCountryName();
        
        if ((country != null) && (country.trim().equalsIgnoreCase("Nigeria")))
        {


          Trends locationTrends = this.twitter.getPlaceTrends(loc.getWoeid());
          
          Trend[] trendsArr = locationTrends.getTrends();
          
          for (int i = 0; i < trendsArr.length; i++) {
            Trend trend = trendsArr[i];
            if (localTrends == null) {
              localTrends = new ArrayList();
            }
            localTrends.add(trend);
            XLogger.getInstance().log(Level.FINER, "Trend: {0}", getClass(), trend);
          }
        }
      }
    }
    
    if (localTrends == null) {
      localTrends = new ArrayList();
    }
    
    XLogger.getInstance().log(Level.FINER, "Local trends: {0}", getClass(), Integer.valueOf(localTrends.size()));
    
    return localTrends;
  }
  
  public boolean publish(String tweet, Set<String> att)
  {
    XLogger.getInstance().log(Level.FINER, "Tweet: {0}. Attachements: {1}", getClass(), tweet, att);
    
    StatusUpdate statusUpdate = new StatusUpdate(tweet.toString());
    statusUpdate.setPlaceId(this.oAuth.getPlaceId());
    statusUpdate.setLocation(this.defaultGeoLocation);
    statusUpdate.setDisplayCoordinates(true);
    
    boolean output = false;
    InputStream in = null;
    try {
      if ((att != null) && (!att.isEmpty())) {
        String first = (String)att.iterator().next();
        try {
          in = Util.getInputStream(first);
          String name = Util.getFileName(first);
          statusUpdate.setMedia(name, in);
        } catch (IOException e) {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
        }
      }
      try
      {
        Status status = this.twitter.updateStatus(statusUpdate);
        output = status != null;
      } catch (TwitterException e) {
        if ((e.getMessage() != null) && (e.getMessage().contains("denied due to update limits")))
        {
          XLogger.getInstance().logSimple(Level.WARNING, getClass(), e);
        } else {
          XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
        }
      }
      






      return output;
    }
    finally
    {
      if (in != null) {
        try {
          in.close();
        }
        catch (IOException ignored) {}
      }
    }
  }
  
  public Twitter getTwitter() {
    return this.twitter;
  }
  
  private String getConsumerKey() {
    return this.oAuth.getProperties().getProperty("key");
  }
  
  private String getConsumerSecret() {
    return this.oAuth.getProperties().getProperty("secret");
  }
}
