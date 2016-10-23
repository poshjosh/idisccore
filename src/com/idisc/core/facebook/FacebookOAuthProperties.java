package com.idisc.core.facebook;

import com.bc.oauth.AbstractOAuthProperties;
import com.idisc.core.IdiscApp;
import java.util.Iterator;
import java.util.Properties;
import org.apache.commons.configuration.Configuration;

public class FacebookOAuthProperties extends AbstractOAuthProperties {
    
  public static final String PROPERTIES_NAME = "facebook";
  private Properties props;
  
  @Override
  public Properties getProperties() {
    if (this.props == null) {
      Configuration config = IdiscApp.getInstance().getConfiguration();
      Configuration subset = config.subset("facebook");
      this.props = new Properties();
      Iterator<String> keys = subset.getKeys();
      while (keys.hasNext()) {
        String key = (String)keys.next();
        this.props.put(key, subset.getProperty(key));
      }
    }
    return this.props;
  }
}
