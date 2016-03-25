package com.idisc.core.twitter;

import com.bc.oauth.AbstractOAuthProperties;
import com.idisc.core.IdiscApp;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;












public class TwitterOAuthProperties
  extends AbstractOAuthProperties
  implements TwitterProperties
{
  public static final String PROPERTIES_NAME = "twitter";
  private Properties props;
  
  public Properties getProperties()
  {
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
  
  public String getPlaceId()
  {
    return getProperties().getProperty("placeId");
  }
  
  public String getTrendingItem()
  {
    return getProperties().getProperty("trendingItem");
  }
  
  public double getLatitude()
  {
    String lat = getProperties().getProperty("latitude");
    return Double.parseDouble(lat);
  }
  
  public double getLongitude()
  {
    String lng = getProperties().getProperty("longitude");
    return Double.parseDouble(lng);
  }
}
