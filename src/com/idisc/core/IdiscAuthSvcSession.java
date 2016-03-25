package com.idisc.core;

import java.util.Map;


/**
 * @(#)AuthSvc.java   17-Jan-2015 15:17:57
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public class IdiscAuthSvcSession extends com.authsvc.client.AuthSvcSession {

    public IdiscAuthSvcSession() { }

    public IdiscAuthSvcSession(String target) {
        super(target);
    }

    public IdiscAuthSvcSession(String target, int maxTrials, long retrialIntervals) {
        super(target, maxTrials, retrialIntervals);
    }
    
    private transient final IOWrapper<Map> t_accessViaGetter = 
            new IOWrapper<>(null, "com.idiscweb.authsvc.app.token");
    @Override
    public void setAppToken(Map tokenPair) {
        t_accessViaGetter.setTarget(tokenPair);
    }
    @Override
    public Map getAppToken() {
        return t_accessViaGetter.getTarget();
    }

    private transient final IOWrapper<Map> ad_accessViaGetter = 
            new IOWrapper<>(null, "com.idiscweb.authsvc.app.details");
    @Override
    public void setAppDetails(Map appDetails) {
        ad_accessViaGetter.setTarget(appDetails);
    }
    @Override
    public Map getAppDetails() {
        return ad_accessViaGetter.getTarget();
    }
}
