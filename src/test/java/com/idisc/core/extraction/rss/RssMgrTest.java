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
package com.idisc.core.extraction.rss;

import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Josh
 */
public class RssMgrTest {
    
    public RssMgrTest() { }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getSyndFeed method, of class RssMgr.
     * @throws java.io.IOException
     * @throws com.rometools.rome.io.FeedException
     */
    @Test
    public void testGetSyndFeed() throws IOException, FeedException {
        System.out.println("getSyndFeed");
        String path = "http://rss.cnn.com/rss/edition_africa.rss";
        RssMgr instance = new RssMgr();
        SyndFeed result = instance.getSyndFeed(path);
        System.out.println("Result: " + result);
    }
}
