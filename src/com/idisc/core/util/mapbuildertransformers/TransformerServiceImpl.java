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

import com.bc.jpa.util.EntityMapBuilder.Transformer;
import com.idisc.pu.entities.Comment;
import com.idisc.pu.entities.Feed;
import com.idisc.pu.entities.Feeduser;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2016 1:42:56 PM
 */
public class TransformerServiceImpl implements TransformerService {

    private final Transformer textTransformer;
    
    private final Map<Class, Transformer> cache;

    public TransformerServiceImpl() {
        this(false, Integer.MAX_VALUE);
    }
    
    public TransformerServiceImpl(boolean plainTextOnly, int maxTextLength) {
        this.textTransformer = plainTextOnly || this.isAcceptableTransformationLength(maxTextLength) ?
                new TextTransformer(plainTextOnly, maxTextLength) : Transformer.NO_OPERATION;
        this.cache = new HashMap();
    } 
    
    private boolean isAcceptableTransformationLength(int len) {
        return len > 0 && len < Integer.MAX_VALUE;
    }
    
    @Override
    public Transformer get(Class entityType) {
        
        Transformer output = cache.get(entityType);
        
        if(output == null) { 
            
            if(entityType == Feed.class) {
                output = new TransformerChain(textTransformer, this.getFeedTranformer(entityType));
            }else if(entityType == Comment.class) {
                output = new TransformerChain(textTransformer, new CommentMapEntryTransformer());
            }else if(entityType == Feeduser.class) {
                output = new TransformerChain(textTransformer, new FeeduserMapEntryTransformer());
            }else{
                output = textTransformer;
            }
            
            cache.put(entityType, output);
        }
        
        return output;
    }
    
    protected Transformer<Feed> getFeedTranformer(Class<Feed> type) {
        return new FeedMapEntryTransformer();
    }
}
