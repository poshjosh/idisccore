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

package com.idisc.core.filters;

import com.scrapper.context.CapturerContext;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 3, 2016 10:32:11 AM
 */
public class CapturerContextImagesFilter extends ImagesFilter {
    public CapturerContextImagesFilter(CapturerContext context) {
        super(context.getConfig().getString(new Object[] { "url", "value" }),
                context.getConfig().getString("imageUrl_requiredRegex"),
                context.getConfig().getString("imageUrl_unwantedRegex"));
    }
}
