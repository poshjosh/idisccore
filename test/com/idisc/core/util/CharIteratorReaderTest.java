package com.idisc.core.util;

import com.idisc.core.characteriterator.ListCharIterator;
import com.idisc.core.characteriterator.CharIteratorReader;
import java.io.BufferedReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class CharIteratorReaderTest extends UtilTestBase {
    
    public CharIteratorReaderTest() { }
    
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
        for(String input:this.getInputs()) {
            final int skip = this.getRandomSkip(input);
            this.testWithBuffer(input, skip);
            this.test(input);
        }
    }

    public void testWithBuffer(String string, int skip) {
        final int batchSize = string.length() / 4;
        final List<String> list = Collections.unmodifiableList(this.getList(string, batchSize));
        testWithBuffer(string, list, skip);
    }
    
    public void test(String string) {
        final int batchSize = string.length() / 4;
        final List<String> list = Collections.unmodifiableList(this.getList(string, batchSize));
        test(string, list);
    }

    public void testWithBuffer(String string, List<String> list, int skip) {
System.out.println("String: "+string);        
System.out.println("List: "+list);
        
        BufferedReader strReader = new BufferedReader(new StringReader(string));
        BufferedReader listReader = new BufferedReader(new CharIteratorReader(new ListCharIterator(list, 1024)));
        
        this.testWithBuffer(strReader, string.length(), listReader, skip);
    }    

    public void test(String string, List<String> list) {
System.out.println("String: "+string);        
System.out.println("List: "+list);
        
        Reader strReader = new StringReader(string);
        Reader listReader = new CharIteratorReader(new ListCharIterator(list, 1024));
        
        this.test(strReader, listReader);
    } 
}
