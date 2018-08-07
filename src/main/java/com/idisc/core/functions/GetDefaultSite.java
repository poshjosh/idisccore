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

package com.idisc.core.functions;

import com.bc.jpa.context.PersistenceUnitContext;
import com.idisc.core.ConfigNames;
import com.idisc.pu.entities.Site;
import java.util.Objects;
import java.util.function.BiFunction;
import org.apache.commons.configuration.Configuration;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 8:01:49 PM
 */
public class GetDefaultSite implements BiFunction<PersistenceUnitContext, Configuration, Site> {

    @Override
    public Site apply(PersistenceUnitContext puContext, Configuration config) {
        final Integer siteId = config.getInteger(ConfigNames.DEFAULTSITE_ID, null);
        Objects.requireNonNull(siteId);
        final Site site = puContext.getDao().findAndClose(Site.class, siteId);
        return Objects.requireNonNull(site);
    }
}
