package com.idisc.core.facebook;

import com.bc.oauth.OAuthProperties;
import com.bc.util.XLogger;
import com.idisc.core.IdiscApp;
import com.idisc.core.Util;
import com.restfb.BinaryAttachment;
import com.restfb.DefaultFacebookClient;
import com.restfb.Parameter;
import com.restfb.WebRequestor;
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

/**
 * @(#)FacebookClient.java   17-Oct-2014 22:39:27
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
public class FacebookClient {
    
    private DefaultFacebookClient facebookClient;
    
    private final FacebookOAuthProperties oAuth;
    
    public FacebookClient() {
        this(new FacebookOAuthProperties());
    }
    
    public FacebookClient(FacebookOAuthProperties oAuth) {
        this.oAuth = oAuth;
// @todo test this        
//        this.facebookClient = new DefaultFacebookClient(oAuth.getAccessToken(), oAuth.getAccessTokenSecret());    
        this.facebookClient = new DefaultFacebookClient(oAuth.getAccessToken());    
    }
    
//////////////////////////////////////////////////////////
    
    public void updateAccessToken(Token accessToken) throws IOException {
        
        if(accessToken == null) throw new NullPointerException();
        
        String tokenValue = accessToken.getToken();
//        String tokenSecret = accessToken.getSecret(); // Usually an empty string
//logger.log(Level.INFO, "{0}. Token value: {1}, Token secret: {2}", new Object[]{logger.getName(), tokenValue, tokenSecret});        

        this.updateAccessToken(tokenValue);
    }
    
    private synchronized void updateAccessToken(String accessToken) throws IOException {
        
        if(accessToken == null) throw new NullPointerException();
        
        Configuration config = IdiscApp.getInstance().getConfiguration();
        
        Configuration facebook_cfg = config.subset(FacebookOAuthProperties.PROPERTIES_NAME);
        
        facebook_cfg.setProperty(OAuthProperties.ACCESS_TOKEN, accessToken);
        
        IdiscApp.getInstance().saveConfig();

        this.facebookClient = new DefaultFacebookClient(accessToken);    
XLogger.getInstance().log(Level.FINE, "Successfully saved access token and created Default Facebook Client", this.getClass());        
    }

    public void refreshToken() throws IOException {
        
        WebRequestor request = facebookClient.getWebRequestor();
        
        HashMap<String, Object> parameterMap = new HashMap<>(4, 1.0f);
//        parameterMap.put(OAuthConstants.CLIENT_ID, getAppId());
//        parameterMap.put(OAuthConstants.CLIENT_SECRET, getAppSecret());
        parameterMap.put("grant_type", "fb_exchange_token");
        parameterMap.put("fb_exchange_token", oAuth.getAccessToken());
        
        StringBuilder query = new StringBuilder();
        Util.appendQuery(parameterMap, query);
        
XLogger.getInstance().log(Level.INFO, "@refreshToken. Request parameters: {0}", 
        this.getClass(), query);

        WebRequestor.Response response = request.executePost(
                oAuth.getAccessTokenURL()+"?", query.toString());

XLogger.getInstance().log(Level.INFO, "@refreshToken. Response body: {0}", 
        this.getClass(), response.getBody());

        Map<String, String> resParams = Util.getParameters(response.getBody(), "&amp;");
        if(resParams == null || resParams.isEmpty()) {
            resParams = Util.getParameters(response.getBody(), "&");
        }
        String accessToken = resParams.get("access_token");
        if(accessToken != null) {
            this.updateAccessToken(accessToken);
        }
    }
    
///////////////////////////////////////////////////////////    
    
    public boolean publish(String message, Set<String> att) {

        // This determines whether we are successful even if
        // the photo is not successfully published
        this.publishFeed(message);
        
        Parameter [] p = {}; // No parameters
        
        try{
            
            for(String imagePath:att) {
                
                this.publishPhoto(imagePath, p);
            }
        }catch(FileNotFoundException e) {
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e.toString());
            return false;
        }
        
        return true;
    }

    public void publishFeed(String message) throws FacebookOAuthException {
        
        // Publishing a simple message.
        // FacebookType represents any Facebook Graph Object that has an ID property.
        FacebookType msgResponse = facebookClient.publish("me/feed", 
        FacebookType.class, Parameter.with("message", message));
        
XLogger.getInstance().log(Level.FINER, "Published feed response Id: {0}, response type: {1}", 
this.getClass(), msgResponse.getId(), msgResponse.getType());
    }
    
    public void publishPhoto(String path, Parameter... parameters) 
            throws FileNotFoundException, FacebookOAuthException {
    
        InputStream in = null;
        try{
            
            in = Util.getInputStream(path);
            
            publishPhoto(in, Util.getFileName(path), parameters);
            
        }catch(IOException e) {    
            XLogger.getInstance().log(Level.WARNING, null, this.getClass(), e);
        }finally{
            if(in != null) try{ in.close(); }catch(IOException e) { }
        }
    }     
        
    public void publishPhoto(InputStream in, String name, 
            Parameter... parameters) throws FacebookOAuthException {
        
        // Publishing an image to a photo album is easy!
        // Just specify the image you'd like to upload and RestFB will handle it from there.
        FacebookType msgResponse = facebookClient.publish("me/photos", FacebookType.class, 
        BinaryAttachment.with(name, in), parameters);
        
XLogger.getInstance().log(Level.FINER, 
        "Published photo response Id: {0}, response type: {1}", 
        this.getClass(), msgResponse.getId(), msgResponse.getType());
    }
    
    public DefaultFacebookClient getDefaultClient() {
        return this.facebookClient;
    }
}
