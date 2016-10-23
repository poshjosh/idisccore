/*
 * Copyright 2016 NUROX Ltd.
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

package com.idisc.core.util;

import com.bc.webdatex.converter.Converter;
import com.bc.webdatex.converter.DateTimeConverter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author Chinomso Bassey Ikwuagwu on Sep 30, 2016 9:32:01 PM
 */
public class TimeZones {
    
    public static final String UTC_ZONEID = "Etc/UTC";
    
    public Date getCurrentTimeInDatabaseTimeZone() {
        Converter<Date, Date> converter = new DateTimeConverter(
                TimeZone.getDefault(), TimeZone.getTimeZone(this.getDatabaseTimeZoneId()));
        return converter.convert(new Date());
    }
    
    public String getDatabaseTimeZoneId() {
        return UTC_ZONEID;
    }

    public String getTimeZoneIdForLocale(Locale locale, String outputIfNone) {
        
        final String countryCode = locale.getISO3Country();
        
        return this.getTimeZoneIdForCountryCode(countryCode, outputIfNone);
    }

    public String getTimeZoneIdForCountryCode(String countryCode, String outputIfNone) {
        String output;
        switch(countryCode) {
            case "NG":
            case "NGA":
                output = "Africa/Lagos"; break;
            case "IN":
            case "IND":
                output = "Asia/Kolkata"; break;
            default:
                output = outputIfNone;
        }
        return output;
    }
}

