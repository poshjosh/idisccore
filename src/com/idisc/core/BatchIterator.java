package com.idisc.core;

import com.bc.util.XLogger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.logging.Level;


/**
 * @(#)DatabaseRecordIterator.java   01-Apr-2015 08:33:23
 *
 * Copyright 2011 NUROX Ltd, Inc. All rights reserved.
 * NUROX Ltd PROPRIETARY/CONFIDENTIAL. Use is subject to license 
 * terms found at http://www.looseboxes.com/legal/licenses/software.html
 */

/**
 * @author   chinomso bassey ikwuagwu
 * @version  2.0
 * @since    2.0
 */
public abstract class BatchIterator<E> implements java.util.Iterator<Collection<E>> {

    private boolean mayContinue = true;
    
    private boolean hasNextInitialized;
    
    private int offset;
    
    private Collection<E> batch;
    
    public BatchIterator() { }
    
    protected abstract Collection<E> loadNextBatch();

    public static void main(String [] args) {
        
            // Mon Nov 17 12:45:19 CET 2014
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy", Locale.ENGLISH);
        try{
            Date date = dateFormat.parse("Mon Nov 17 12:45:19 CET 2014");
System.out.println("Date: "+date);            
        }catch(Exception e) {
            e.printStackTrace();
        }
if(true) {
    return;
}        
        
        BatchIterator iter = new BatchIterator() {
            @Override
            protected Collection loadNextBatch() {
                int off = this.getOffset();
                if(off < 100) {
System.out.println("Loading batch: "+off);                
                    ArrayList list = new ArrayList();
                    for(int j=off; j<off+10; j++) {
                        list.add(j);
                    }
                    return list;
                }else{
                    return null;
                }
            }
        };
        while(iter.hasNext()) {
            iter.hasNext();
System.out.println(iter.next());
            iter.hasNext();
        }
        iter.reset(); // Note this
        iter.setOffset(3); // Note this also
        while(iter.hasNext()) {
            iter.hasNext();
System.out.println(iter.next());
            iter.hasNext();
        }
    }
    
    public void reset() {
        this.batch = null;
        this.hasNextInitialized = false;
        this.mayContinue = true;
        this.offset = 0;
    }
    
    @Override
    public boolean hasNext() {
        if(!this.mayContinue) {
            return false;
        }
        if(!this.hasNextInitialized) {
            this.hasNextInitialized = true;
            this.batch = this.loadNextBatch();
            final int count = this.batch == null ? 0 : this.batch.size();
XLogger.getInstance().log(Level.FINER, "Loaded {0} emails", this.getClass(), count);            
            this.offset += count;
            this.mayContinue = count > 0;
        }
        return this.mayContinue;
    }

    @Override
    public Collection<E> next() {
        if(!this.hasNext()) {
            throw new UnsupportedOperationException();
        }
        this.hasNextInitialized = false;
        return this.batch;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not supported."); 
    }
    
    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public Collection<E> getBatch() {
        return batch;
    }
}
