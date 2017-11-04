package com.idisc.core.extraction.facebook;

import com.bc.oauth.OAuthProperties;
import com.bc.util.QueryParametersConverter;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.SocialClient;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.WebRequestor;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.scribe.model.Token;

public class FacebookClient extends SocialClient<OAuthProperties> {
    
  private DefaultFacebookClient facebookClient;
  
  public FacebookClient() {
    this(new FacebookOAuthProperties());
  }
  
  public FacebookClient(FacebookOAuthProperties oAuth) {
    super(oAuth);
    this.facebookClient = new DefaultFacebookClient(oAuth.getAccessToken());
  }
  
  public void updateAccessToken(Token accessToken) throws IOException {
      
    Objects.requireNonNull(accessToken);
    
    String tokenValue = accessToken.getToken();
    
    updateAccessToken(tokenValue);
  }
  
  private synchronized void updateAccessToken(String accessToken) throws IOException {
      
    Objects.requireNonNull(accessToken);
    
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    Configuration facebook_cfg = config.subset("facebook");
    
    facebook_cfg.setProperty("accessToken", accessToken);
    
    IdiscApp.getInstance().saveConfig();
    
    this.facebookClient = new DefaultFacebookClient(accessToken);
    XLogger.getInstance().log(Level.FINE, "Successfully saved access token and created Default Facebook Client", getClass());
  }
  
  public void refreshToken() throws IOException {
      
    WebRequestor request = this.facebookClient.getWebRequestor();
    
    HashMap<String, Object> parameterMap = new HashMap<>();
    
    parameterMap.put("grant_type", "fb_exchange_token");
    parameterMap.put("fb_exchange_token", this.getoAuthProperties().getAccessToken());
    
    QueryParametersConverter converter = new QueryParametersConverter();
    final String query = converter.convert(parameterMap);
    
    XLogger.getInstance().log(Level.INFO, "@refreshToken. Request parameters: {0}", getClass(), query);
    
    WebRequestor.Response response = request.executePost(this.getoAuthProperties().getAccessTokenURL() + '?', query);
    
    XLogger.getInstance().log(Level.INFO, "@refreshToken. Response body: {0}", getClass(), response.getBody());
    
    final String responseBody = response.getBody();
    final Map<String, String> responseParams;
    if(responseBody.contains("&amp;")) {
        responseParams = converter.toMap(responseBody, "&amp;");
    }else if(responseBody.contains("&")) {
        responseParams = converter.toMap(responseBody, "&");
    }else{
        responseParams = null;
    }
    
    final String accessToken = responseParams == null ? null : responseParams.get("access_token");
    
    if (accessToken != null) {
      updateAccessToken(accessToken);
    }
  }
  
  @Override
  public boolean publish(String message, Set<String> attachments) {
      
    publishFeed(message);
    
    Parameter[] p = new Parameter[0];
    
    try {
        
      for (String imagePath : attachments) {
          
        publishPhoto(imagePath, p);
      }
    } catch (FileNotFoundException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e.toString());
      return false;
    }
    
    return true;
  }
  
  public void publishFeed(String message) throws FacebookOAuthException {
      
    FacebookType msgResponse = (FacebookType)this.facebookClient.publish("me/feed", FacebookType.class, new Parameter[] { Parameter.with("message", message) });
    
    XLogger.getInstance().log(Level.FINER, "Published feed response Id: {0}, response type: {1}", getClass(), msgResponse.getId(), msgResponse.getType());
  }
  
  public void publishPhoto(String path, Parameter... parameters)
    throws FileNotFoundException, FacebookOAuthException {
      
    InputStream in = null;
    try {
        
      in = this.getInputStream(path);
      
      final String fileName = Paths.get(path).getFileName().toString();
      
      publishPhoto(in, fileName, parameters); 
      
    } catch (IOException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    } finally {
      if (in != null) { 
        try { 
          in.close();
        }catch (IOException e) {}
      }
    }
  }
  
  public void publishPhoto(InputStream in, String name, Parameter... parameters) 
      throws FacebookOAuthException {
      
    FacebookType msgResponse = (FacebookType)this.facebookClient.publish("me/photos", FacebookType.class, BinaryAttachment.with(name, in), parameters);
    
    XLogger.getInstance().log(Level.FINER, "Published photo response Id: {0}, response type: {1}", 
            getClass(), msgResponse.getId(), msgResponse.getType());
  }
  
  public DefaultFacebookClient getDefaultClient() {
    return this.facebookClient;
  }
}
