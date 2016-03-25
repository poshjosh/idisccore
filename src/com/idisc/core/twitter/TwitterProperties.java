package com.idisc.core.twitter;

public abstract interface TwitterProperties
{
  public static final String LATITUDE = "latitude";
  public static final String LONGITUDE = "longitude";
  public static final String PLACE_ID = "placeId";
  public static final String TRENDING_ITEM = "trendingItem";
  
  public abstract double getLatitude();
  
  public abstract double getLongitude();
  
  public abstract String getPlaceId();
  
  public abstract String getTrendingItem();
}
