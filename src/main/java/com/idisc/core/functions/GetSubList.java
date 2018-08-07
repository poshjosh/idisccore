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

package com.idisc.core.functions;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 1, 2017 9:39:39 AM
 */
public class GetSubList<E> implements BiFunction<Collection<E>, Integer, List<E>>, Serializable {

    private transient static final Logger LOG = Logger.getLogger(GetSubList.class.getName());
    
    private int offset;

    public GetSubList() {
        this(0);
    }

    public GetSubList(int offset) {
        this.offset = offset;
    }
    
    @Override
    public List<E> apply(Collection<E> collection, Integer limit) {
        
        if(limit > collection.size()) {
            limit = collection.size();
        }
        
        Objects.requireNonNull(collection);
        Objects.requireNonNull(limit);
        
        final List<E> buff = new ArrayList(collection);
        
        final int from = offset;
        final int to = from + limit;
        
        LOG.finer(() -> "Size: " + collection.size() + ", from: " + from + ", to: " + to);
        
        final int nextOffset;
        
        final List<E> output;
        
        if(to > buff.size()) {
            
            nextOffset = to - buff.size();
            
            LOG.finer(() -> "Before rotate: " + buff);
            
            Collections.rotate(buff, buff.size() - from);
            
            LOG.finer(() -> " After rotate: " + buff);

            output = buff.subList(0, limit);
            
//            output = new ArrayList(limit);
//            output.addAll(buff.subList(from, buff.size()));
//            output.addAll(buff.subList(0, nextOffset));
        }else{
            
            nextOffset = to == buff.size() ? 0 : to;
            
            output = buff.subList(from, to);
        }
        
        LOG.fine(() -> MessageFormat.format(
                "All sites: {0}\nFrom offset: {1}, sites: {2}", 
                buff, offset, output));
        
        offset = nextOffset;
        
        return output;
    }

    public final int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return super.toString() + "{offset=" + offset + '}';
    }
}
