/*
 * Copyright 2017 NUROX Ltd.
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

package com.idisc.core.extraction;

import com.bc.webcrawler.ResumeHandler;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 6:43:06 PM
 */
public class FeedDownloadResumeHandler implements ResumeHandler, AutoCloseable {

    private static final Logger logger = Logger.getLogger(FeedDownloadResumeHandler.class.getName());

    private final EntityManager entityManager;

    public FeedDownloadResumeHandler(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(entityManager);
    }
    
    @Override
    public void close() {
        if(this.entityManager.isOpen()) {
          this.entityManager.close();
        }
    }

    @Override
    public boolean isExisting(String name) {
        boolean found;
        try{
            final String col = Feed_.url.getName();
            TypedQuery<String> query = this.entityManager.createQuery("SELECT f."+col+" FROM "+Feed.class.getSimpleName()+" f WHERE f."+col+" = :"+col, String.class);
            query.setParameter(col, name);
            query.setFirstResult(0).setMaxResults(1);
            found = query.getSingleResult() != null;
        }catch(NoResultException e) {
            
            found = false;
        }
        if (found) {
            logger.log(Level.FINER, "Link is already in database: {0}", name);
        }else{
            logger.log(Level.FINER, "Link not found in database: {0}", name);
        }

        return found;
    }

    @Override
    public boolean saveIfNotExists(String name) {
        return false;
    }
}
