package com.idisc.core.util;

import java.util.logging.Logger;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;

public abstract class BatchIterator<E> implements Iterator<Collection<E>> {

  private transient static final Logger LOG = Logger.getLogger(BatchIterator.class.getName());
    
  private boolean mayContinue = true;
  
  private boolean hasNextInitialized;
  
  private int offset;
  
  private Collection<E> batch;
  
  protected abstract Collection<E> loadNextBatch();
  
  public void reset() {
    this.batch = null;
    this.hasNextInitialized = false;
    this.mayContinue = true;
    this.offset = 0;
  }
  
  @Override
  public boolean hasNext()
  {
    if (!this.mayContinue) {
      return false;
    }
    if (!this.hasNextInitialized) {
      this.hasNextInitialized = true;
      this.batch = loadNextBatch();
      int count = this.batch == null ? 0 : this.batch.size();
      if(LOG.isLoggable(Level.FINER)){
         LOG.log(Level.FINER, "Loaded {0} emails", Integer.valueOf(count));
      }
      this.offset += count;
      this.mayContinue = (count > 0);
    }
    return this.mayContinue;
  }
  
  @Override
  public Collection<E> next()
  {
    if (!hasNext()) {
      throw new UnsupportedOperationException();
    }
    this.hasNextInitialized = false;
    return this.batch;
  }
  
  @Override
  public void remove()
  {
    throw new UnsupportedOperationException("Not supported.");
  }
  
  public void setOffset(int offset) {
    this.offset = offset;
  }
  
  public int getOffset() {
    return this.offset;
  }
  
  public Collection<E> getBatch() {
    return this.batch;
  }
}