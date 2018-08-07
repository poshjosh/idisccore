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

package com.idisc.core;

import com.bc.webdatex.extractors.date.DateStringFromUrlExtractor;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 29, 2018 5:35:04 PM
 */
public class DatePatternsTest {

    public static void main(String... args) throws Exception {
        final String url = "https://www.looseboxes.com/images/2018/07/kidsclothes/1234.jpg";
        final DateStringFromUrlExtractor ex = new DateStringFromUrlExtractor();
        final String dateStr = ex.extract(url, null);
        System.out.println("Date text: " + dateStr);
        if(dateStr == null || dateStr.isEmpty()) {
            return;
        }
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM");
        final boolean lenientMayCauseFalsePositives = true;
        sdf.setLenient(!lenientMayCauseFalsePositives);
        final Date date = sdf.parse(dateStr);
        System.out.println("Date: " + date);
    }
}
