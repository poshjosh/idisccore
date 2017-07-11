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

package com.idisc.core;

import com.bc.jpa.JpaContext;
import com.bc.util.XLogger;
import com.idisc.pu.FeedSvc;
import com.idisc.pu.entities.Feed;
import java.util.Date;
import java.util.logging.Level;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 16, 2016 12:56:16 PM
 */
public class InsertFeedToDatabase extends FeedSvc implements FeedHandler {

    private final Date defaultDatecreated;
    
    public InsertFeedToDatabase(JpaContext jpaContext) {
        
        super(jpaContext);
        
        this.defaultDatecreated = new Date();
    }

    @Override
    public boolean process(Feed feed) {
        
        if(feed.getDatecreated() == null) {
            
            feed.setDatecreated(defaultDatecreated);
        }

        boolean created;
        try{

            created = this.createIfNoneExistsWithMatchingData(feed);

        }catch(Exception e) {

            created = false;

            XLogger.getInstance().log(Level.WARNING, "#doCall(). Caught exception: {0}", this.getClass(), e.toString());
        }
        
        return created;
    }
}
