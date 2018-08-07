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

import com.bc.jpa.context.JpaContext;
import com.bc.webdatex.context.ExtractionConfig;
import com.idisc.core.IdiscTestBase;
import com.idisc.core.SiteNames;
import java.io.IOException;
import java.util.Date;
import org.junit.Test;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 6, 2018 10:30:24 PM
 */
public class FeedCreationContextTest extends IdiscTestBase {

    @Test
    public void test() {
        try{
            final String sitetype = ScrapContext.TYPE_WEB;
            final String siteName = SiteNames.THISDAY;
            final ExtractionConfig config = this.getExtractionContextFactory()
                    .getContext(siteName).getExtractionConfig();
            final JpaContext jpa = this.createJpaContext();
            final FeedCreationContext context = FeedCreationContext.builder()
                    .with(jpa, sitetype, config).build();
            final String input = "2018-08-02T04:43:08+00:00";
            this.test(context, input);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public void test(FeedCreationContext context, String input) {
        final Date output = context.getDate(input, null);
        System.out.println("Input: " + input + ", output: " + output);
    }
}
