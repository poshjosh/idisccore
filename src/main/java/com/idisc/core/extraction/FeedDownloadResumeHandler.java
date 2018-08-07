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
import com.idisc.core.functions.ExistingValueTest;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feed_;
import javax.persistence.EntityManager;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 6:43:06 PM
 */
public class FeedDownloadResumeHandler extends ExistingValueTest implements ResumeHandler {

    public FeedDownloadResumeHandler(EntityManager entityManager) {
        super(entityManager, Feed.class);
    }

    @Override
    public boolean isExisting(String name) {
        return super.test(Feed_.url.getName(), name);
    }

    @Override
    public boolean saveIfNotExists(String name) {
        return false;
    }
}
