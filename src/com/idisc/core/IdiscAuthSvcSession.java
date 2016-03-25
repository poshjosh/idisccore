package com.idisc.core;

import com.authsvc.client.AuthSvcSession;
import java.util.Map;












public class IdiscAuthSvcSession
  extends AuthSvcSession
{
  public IdiscAuthSvcSession() {}
  
  public IdiscAuthSvcSession(String target)
  {
    super(target);
  }
  
  public IdiscAuthSvcSession(String target, int maxTrials, long retrialIntervals) {
    super(target, maxTrials, retrialIntervals);
  }
  
  private final transient IOWrapper<Map> t_accessViaGetter = new IOWrapper(null, "com.idiscweb.authsvc.app.token");
  
  public void setAppToken(Map tokenPair)
  {
    this.t_accessViaGetter.setTarget(tokenPair);
  }
  
  public Map getAppToken() {
    return (Map)this.t_accessViaGetter.getTarget();
  }
  
  private final transient IOWrapper<Map> ad_accessViaGetter = new IOWrapper(null, "com.idiscweb.authsvc.app.details");
  
  public void setAppDetails(Map appDetails)
  {
    this.ad_accessViaGetter.setTarget(appDetails);
  }
  
  public Map getAppDetails() {
    return (Map)this.ad_accessViaGetter.getTarget();
  }
}
