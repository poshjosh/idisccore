package com.idisc.core.facebook;

import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.Util;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.WebRequestor;
import com.restfb.WebRequestor.Response;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.FacebookType;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import org.apache.commons.configuration.Configuration;
import org.scribe.model.Token;













public class FacebookClient
{
  private DefaultFacebookClient facebookClient;
  private final FacebookOAuthProperties oAuth;
  
  public FacebookClient()
  {
    this(new FacebookOAuthProperties());
  }
  
  public FacebookClient(FacebookOAuthProperties oAuth) {
    this.oAuth = oAuth;
    

    this.facebookClient = new DefaultFacebookClient(oAuth.getAccessToken());
  }
  

  public void updateAccessToken(Token accessToken)
    throws IOException
  {
    if (accessToken == null) { throw new NullPointerException();
    }
    String tokenValue = accessToken.getToken();
    


    updateAccessToken(tokenValue);
  }
  
  private synchronized void updateAccessToken(String accessToken) throws IOException
  {
    if (accessToken == null) { throw new NullPointerException();
    }
    Configuration config = IdiscApp.getInstance().getConfiguration();
    
    Configuration facebook_cfg = config.subset("facebook");
    
    facebook_cfg.setProperty("accessToken", accessToken);
    
    IdiscApp.getInstance().saveConfig();
    
    this.facebookClient = new DefaultFacebookClient(accessToken);
    XLogger.getInstance().log(Level.FINE, "Successfully saved access token and created Default Facebook Client", getClass());
  }
  
  public void refreshToken() throws IOException
  {
    WebRequestor request = this.facebookClient.getWebRequestor();
    
    HashMap<String, Object> parameterMap = new HashMap(4, 1.0F);
    

    parameterMap.put("grant_type", "fb_exchange_token");
    parameterMap.put("fb_exchange_token", this.oAuth.getAccessToken());
    
    StringBuilder query = new StringBuilder();
    Util.appendQuery(parameterMap, query);
    
    XLogger.getInstance().log(Level.INFO, "@refreshToken. Request parameters: {0}", getClass(), query);
    

    WebRequestor.Response response = request.executePost(this.oAuth.getAccessTokenURL() + "?", query.toString());
    

    XLogger.getInstance().log(Level.INFO, "@refreshToken. Response body: {0}", getClass(), response.getBody());
    

    Map<String, String> resParams = Util.getParameters(response.getBody(), "&amp;");
    if ((resParams == null) || (resParams.isEmpty())) {
      resParams = Util.getParameters(response.getBody(), "&");
    }
    String accessToken = (String)resParams.get("access_token");
    if (accessToken != null) {
      updateAccessToken(accessToken);
    }
  }
  




  public boolean publish(String message, Set<String> att)
  {
    publishFeed(message);
    
    Parameter[] p = new Parameter[0];
    
    try
    {
      for (String imagePath : att)
      {
        publishPhoto(imagePath, p);
      }
    } catch (FileNotFoundException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e.toString());
      return false;
    }
    
    return true;
  }
  

  public void publishFeed(String message)
    throws FacebookOAuthException
  {
    FacebookType msgResponse = (FacebookType)this.facebookClient.publish("me/feed", FacebookType.class, new Parameter[] { Parameter.with("message", message) });
    

    XLogger.getInstance().log(Level.FINER, "Published feed response Id: {0}, response type: {1}", getClass(), msgResponse.getId(), msgResponse.getType());
  }
  

  public void publishPhoto(String path, Parameter... parameters)
    throws FileNotFoundException, FacebookOAuthException
  {
    InputStream in = null;
    try
    {
      in = Util.getInputStream(path);
      
      publishPhoto(in, Util.getFileName(path), parameters); return;
    }
    catch (IOException e) {
      XLogger.getInstance().log(Level.WARNING, null, getClass(), e);
    } finally {
      if (in != null) try { in.close();
        }
        catch (IOException e) {}
    }
  }
  
  public void publishPhoto(InputStream in, String name, Parameter... parameters)
    throws FacebookOAuthException
  {
    FacebookType msgResponse = (FacebookType)this.facebookClient.publish("me/photos", FacebookType.class, BinaryAttachment.with(name, in), parameters);
    

    XLogger.getInstance().log(Level.FINER, "Published photo response Id: {0}, response type: {1}", getClass(), msgResponse.getId(), msgResponse.getType());
  }
  

  public DefaultFacebookClient getDefaultClient()
  {
    return this.facebookClient;
  }
}
