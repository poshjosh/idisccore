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

package com.idisc.core.util.mapbuildertransformers;

import com.bc.util.MapBuilder;
import com.idisc.pu.entities.Feed;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 3:02:15 PM
 */
public class TransformerService_appVersionCode8orBelow extends TransformerServiceImpl {

    public TransformerService_appVersionCode8orBelow() { }

    public TransformerService_appVersionCode8orBelow(boolean plainTextOnly, int maxTextLength) {
        super(plainTextOnly, maxTextLength);
    }

    @Override
    protected MapBuilder.Transformer getFeedTranformer(Class<Feed> type) {
        return new FeedMapEntryTransformer_appVersionCode8orBelow();
    }
}
