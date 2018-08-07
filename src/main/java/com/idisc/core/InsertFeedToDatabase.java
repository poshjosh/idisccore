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

import com.bc.jpa.context.JpaContext;
import com.idisc.pu.FeedDao;
import com.idisc.pu.entities.Feed;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 16, 2016 12:56:16 PM
 */
public class InsertFeedToDatabase extends FeedDao implements FeedHandler {

    private transient static final Logger LOG = Logger.getLogger(InsertFeedToDatabase.class.getName());

    private final Date defaultDatecreated;
    
    private final boolean onlyIfNoneExistsWithMatchingData;
    
    public InsertFeedToDatabase(JpaContext jpaContext) {
        this(jpaContext, false);
    }
    
    public InsertFeedToDatabase(JpaContext jpaContext, boolean onlyIfNoneExistsWithMatchingData) {
        
        super(jpaContext);
        
        this.onlyIfNoneExistsWithMatchingData = onlyIfNoneExistsWithMatchingData;
        
        this.defaultDatecreated = new Date();
    }

    @Override
    public boolean process(Feed feed) {
        
        if(feed.getDatecreated() == null) {
            
            feed.setDatecreated(defaultDatecreated);
        }

        boolean created;
        try{

            created = this.create(feed, this.onlyIfNoneExistsWithMatchingData);

        }catch(Exception e) {

            created = false;

            LOG.log(Level.WARNING, "Caught exception: {0}", e.toString());
            LOG.log(Level.FINE, "Stacktrace printed for debugging purposes", e);
        }
        
        return created;
    }
}
