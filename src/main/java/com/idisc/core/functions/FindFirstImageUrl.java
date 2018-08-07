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

import com.bc.webdatex.functions.FindFirstNode;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Node;
import org.htmlparser.tags.ImageTag;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 9:11:46 PM
 */
public class FindFirstImageUrl implements BiFunction<List<Node>, Predicate<Node>, String>, Serializable {

    private transient static final Logger LOG = Logger.getLogger(FindFirstImageUrl.class.getName());
    
    private final BiFunction<List<Node>, Predicate<Node>, Node> findFirstNode;

    public FindFirstImageUrl() {
        this(new FindFirstNode());
    }

    public FindFirstImageUrl(BiFunction<List<Node>, Predicate<Node>, Node> findFirstNode) {
        this.findFirstNode = Objects.requireNonNull(findFirstNode);
    }

    @Override
    public String apply(List<Node> nodeList, Predicate<Node> nodeTest) {
    
        try{
            
            if(nodeTest != null) {

                final Node node = findFirstNode.apply(nodeList, nodeTest);
                
                final ImageTag imageTag = (node instanceof ImageTag) ? (ImageTag)node : null;

                if (imageTag != null) {
                    
                    String imageUrl = imageTag.getImageURL();
                    
                    if(LOG.isLoggable(Level.FINE)){
                              LOG.log(Level.FINE, "Image URL: {0}", imageUrl);
                    }

                    return imageUrl;
                }
            }
        }catch(Exception e) {
            
            if(LOG.isLoggable(Level.WARNING)){
                  LOG.log(Level.WARNING, "Error extracting image url", e);
            }
        }
        
        return null;
    }
}
