package com.idisc.core.util;

import com.bc.util.Util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import static org.junit.Assert.assertEquals;

/**
 * @author Josh
 */
public class UtilTestBase {
    
    private final String [] inputs;
    public UtilTestBase() { 
        inputs = new String[]{
            "Jesus is Lord of all. cdu-546278q[phkl;ms.,/zz,m.#",
            "",
            "1",
            "Testsuite: com.idisc.core.util.ListCharIteratorTest\n" +
                "Batch size: 12\n" +
                "String: Jesus is Lord of all. cdu-546278q[phkl;ms.,/zz,m.#\n" +
                "List: [Jesus is Lo, rd of all. , cdu-546278q, [phkl;ms.,/, zz,m.#]\n" +
                "J == J\n" +
                "e == e\n" +
                "s == s" +
                "Deleting: C:\\Users\\Josh\\AppData\\Local\\Temp\\TEST-com.idisc.core.util.ListCharIteratorTest.xml\n" +
                "BUILD SUCCESSFUL (total time: 2 seconds)"+
                "Copying 1 file to C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\build\\web\\WEB-INF\\lib\n" +
                "Copying 1 file to C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\build\\web\\WEB-INF\\lib\n" +
                "library-inclusion-in-manifest:\n" +
                "Created dir: C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\build\\empty\n" +
                "Created dir: C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\build\\generated-sources\\ap-source-output\n" +
                "Compiling 91 source files to C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\build\\web\\WEB-INF\\classes\n" +
                "Note: Some input files use unchecked or unsafe operations.\n" +
                "Note: Recompile with -Xlint:unchecked for details.\n" +
                "compile:\n" +
                "compile-jsps:\n" +
                "Created dir: C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\dist\n" +
                "Building jar: C:\\Users\\Josh\\Documents\\NetBeansProjects\\idisc\\dist\\idisc.war\n" +
                "do-dist:\n" +
                "dist:\n" +
                "BUILD SUCCESSFUL (total time: 4 minutes 25 seconds)"
        };
    }
    
    public String getRandomString() {
        return inputs[Util.randomInt(inputs.length)];
    }

    public List<String> getList(String string, int batchSize) {
        if(batchSize < 2) {
            batchSize = 2;
        }
System.out.println("Batch size: "+batchSize);        
        List<String> list = new ArrayList<>();
        StringBuilder sb = new StringBuilder(batchSize + 1);
        int batchCount = 0;
        for(int i=0; i<string.length(); i++) {
            sb.append(string.charAt(i));
            ++batchCount;
            if(batchCount == batchSize - 1) {
                list.add(sb.toString());
                sb.setLength(0);
                batchCount = 0;
            }
        }
        if(batchCount > 0) {
            list.add(sb.toString());
        }
        return list;
    }
    
    public List<Reader> getReaders() {
        List<Reader> readers = new ArrayList(inputs.length);
        for(String input:inputs) {
            readers.add(new StringReader(input));
        }
        return readers;
    }
    
    public int getInputsLength() {
        return this.getLength(this.getInputs());
    }
    
    public int getLength(List<String> list) {
        int len = 0;
        for(String s:list) {
            len += s.length();
        }
        return len;
    }

    public List<String> getInputs() {
        return Arrays.asList(inputs);
    }

    public void testWithBuffer(BufferedReader strReader, int len, BufferedReader listReader, int skip) {
        
        try{
            
            if(skip > 0) {
                strReader.skip(skip);
                listReader.skip(skip);
System.out.println("Skipped: "+skip);                
            }
            
            final int left = skip >= len ? 0 : len - skip;
            final boolean mark = left > 0;
            
            int strLeft = left;
            int listLeft = left;
            
            boolean marked = false;
            boolean reset = false;
            
            do{

                String strLine = strReader.readLine();
                String listLine = listReader.readLine();
                
System.out.println("= = = = = = = lines = = = = = = =\n"+strLine + "\n" + listLine);

                assertEquals(strLine, listLine);
                
                if(strLine == null && listLine == null) {
                    break;
                }
                
                strLeft -= strLine.length();
                listLeft -= listLine.length();
                
                if(strLeft < 1 && listLeft < 1) {
                    continue;
                }
                
                if(mark){
                    if(!marked) {
                        marked = true;
                        strReader.mark(strLeft); 
                        listReader.mark(listLeft);
                    }else{
                        if(!reset) {
                            reset = true;
                            strReader.reset();
                            listReader.reset();
//System.out.println("Input len: "+string.length()+", skipped at: "+skip+", left after skip, string: "+strLeft+", list: "+listLeft);                                                        
                        }
                    }
                }

            }while(true);
            
        }catch(IOException e) {
            
            e.printStackTrace();
            
        }finally{
           
            try{
                strReader.close();
                listReader.close();
            }catch(IOException e) { e.printStackTrace(); }
        }
    }    

    public void test(Reader strReader, Reader listReader) {
        
        try{
            
            do{

                int strChar = strReader.read(); 
                int listChar = listReader.read();

System.out.println(Character.toString((char)strChar) + " = " + Character.toString((char)listChar));

                assertEquals(strChar, listChar);
                
                if(strChar == -1 && listChar == -1) {
                    break;
                }

            }while(true);
            
        }catch(IOException e) {
            
            e.printStackTrace();
            
        }finally{
           
            try{
                strReader.close();
                listReader.close();
            }catch(IOException e) { e.printStackTrace(); }
        }
    } 
    
    public int getRandomSkip(String input) {
        int skip = Util.randomInt(input.length());
        if(skip < 1) {
            skip = 1;
        }
        if(skip >= input.length()) {
            skip = input.length() - 1;
        }
        return skip;
    }
}
