package com.idisc.core.util;

import java.io.BufferedReader;
import java.io.Reader;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Josh
 */
public class SequenceReaderTest extends UtilTestBase {
    
    public SequenceReaderTest() { }
    
    @BeforeClass
    public static void setUpClass() { }
    
    @AfterClass
    public static void tearDownClass() { }
    
    @Before
    public void setUp() { }
    
    @After
    public void tearDown() { }

    @Test
    public void test() throws Exception {
        
        this.test(this.getCharIteratorReader(), this.getSequenceRequest());
        
        final int skip = 1;
        
        this.testWithBuffer(
                new BufferedReader(this.getCharIteratorReader()), this.getInputsLength(), 
                new BufferedReader(this.getSequenceRequest()), skip);
    }
    
    public Reader getCharIteratorReader() {
        List<String> inputs = this.getInputs();
        ListCharIterator lci = new ListCharIterator(inputs);
        CharIteratorReader iterReader = new CharIteratorReader(lci);
        return iterReader;
    }
    
    public Reader getSequenceRequest() {
        List<Reader> readers = this.getReaders();
        SequenceReader seqReader = new SequenceReader(readers);
        return seqReader;
    }
}
