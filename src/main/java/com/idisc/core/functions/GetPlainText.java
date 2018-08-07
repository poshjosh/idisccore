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

import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringExtractingNodeVisitor;
import org.htmlparser.util.ParserException;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 12:55:38 AM
 */
public class GetPlainText implements BiFunction<String, String, String>, Serializable {

    private transient static final Logger LOG = Logger.getLogger(GetPlainText.class.getName());
    
    private final Lock lock = new ReentrantLock();

    private final StringExtractingNodeVisitor stringExtractingNodeVisitor;
    
    private final Parser parser;

    public GetPlainText() {
        stringExtractingNodeVisitor = new StringExtractingNodeVisitor() {
            @Override
            protected void carriageReturn() {
                this.appendSeparator(" ");
            }
        };
        
        this.parser = new Parser();
    }
    
    @Override
    public String apply(String input, String outputIfNone) {

        Objects.requireNonNull(input);
        
        String output;
        
        try{
            
            lock.lock();

            stringExtractingNodeVisitor.reset();
            stringExtractingNodeVisitor.setCollapse(true);
            stringExtractingNodeVisitor.setLinks(false);
            stringExtractingNodeVisitor.setReplaceNonBreakingSpaces(false);
            
            parser.setInputHTML (input);

            parser.visitAllNodesWith (stringExtractingNodeVisitor);

            output = stringExtractingNodeVisitor.getStrings();
                
        }catch(ParserException e) {
            LOG.warning(e.toString());
            LOG.log(Level.FINE, null, e);
            output = null;
        }finally{
            lock.unlock();
        }
        
        return output == null || output.isEmpty() ? outputIfNone : output;
    }
}
