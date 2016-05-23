package org.scribe.services;

//import org.apache.commons.codec.binary.*;
import org.scribe.exceptions.*;

//import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;

public class CommonsEncoder extends Base64Encoder
{

  @Override
  public String encode(byte[] bytes)
  {
    try
    { 
      Class cls = Class.forName("org.apache.commons.codec.binary.Base64");  
      Method mtd = cls.getMethod("encodeBase64", byte[].class);
      return new String((byte[])mtd.invoke(null, bytes), "UTF-8");
//      return new String(Base64.encodeBase64(bytes), "UTF-8");
    }
    catch (Exception e)
    {
//    catch (UnsupportedEncodingException e)
//    {
      throw new OAuthSignatureException("Can't perform base64 encoding", e);
    }
  }

  @Override
  public String getType()
  {
    return "CommonsCodec";
  }

  public static boolean isPresent()
  {
    try
    {
      Class.forName("org.apache.commons.codec.binary.Base64");
      return true;
    }
    catch (ClassNotFoundException e)
    {
      return false;
    }
  }
}
