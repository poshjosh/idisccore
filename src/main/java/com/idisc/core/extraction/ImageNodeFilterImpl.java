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

package com.idisc.core.extraction;

import com.bc.json.config.JsonConfig;
import com.bc.nodelocator.ConfigName;
import com.bc.webdatex.context.NodeExtractorConfig;
import com.bc.webdatex.nodefilters.ImageNodeFilter;

/**
 * @author Chinomso Bassey Ikwuagwu on Jun 23, 2018 12:12:55 PM
 */
public class ImageNodeFilterImpl extends ImageNodeFilter {

    public ImageNodeFilterImpl(JsonConfig config) {
        super(
                null, //config.getString(new Object[] { "url", "value" }), // Images may not start with baseUrl
                config.getString(ConfigName.imageUrl_requiredRegex),
                config.getString(ConfigName.imageUrl_unwantedRegex));
    }

    public ImageNodeFilterImpl(NodeExtractorConfig config) {
        super(
                null, //config.getString(new Object[] { "url", "value" }), // Images may not start with baseUrl
                config.getImageUrlRequiredRegex(),
                config.getImageUrlUnwantedRegex());
    }
}
