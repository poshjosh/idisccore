package com.idisc.core;

import com.bc.util.XLogger;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Country;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Gender;
import com.idisc.pu.entities.Howdidyoufindus;
import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.exceptions.PreexistingEntityException;
import com.idisc.pu.entities.Installation;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;


/**
 * @(#)User.java   22-Jan-2015 14:08:38
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * A combination of both auth user details and feed user
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class User extends Feeduser {

    private Map authdetails;
    private Feeduser delegate;
    
    public User() {}

    public static User getInstance(
            Map authdetails, boolean create) 
            throws PreexistingEntityException, Exception{
        
        User output = new User();
        
        ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
        EntityController<Feeduser, Integer> ec = 
                factory.getEntityController(Feeduser.class, Integer.class);

        Feeduser feeduser = new Feeduser();

        // We set this so we can call #getXXX methods
        //
        output.authdetails = authdetails;
        String email = output.getAuthEmailaddress();
        feeduser.setEmailAddress(email);
        
        Map where = ec.toMap(feeduser, false);
        
        List<Feeduser> found = ec.select(where, "AND");
        if(found.isEmpty()) {
            if(create) {
                feeduser.setDatecreated(new Date());
                try{
                    ec.create(feeduser);
                    output.authdetails = authdetails;
                    output.delegate = feeduser;
                }catch(Exception e) {
                    output = null;
                    throw e;
                }
            }else{
                output = null;
            }
        }else if(found.size() == 1) {
            feeduser = found.get(0);
            output.authdetails = authdetails;
            output.delegate = feeduser;
        }else{
            output = null;
            throw new UnsupportedOperationException(
            "Found > 1 records where 1 or less was expected, entity: "+
            Feeduser.class.getName()+", parameters: "+authdetails);
        }
        return output;
    }
    
    public Map getDetails() {
        ControllerFactory cf = IdiscApp.getInstance().getControllerFactory();
        EntityController<Feeduser, Integer> feedUserCtrl = cf.getEntityController(Feeduser.class, Integer.class);
        Map feeduserdetails = feedUserCtrl.toMap(delegate, false);
        feeduserdetails.put("commentList", null);
        feeduserdetails.put("feedhitList", null);
XLogger.getInstance().log(Level.FINER, "Feeduser details: {0}", this.getClass(), feeduserdetails);
        EntityController<Installation, Integer> instlCtrl = cf.getEntityController(Installation.class, Integer.class);
        Installation installation = instlCtrl.selectFirst("feeduserid", delegate);
        Map instldetails;
        if(installation != null) {
            instldetails = instlCtrl.toMap(installation, false);
            instldetails.put("extractedemailList", null);
            instldetails.put("feedhitList", null); 
            instldetails.put("usersitehitcountList", null);
        }else{
            instldetails = null;
        }
XLogger.getInstance().log(Level.FINER, "Installation details: {0}", this.getClass(), instldetails);
XLogger.getInstance().log(Level.FINER, "Authentication details: {0}", this.getClass(), this.authdetails);
        Map output = new HashMap(30, 1.0f);
        output.putAll(this.authdetails);
        if(instldetails != null) {
            output.putAll(instldetails);
        }
        output.putAll(feeduserdetails);
        return output;
    }
    
    public Map getAuthdetails() {
        return authdetails;
    }

    public void setAuthdetails(Map authdetails) {
        this.authdetails = authdetails;
    }

    public Object getAppuserid() {
        return authdetails.get("appuserid");
    }

    public void setAppuserid(Object appuserid) {
        authdetails.put("appuserid", appuserid);
    }

    public String getAuthEmailaddress() {
        return (String)authdetails.get("emailaddress");
    }

    public void setAuthEmailaddress(String emailaddress) {
        authdetails.put("emailaddress", emailaddress);
    }

    public String getPassword() {
        return (String)authdetails.get("password");
    }

    public void setPassword(String password) {
        authdetails.put("password", password);
    }

    public Object getAppid() {
        return authdetails.get("appid");
    }

    public void setAppid(Object appid) {
        authdetails.put("appid", appid);
    }

    public String getUsername() {
        return (String)authdetails.get("username");
    }

    public void setUsername(String username) {
        authdetails.put("username", username);
    }

    public Object getUserstatus() {
        return authdetails.get("userstatus");
    }

    public void setUserstatus(Object userstatus) {
        authdetails.put("userstatus", userstatus);
    }
    
    @Override
    public Integer getFeeduserid() {
        return delegate.getFeeduserid();
    }

    @Override
    public void setFeeduserid(Integer feeduserid) {
        delegate.setFeeduserid(feeduserid);
    }

    @Override
    public String getEmailAddress() {
        return delegate.getEmailAddress();
    }

    @Override
    public void setEmailAddress(String emailAddress) {
        delegate.setEmailAddress(emailAddress);
    }

    @Override
    public String getLastName() {
        return delegate.getLastName();
    }

    @Override
    public void setLastName(String lastName) {
        delegate.setLastName(lastName);
    }

    @Override
    public String getFirstName() {
        return delegate.getFirstName();
    }

    @Override
    public void setFirstName(String firstName) {
        delegate.setFirstName(firstName);
    }

    @Override
    public Date getDateOfBirth() {
        return delegate.getDateOfBirth();
    }

    @Override
    public void setDateOfBirth(Date dateOfBirth) {
        delegate.setDateOfBirth(dateOfBirth);
    }

    @Override
    public String getPhoneNumber1() {
        return delegate.getPhoneNumber1();
    }

    @Override
    public void setPhoneNumber1(String phoneNumber1) {
        delegate.setPhoneNumber1(phoneNumber1);
    }

    @Override
    public String getPhoneNumber2() {
        return delegate.getPhoneNumber2();
    }

    @Override
    public void setPhoneNumber2(String phoneNumber2) {
        delegate.setPhoneNumber2(phoneNumber2);
    }

    @Override
    public String getFax() {
        return delegate.getFax();
    }

    @Override
    public void setFax(String fax) {
        delegate.setFax(fax);
    }

    @Override
    public String getStateOrRegion() {
        return delegate.getStateOrRegion();
    }

    @Override
    public void setStateOrRegion(String stateOrRegion) {
        delegate.setStateOrRegion(stateOrRegion);
    }

    @Override
    public String getCity() {
        return delegate.getCity();
    }

    @Override
    public void setCity(String city) {
        delegate.setCity(city);
    }

    @Override
    public String getCounty() {
        return delegate.getCounty();
    }

    @Override
    public void setCounty(String county) {
        delegate.setCounty(county);
    }

    @Override
    public String getStreetAddress() {
        return delegate.getStreetAddress();
    }

    @Override
    public void setStreetAddress(String streetAddress) {
        delegate.setStreetAddress(streetAddress);
    }

    @Override
    public String getPostalCode() {
        return delegate.getPostalCode();
    }

    @Override
    public void setPostalCode(String postalCode) {
        delegate.setPostalCode(postalCode);
    }

    @Override
    public String getImage1() {
        return delegate.getImage1();
    }

    @Override
    public void setImage1(String image1) {
        delegate.setImage1(image1);
    }

    @Override
    public Date getDatecreated() {
        return delegate.getDatecreated();
    }

    @Override
    public void setDatecreated(Date datecreated) {
        delegate.setDatecreated(datecreated);
    }

    @Override
    public Date getTimemodified() {
        return delegate.getTimemodified();
    }

    @Override
    public void setTimemodified(Date timemodified) {
        delegate.setTimemodified(timemodified);
    }

    @Override
    public String getExtradetails() {
        return delegate.getExtradetails();
    }

    @Override
    public void setExtradetails(String extradetails) {
        delegate.setExtradetails(extradetails);
    }

    @Override
    public Gender getGender() {
        return delegate.getGender();
    }

    @Override
    public void setGender(Gender gender) {
        delegate.setGender(gender);
    }

    @Override
    public Country getCountry() {
        return delegate.getCountry();
    }

    @Override
    public void setCountry(Country country) {
        delegate.setCountry(country);
    }

    @Override
    public Howdidyoufindus getHowDidYouFindUs() {
        return delegate.getHowDidYouFindUs();
    }

    @Override
    public void setHowDidYouFindUs(Howdidyoufindus howDidYouFindUs) {
        delegate.setHowDidYouFindUs(howDidYouFindUs);
    }

    @Override
    public List<Comment> getCommentList() {
        return delegate.getCommentList();
    }

    @Override
    public void setCommentList(List<Comment> commentList) {
        delegate.setCommentList(commentList);
    }

    public Feeduser getDelegate() {
        return delegate;
    }

    public void setDelegate(Feeduser delegate) {
        this.delegate = delegate;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        return delegate.equals(object);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
