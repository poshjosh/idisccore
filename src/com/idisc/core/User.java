package com.idisc.core;

import com.bc.jpa.ControllerFactory;
import com.bc.jpa.EntityController;
import com.bc.jpa.exceptions.PreexistingEntityException;
import com.bc.util.XLogger;
import com.idisc.core.util.EntityMapBuilder;
import com.idisc.pu.entities.Country;
import com.idisc.pu.entities.Feeduser;
import com.idisc.pu.entities.Gender;
import com.idisc.pu.entities.Howdidyoufindus;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class User
  extends Feeduser
{
  private Map authdetails;
  private Feeduser delegate;
  
  public static User getInstance(Map authdetails, boolean create)
    throws PreexistingEntityException, Exception
  {
    User output = new User();
    
    ControllerFactory factory = IdiscApp.getInstance().getControllerFactory();
    EntityController<Feeduser, Integer> ec = factory.getEntityController(Feeduser.class, Integer.class);
    
    Feeduser feeduser = new Feeduser();

    output.authdetails = authdetails;
    String email = output.getAuthEmailaddress();
    feeduser.setEmailAddress(email);
    
    Map where = ec.toMap(feeduser, false);
    
    List<Feeduser> found = ec.select(where, "AND");
    if (found.isEmpty()) {
      if (create) {
XLogger.getInstance().log(Level.FINER, "Creating user: {0}", User.class, feeduser);
        feeduser.setDatecreated(new Date());
        try {
          ec.create(feeduser);
XLogger.getInstance().log(Level.FINE, "Created user: {0}", User.class, feeduser);
          output.authdetails = authdetails;
          output.delegate = feeduser;
        } catch (Exception e) {
          output = null;
          throw e;
        }
      } else {
        output = null;
      }
    } else if (found.size() == 1) {
      feeduser = (Feeduser)found.get(0);
      output.authdetails = authdetails;
      output.delegate = feeduser;
    } else {
      output = null;
      throw new UnsupportedOperationException("Found > 1 records where 1 or less was expected, entity: " + Feeduser.class.getName() + ", parameters: " + authdetails);
    }
XLogger.getInstance().log(Level.FINE, "User: {0}", User.class, output);

    return output;
  }
  
  public Map getDetails() {
    XLogger.getInstance().log(Level.FINER, "Authentication details: {0}", getClass(), this.authdetails);
    Map output = new HashMap(40, 0.75F);
    output.putAll(this.authdetails);
    Map feeduserdetails = new EntityMapBuilder().toMap(this.delegate);
    output.putAll(feeduserdetails);
    return output;
  }
  
  public Map getAuthdetails() {
    return this.authdetails;
  }
  
  public void setAuthdetails(Map authdetails) {
    this.authdetails = authdetails;
  }
  
  public Object getAppuserid() {
    return this.authdetails.get("appuserid");
  }
  
  public void setAppuserid(Object appuserid) {
    this.authdetails.put("appuserid", appuserid);
  }
  
  public String getAuthEmailaddress() {
    return (String)this.authdetails.get("emailaddress");
  }
  
  public void setAuthEmailaddress(String emailaddress) {
    this.authdetails.put("emailaddress", emailaddress);
  }
  
  public String getPassword() {
    return (String)this.authdetails.get("password");
  }
  
  public void setPassword(String password) {
    this.authdetails.put("password", password);
  }
  
  public Object getAppid() {
    return this.authdetails.get("appid");
  }
  
  public void setAppid(Object appid) {
    this.authdetails.put("appid", appid);
  }
  
  public String getUsername() {
    return (String)this.authdetails.get("username");
  }
  
  public void setUsername(String username) {
    this.authdetails.put("username", username);
  }
  
  public Object getUserstatus() {
    return this.authdetails.get("userstatus");
  }
  
  public void setUserstatus(Object userstatus) {
    this.authdetails.put("userstatus", userstatus);
  }
  
  public Integer getFeeduserid()
  {
    return this.delegate.getFeeduserid();
  }
  
  public void setFeeduserid(Integer feeduserid)
  {
    this.delegate.setFeeduserid(feeduserid);
  }
  
  public String getEmailAddress()
  {
    return this.delegate.getEmailAddress();
  }
  
  public void setEmailAddress(String emailAddress)
  {
    this.delegate.setEmailAddress(emailAddress);
  }
  
  public String getLastName()
  {
    return this.delegate.getLastName();
  }
  
  public void setLastName(String lastName)
  {
    this.delegate.setLastName(lastName);
  }
  
  public String getFirstName()
  {
    return this.delegate.getFirstName();
  }
  
  public void setFirstName(String firstName)
  {
    this.delegate.setFirstName(firstName);
  }
  
  public Date getDateOfBirth()
  {
    return this.delegate.getDateOfBirth();
  }
  
  public void setDateOfBirth(Date dateOfBirth)
  {
    this.delegate.setDateOfBirth(dateOfBirth);
  }
  
  public String getPhoneNumber1()
  {
    return this.delegate.getPhoneNumber1();
  }
  
  public void setPhoneNumber1(String phoneNumber1)
  {
    this.delegate.setPhoneNumber1(phoneNumber1);
  }
  
  public String getPhoneNumber2()
  {
    return this.delegate.getPhoneNumber2();
  }
  
  public void setPhoneNumber2(String phoneNumber2)
  {
    this.delegate.setPhoneNumber2(phoneNumber2);
  }
  
  public String getFax()
  {
    return this.delegate.getFax();
  }
  
  public void setFax(String fax)
  {
    this.delegate.setFax(fax);
  }
  
  public String getStateOrRegion()
  {
    return this.delegate.getStateOrRegion();
  }
  
  public void setStateOrRegion(String stateOrRegion)
  {
    this.delegate.setStateOrRegion(stateOrRegion);
  }
  
  public String getCity()
  {
    return this.delegate.getCity();
  }
  
  public void setCity(String city)
  {
    this.delegate.setCity(city);
  }
  
  public String getCounty()
  {
    return this.delegate.getCounty();
  }
  
  public void setCounty(String county)
  {
    this.delegate.setCounty(county);
  }
  
  public String getStreetAddress()
  {
    return this.delegate.getStreetAddress();
  }
  
  public void setStreetAddress(String streetAddress)
  {
    this.delegate.setStreetAddress(streetAddress);
  }
  
  public String getPostalCode()
  {
    return this.delegate.getPostalCode();
  }
  
  public void setPostalCode(String postalCode)
  {
    this.delegate.setPostalCode(postalCode);
  }
  
  public String getImage1()
  {
    return this.delegate.getImage1();
  }
  
  public void setImage1(String image1)
  {
    this.delegate.setImage1(image1);
  }
  
  public Date getDatecreated()
  {
    return this.delegate.getDatecreated();
  }
  
  public void setDatecreated(Date datecreated)
  {
    this.delegate.setDatecreated(datecreated);
  }
  
  public Date getTimemodified()
  {
    return this.delegate.getTimemodified();
  }
  
  public void setTimemodified(Date timemodified)
  {
    this.delegate.setTimemodified(timemodified);
  }
  
  public String getExtradetails()
  {
    return this.delegate.getExtradetails();
  }
  
  public void setExtradetails(String extradetails)
  {
    this.delegate.setExtradetails(extradetails);
  }
  
  public Gender getGender()
  {
    return this.delegate.getGender();
  }
  
  public void setGender(Gender gender)
  {
    this.delegate.setGender(gender);
  }
  
  public Country getCountry()
  {
    return this.delegate.getCountry();
  }
  
  public void setCountry(Country country)
  {
    this.delegate.setCountry(country);
  }
  
  public Howdidyoufindus getHowDidYouFindUs()
  {
    return this.delegate.getHowDidYouFindUs();
  }
  
  public void setHowDidYouFindUs(Howdidyoufindus howDidYouFindUs)
  {
    this.delegate.setHowDidYouFindUs(howDidYouFindUs);
  }
  
  public Feeduser getDelegate() {
    return this.delegate;
  }
  
  public void setDelegate(Feeduser delegate) {
    this.delegate = delegate;
  }
  
  public int hashCode()
  {
    return this.delegate.hashCode();
  }
  
  public boolean equals(Object object)
  {
    return this.delegate.equals(object);
  }
  
  public String toString()
  {
    return this.delegate.toString();
  }
}
