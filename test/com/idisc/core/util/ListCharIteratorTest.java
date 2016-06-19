package com.idisc.core.util;

import com.idisc.core.characteriterator.ListCharIterator;
import java.io.IOException;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class ListCharIteratorTest extends UtilTestBase {
    
    public ListCharIteratorTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void testAll() throws IOException {
        for(String input:this.getInputs()) {
            this.test(input);
        }
    }
    
    public void test(String string) throws IOException {
        final int batchSize = string.length() / 4;
        final List<String> list = Collections.unmodifiableList(this.getList(string, batchSize));
        test(string, list);
    }

    public void test(String string, List<String> list) throws IOException {
System.out.println("String: "+string);        
System.out.println("List: "+list);
        
        ListCharIterator listIter = new ListCharIterator(list, 1024);
        StringCharacterIterator strIter = new StringCharacterIterator(string);
        
        char strNext = strIter.first();
        while(strNext != StringCharacterIterator.DONE || listIter.hasNext()) {
            
            listIter.hasNext(); listIter.hasNext();// This should not advance our positon
            
            char listNext = listIter.next();
System.out.println(strNext + " == " + listNext);            
            assertEquals(strNext, listNext);
            strNext = strIter.next();
        }
//        fail("The test case is a prototype.");
    }
}
