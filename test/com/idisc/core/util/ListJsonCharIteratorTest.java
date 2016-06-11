package com.idisc.core.util;

import com.bc.util.JsonFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONValue;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class ListJsonCharIteratorTest extends UtilTestBase {
    
    public ListJsonCharIteratorTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testAll() {
        Map [] arr = {
            Collections.singletonMap(0, Boolean.FALSE),
            Collections.singletonMap(1, 1),
            Collections.singletonMap(2, "Element Two"),
            Collections.singletonMap(3, new Date())
        };
        
        ListJsonCharIterator instance = new ListJsonCharIterator(Arrays.asList(arr), 127, new JsonFormat());
        
        StringBuilder builder = new StringBuilder();
        
        while(instance.hasNext()) {
            char ch = instance.next();
            builder.append(ch);
        }
        
System.out.println(builder);

        List list = (List)JSONValue.parse(builder.toString());
        for(Object o:list) {
System.out.println(o);            
        }
    }
}
