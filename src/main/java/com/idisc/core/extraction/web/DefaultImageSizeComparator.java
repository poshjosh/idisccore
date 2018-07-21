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

package com.idisc.core.extraction.web;

import com.bc.webdatex.image.ImageSizeComparator;

/**
 * @author Chinomso Bassey Ikwuagwu on Nov 9, 2016 11:01:20 AM
 */
public class DefaultImageSizeComparator extends ImageSizeComparator {

    public DefaultImageSizeComparator() {
        super(null, null, "https\\://fb\\.onthe\\.io/.+?(.prx.r600x315.).+?.jpg");
    }
}