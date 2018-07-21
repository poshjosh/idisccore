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

import java.io.Serializable;
import org.htmlparser.dom.HtmlDocument;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * @author Chinomso Bassey Ikwuagwu on Oct 7, 2017 7:57:50 PM
 */
public class ScrappDocumentTest implements Predicate<HtmlDocument>, Serializable {

    private transient static final Logger LOG = Logger.getLogger(ScrappDocumentTest.class.getName());
    
    private final Predicate<String> docUrlTest;

    public ScrappDocumentTest() {
        this((link) -> true);
    }

    public ScrappDocumentTest(Predicate<String> docUrlTest) {
        this.docUrlTest = Objects.requireNonNull(docUrlTest);
    }
    
    @Override
    public boolean test(HtmlDocument doc) {
        final boolean success = doc != null && this.acceptBody(doc) && 
            this.acceptTitle(doc) && this.acceptUrl(doc);
        LOG.finer(() -> "Scrapp: " + success + ", " + doc.getURL());
        return success;
    }
  
    private boolean acceptBody(HtmlDocument doc) {
        return true;//pageNodes.getBody() != null;
    }
  
    private boolean acceptTitle(HtmlDocument doc) {
        return doc.getTitle() == null || !doc.getTitle().toPlainTextString().toLowerCase().contains("400 bad request");  
    }
  
    private boolean acceptUrl(HtmlDocument doc) {
        return this.docUrlTest.test(doc.getURL());
    }
}
