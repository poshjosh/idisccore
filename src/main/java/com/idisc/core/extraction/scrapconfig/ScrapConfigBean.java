/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core.extraction.scrapconfig;

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 1:47:06 PM
 */
public class ScrapConfigBean implements ScrapConfig, Serializable {

    private TimeUnit timeUnit;
    
    private long timeout;
    
    private long timeoutPerSite;
    
    private int siteLimit;
    
    private boolean acceptDuplicateLinks;
    
    private int maxConcurrentUnits;
    
    private int crawlLimit;
    
    private int parseLimit;
    
    private int scrapLimit;
    
    private int maxFailsAllowed;
    
    private float tolerance;

    public ScrapConfigBean() { }
    
    public ScrapConfigBean(ScrapConfig scrapConfig) { 
        this.setTimeUnit(scrapConfig.getTimeUnit());
        this.setTimeout(scrapConfig.getTimeout());
        this.setTimeoutPerSite(scrapConfig.getTimeoutPerSite());
        this.setSiteLimit(scrapConfig.getScrapLimit());
        this.setAcceptDuplicateLinks(scrapConfig.isAcceptDuplicateLinks());
        this.setMaxConcurrentUnits(scrapConfig.getMaxConcurrentUnits());
        this.setCrawlLimit(scrapConfig.getCrawlLimit());
        this.setParseLimit(scrapConfig.getParseLimit());
        this.setScrapLimit(scrapConfig.getScrapLimit());
        this.setMaxFailsAllowed(scrapConfig.getMaxConcurrentUnits());
        this.setTolerance(scrapConfig.getTolerance());
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    public long getTimeoutPerSite() {
        return timeoutPerSite;
    }

    public void setTimeoutPerSite(long timeoutPerSite) {
        this.timeoutPerSite = timeoutPerSite;
    }

    @Override
    public int getSiteLimit() {
        return siteLimit;
    }

    public void setSiteLimit(int siteLimit) {
        this.siteLimit = siteLimit;
    }

    @Override
    public boolean isAcceptDuplicateLinks() {
        return acceptDuplicateLinks;
    }

    public void setAcceptDuplicateLinks(boolean acceptDuplicateLinks) {
        this.acceptDuplicateLinks = acceptDuplicateLinks;
    }

    @Override
    public int getMaxConcurrentUnits() {
        return maxConcurrentUnits;
    }

    public void setMaxConcurrentUnits(int maxConcurrentUnits) {
        this.maxConcurrentUnits = maxConcurrentUnits;
    }

    @Override
    public int getCrawlLimit() {
        return crawlLimit;
    }

    public void setCrawlLimit(int crawlLimit) {
        this.crawlLimit = crawlLimit;
    }

    @Override
    public int getParseLimit() {
        return parseLimit;
    }

    public void setParseLimit(int parseLimit) {
        this.parseLimit = parseLimit;
    }

    @Override
    public int getScrapLimit() {
        return scrapLimit;
    }

    public void setScrapLimit(int scrapLimit) {
        this.scrapLimit = scrapLimit;
    }

    @Override
    public int getMaxFailsAllowed() {
        return maxFailsAllowed;
    }

    public void setMaxFailsAllowed(int maxFailsAllowed) {
        this.maxFailsAllowed = maxFailsAllowed;
    }

    @Override
    public float getTolerance() {
        return tolerance;
    }

    public void setTolerance(float tolerance) {
        this.tolerance = tolerance;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + Objects.hashCode(this.timeUnit);
        hash = 37 * hash + (int) (this.timeout ^ (this.timeout >>> 32));
        hash = 37 * hash + (int) (this.timeoutPerSite ^ (this.timeoutPerSite >>> 32));
        hash = 37 * hash + this.siteLimit;
        hash = 37 * hash + (this.acceptDuplicateLinks ? 1 : 0);
        hash = 37 * hash + this.maxConcurrentUnits;
        hash = 37 * hash + this.crawlLimit;
        hash = 37 * hash + this.parseLimit;
        hash = 37 * hash + this.scrapLimit;
        hash = 37 * hash + this.maxFailsAllowed;
        hash = 37 * hash + Float.floatToIntBits(this.tolerance);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ScrapConfigBean other = (ScrapConfigBean) obj;
        if (this.timeout != other.timeout) {
            return false;
        }
        if (this.timeoutPerSite != other.timeoutPerSite) {
            return false;
        }
        if (this.siteLimit != other.siteLimit) {
            return false;
        }
        if (this.acceptDuplicateLinks != other.acceptDuplicateLinks) {
            return false;
        }
        if (this.maxConcurrentUnits != other.maxConcurrentUnits) {
            return false;
        }
        if (this.crawlLimit != other.crawlLimit) {
            return false;
        }
        if (this.parseLimit != other.parseLimit) {
            return false;
        }
        if (this.scrapLimit != other.scrapLimit) {
            return false;
        }
        if (this.maxFailsAllowed != other.maxFailsAllowed) {
            return false;
        }
        if (Float.floatToIntBits(this.tolerance) != Float.floatToIntBits(other.tolerance)) {
            return false;
        }
        if (this.timeUnit != other.timeUnit) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return super.toString() + "{timeUnit=" + timeUnit + ", timeout=" + timeout 
                + ", timeoutPerSite=" + timeoutPerSite + ", siteLimit=" + siteLimit + 
                ", acceptDuplicateLinks=" + acceptDuplicateLinks + ", maxConcurrentUnits=" + maxConcurrentUnits + 
                ", crawlLimit=" + crawlLimit + ", parseLimit=" + parseLimit + ", scrapLimit=" + scrapLimit + 
                ", maxFailsAllowed=" + maxFailsAllowed + ", tolerance=" + tolerance + '}';
    }
}
