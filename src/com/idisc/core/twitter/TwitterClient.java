package com.idisc.core.twitter;

import com.bc.oauth.OAuthProperties;
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
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * @(#)TwitterClient.java   17-Oct-2014 19:49:41
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */
/**
 * @author   chinomso bassey ikwuagwu
 * @version  0.1
 * @since    0.1
 */
public class TwitterClient {

    private final Twitter twitter;
    
    private final GeoLocation defaultGeoLocation;
    
    private final TwitterOAuthProperties oAuth;
    
    public TwitterClient() {
        this(new TwitterOAuthProperties());
    }
    
    public TwitterClient(TwitterOAuthProperties oAuth) {
        
        this.oAuth = oAuth;

        this.twitter = newTwitter();

        this.defaultGeoLocation = new GeoLocation(
                oAuth.getLatitude(), oAuth.getLongitude());
    }
    
    public final Twitter newTwitter() {
        Twitter twtr;
        // Both methods are OK
        if(false) {
            twtr = TwitterFactory.getSingleton();
            twtr.setOAuthConsumer(this.getConsumerKey(), this.getConsumerSecret());
            AccessToken accessToken = new AccessToken(oAuth.getAccessToken(), oAuth.getAccessTokenSecret());
            twtr.setOAuthAccessToken(accessToken);
        }else{
            ConfigurationBuilder config = new ConfigurationBuilder();
            boolean debug = XLogger.getInstance().isLoggable(Level.FINE, this.getClass());
            config.setDebugEnabled(debug);
            config.setOAuthConsumerKey(this.getConsumerKey());
            config.setOAuthConsumerSecret(this.getConsumerSecret());
            config.setOAuthAccessToken(oAuth.getAccessToken());
            config.setOAuthAccessTokenSecret(oAuth.getAccessTokenSecret());
            twtr = new TwitterFactory(config.build()).getInstance();
        }
        
        return twtr;
    }

    public GeoLocation getGeoLocation(String ipAddress) throws TwitterException {
        
        GeoQuery geoQuery = null;
        try{
            
            geoQuery = new GeoQuery(ipAddress);

            ResponseList<Place> places = twitter.searchPlaces(geoQuery);

            if(places != null || !places.isEmpty()) {
                
                Place place = places.get(0);
                
XLogger.getInstance().log(Level.FINER, "Ip address: {0}, place: {1}, country: {2}", 
this.getClass(), ipAddress, place.getName(), place.getCountryCode());            

                GeoLocation [][] coords = place.getGeometryCoordinates();
                
                if(coords == null) {
                    
                    return this.defaultGeoLocation;
                }
                
                return coords[0][0];
                
            }else{
                
                return this.defaultGeoLocation;
            }
        }catch(TwitterException e) {
            StringBuilder msg = new StringBuilder("Exception encountered searching places for ipAddress: ");
            msg.append(ipAddress).append(", using GeoQuery: ").append(geoQuery);
            // We don't log the whole stack trace
            msg.append(' ').append(e);
            
            //////////////////////////////////////////////////////
            // This error occurs very often, so I commented it out
            //
//            logger.log(Level.WARNING, msg.toString());
            return this.defaultGeoLocation;
        }  
    }

    public List<Trend> getLocationTrends(String ipAddress) throws TwitterException {

        GeoLocation geoLocation = this.getGeoLocation(ipAddress);

        return this.getLocationTrends(geoLocation);
    }
    
    public List<Trend> getLocationTrends(GeoLocation geoLocation) throws TwitterException {
            
        ArrayList<Trend> localTrends = null;

        ResponseList<Location> locs = null;
        
        try{
            if(geoLocation != null) {
                locs = twitter.getClosestTrends(geoLocation);
            }
        }catch(NoSuchMethodError ignored) { }    
        
        if(locs == null || locs.isEmpty()) {
            locs = twitter.getAvailableTrends();
        }

        if(locs != null && !locs.isEmpty()) {
            
            Iterator<Location> iter = locs.iterator();

            do {

                if (!iter.hasNext()) break;

                Location loc = iter.next();
                
XLogger.getInstance().log(Level.FINE, "Country: {0}, Place: {1}, URL: {2}.", 
this.getClass(), loc.getCountryName(), loc.getPlaceName(), loc.getURL());

                String country = loc.getCountryName();

                if (country == null || !country.trim().equalsIgnoreCase("Nigeria")) {
                    continue;
                }

                Trends locationTrends = twitter.getPlaceTrends(loc.getWoeid());

                Trend trendsArr[] = locationTrends.getTrends();

                for (int i = 0; i < trendsArr.length; i++) {
                    Trend trend = trendsArr[i];
                    if(localTrends == null) {
                        localTrends = new ArrayList<Trend>();
                    }
                    localTrends.add(trend);
XLogger.getInstance().log(Level.FINER, "Trend: {0}", this.getClass(), trend);
                }
                break;
            } while (true);
        }

        if(localTrends == null) {
            localTrends = new ArrayList<Trend>();
        }
        
XLogger.getInstance().log(Level.FINER, "Local trends: {0}", this.getClass(), localTrends.size());        

        return localTrends;
    }

    public boolean publish(final String tweet, Set<String> att) {
        
XLogger.getInstance().log(Level.FINER, "Tweet: {0}. Attachements: {1}", this.getClass(), tweet, att);

        StatusUpdate statusUpdate = new StatusUpdate(tweet.toString());
        statusUpdate.setPlaceId(oAuth.getPlaceId());
        statusUpdate.setLocation(defaultGeoLocation);
        statusUpdate.setDisplayCoordinates(true);

        boolean output = false;
        InputStream in = null;
        try{
            if(att != null && !att.isEmpty()) {
                String first = att.iterator().next();
                try{
                    in = Util.getInputStream(first);
                    String name = Util.getFileName(first);
                    statusUpdate.setMedia(name, in);
                }catch(IOException e) {
                    XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                }
            }

            try {
                Status status = twitter.updateStatus(statusUpdate);
                output = status != null;
            }catch (TwitterException e) {
                if(e.getMessage() != null && e.getMessage().contains(
                "denied due to update limits")) {
                    XLogger.getInstance().logSimple(Level.WARNING, this.getClass(), e);
                }else{
                    XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
                }
            }
        }finally{
            if(in != null) {
                try{
                    in.close();
                }catch(IOException ignored) { }
            }
        }    
        return output;
    }

    public Twitter getTwitter() {
        return twitter;
    }

    private String getConsumerKey() {
        return this.oAuth.getProperties().getProperty(OAuthProperties.KEY);
    }

    private String getConsumerSecret() {
        return this.oAuth.getProperties().getProperty(OAuthProperties.SECRET);
    }
}
