package com.deprecated.twitter;

import com.bc.oauth.OAuthProperties;
import java.util.logging.Logger;
import com.idisc.core.SocialClient;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
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

public class TwitterClient extends SocialClient<TwitterOAuthProperties> {
    private transient static final Logger LOG = Logger.getLogger(TwitterClient.class.getName());
    
  private final Twitter twitter;
  private final GeoLocation defaultGeoLocation;
  
  public TwitterClient() {
      
    this(new TwitterOAuthPropertiesImpl());
  }
  
  public TwitterClient(TwitterOAuthProperties oAuth) {
      
    super(oAuth);
    
    this.twitter = newTwitter();
    
    this.defaultGeoLocation = new GeoLocation(oAuth.getLatitude(), oAuth.getLongitude());
  }
  
  public final Twitter newTwitter() {
      
    ConfigurationBuilder config = new ConfigurationBuilder();
    boolean debug = LOG.isLoggable(Level.FINE);
    config.setDebugEnabled(debug);
    OAuthProperties oAuthProps = this.getoAuthProperties();
    config.setOAuthConsumerKey(oAuthProps.getKey());
    config.setOAuthConsumerSecret(oAuthProps.getSecret());
    config.setOAuthAccessToken(oAuthProps.getAccessToken());
    config.setOAuthAccessTokenSecret(oAuthProps.getAccessTokenSecret());
    Twitter twtr = new TwitterFactory(config.build()).getInstance();
    
    return twtr;
  }
  
  public GeoLocation getGeoLocation(String ipAddress) throws TwitterException {
      
    GeoQuery geoQuery = null;
    try {
        
      geoQuery = new GeoQuery(ipAddress);
      
      ResponseList<Place> places = this.twitter.searchPlaces(geoQuery);
      
      if ((places != null) || (!places.isEmpty())) {
          
        Place place = (Place)places.get(0);
        
        if(LOG.isLoggable(Level.FINER)){
            LOG.log(Level.FINER, "Ip address: {0}, place name: {1}, country: {2}",  
                    new Object[]{ipAddress,  place.getName(),  place.getCountryCode()});
        }
        
        GeoLocation[][] coords = place.getGeometryCoordinates();
        
        if (coords == null) {
            
          return this.defaultGeoLocation;
        }
        
        return coords[0][0];
      }
      
      return this.defaultGeoLocation;
      
    } catch (TwitterException e) {
        
      StringBuilder msg = new StringBuilder("Exception encountered searching places for ipAddress: ");
      msg.append(ipAddress).append(", using GeoQuery: ").append(geoQuery);
      
      msg.append(' ').append(e);
    }
    
    return this.defaultGeoLocation;
  }
  
  public List<Trend> getLocationTrends(String ipAddress)
    throws TwitterException {
      
    GeoLocation geoLocation = getGeoLocation(ipAddress);
    
    return getLocationTrends(geoLocation);
  }
  
  public List<Trend> getLocationTrends(GeoLocation geoLocation) throws TwitterException {
      
    ArrayList<Trend> localTrends = null;
    
    ResponseList<Location> locs = null;
    try {
      if (geoLocation != null) {
        locs = this.twitter.getClosestTrends(geoLocation);
      }
    } catch (NoSuchMethodError ignored) {}
    if ((locs == null) || (locs.isEmpty())) {
      locs = this.twitter.getAvailableTrends();
    }
    
    if ((locs != null) && (!locs.isEmpty())) {
        
      Iterator<Location> iter = locs.iterator();
      
      while (iter.hasNext()) {
          
        Location loc = (Location)iter.next();
        
        if(LOG.isLoggable(Level.FINE)){
            LOG.log(Level.FINE, "Country: {0}, place name: {1}, URL: {2}.",  
                    new Object[]{loc.getCountryName(),  loc.getPlaceName(),  loc.getURL()});
        }
        
        final String country = loc.getCountryName();
        
        if ((country != null) && (country.trim().equalsIgnoreCase("Nigeria"))) {

          Trends locationTrends = this.twitter.getPlaceTrends(loc.getWoeid());
          
          Trend[] trendsArr = locationTrends.getTrends();
          
          for (Trend trend : trendsArr) {
            if (localTrends == null) {
              localTrends = new ArrayList();
            }
            localTrends.add(trend);
            if(LOG.isLoggable(Level.FINER)){
                  LOG.log(Level.FINER, "Trend: {0}", trend);
            }
          }
        }
      }
    }
    
    if (localTrends == null) {
      localTrends = new ArrayList();
    }
    
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Local trends: {0}", localTrends.size());
    }
    
    return localTrends;
  }
  
  @Override
  public boolean publish(String tweet, Set<String> att) {
      
    if(LOG.isLoggable(Level.FINER)){
      LOG.log(Level.FINER, "Tweet: {0}. Attachements: {1}",new Object[]{ tweet,  att});
    }
    
    StatusUpdate statusUpdate = new StatusUpdate(tweet);
    statusUpdate.setPlaceId(this.getoAuthProperties().getPlaceId());
    statusUpdate.setLocation(this.defaultGeoLocation);
    statusUpdate.setDisplayCoordinates(true);
    
    boolean output = false;
    InputStream in = null;
    try {
      if ((att != null) && (!att.isEmpty())) {
        String first = (String)att.iterator().next();
        try {
          in = this.getInputStream(first);
          String name = Paths.get(first).getFileName().toString();
          statusUpdate.setMedia(name, in);
        } catch (IOException e) {
          if(LOG.isLoggable(Level.WARNING)){
               LOG.log(Level.WARNING, null, e);
          }
        }
      }
      
      try {
          
        Status status = this.twitter.updateStatus(statusUpdate);
        output = status != null;
      } catch (TwitterException e) {
        if ((e.getMessage() != null) && (e.getMessage().contains("denied due to update limits")))
        {
          if(LOG.isLoggable(Level.WARNING)){
               LOG.log(Level.WARNING, null, e);
          }
        } else {
          if(LOG.isLoggable(Level.WARNING)){
               LOG.log(Level.WARNING, null, e);
          }
        }
      }
      
      return output;
      
    }finally {
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
}
