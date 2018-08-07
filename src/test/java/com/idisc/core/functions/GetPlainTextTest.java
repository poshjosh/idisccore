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

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class GetPlainTextTest {
    
    public GetPlainTextTest() {
    }

    /**
     * Test of apply method, of class GetPlainText.
     */
    @Test
    public void testApply() {
        System.out.println("apply");
        
        final String [] inputArr = {"@thisdaylive", 
            "<html><head><title>Title text</title></head><body>Body text</body></html>", 
            "2018-08-06T00:37:41+00:00",
            "<div>\n\tWithin outer div\n\t<span>Within span</span>\n\t<div>Within inner div</div>\n</div>\n<p>Within paragraph</p>"
        };
        final String [] outputArr = {inputArr[0], "Title text Body text", 
            inputArr[2], "Within outer div Within span Within inner div Within paragraph"};
        
        final String outputIfNone = "";
        final GetPlainText instance = new GetPlainText();
        
        for(int i=0; i<inputArr.length; i++) {
            
            final String expResult = outputArr[i];
            final String result = instance.apply(inputArr[i], outputIfNone);
            System.out.println("\nExpected: " + expResult);
            System.out.println("   Found: " + result);
           
            assertEquals(expResult, result);
        }
    }
}
