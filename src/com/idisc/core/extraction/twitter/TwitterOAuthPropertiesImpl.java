package com.idisc.core.extraction.twitter;

import com.bc.oauth.AbstractOAuthProperties;
import com.idisc.core.IdiscApp;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

public class TwitterOAuthPropertiesImpl
  extends AbstractOAuthProperties
  implements TwitterOAuthProperties{
    
  public static final String PROPERTIES_NAME = "twitter";
  private Properties props;
  
  @Override
  public Properties getProperties() {
    if (this.props == null) {
      Configuration config = IdiscApp.getInstance().getConfiguration();
      Configuration subset = config.subset("twitter");
      this.props = new Properties();
      Iterator<String> keys = subset.getKeys();
      while (keys.hasNext()) {
        String key = (String)keys.next();
        this.props.put(key, subset.getProperty(key));
      }
    }
    return this.props;
  }
  
  @Override
  public String getPlaceId()
  {
    return getProperties().getProperty("placeId");
  }
  
  @Override
  public String getTrendingItem()
  {
    return getProperties().getProperty("trendingItem");
  }
  
  @Override
  public double getLatitude()
  {
    String lat = getProperties().getProperty("latitude");
    return Double.parseDouble(lat);
  }
  
  @Override
  public double getLongitude()
  {
    String lng = getProperties().getProperty("longitude");
    return Double.parseDouble(lng);
  }
}
