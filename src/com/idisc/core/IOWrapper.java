package com.idisc.core;







public class IOWrapper<K>
  extends com.bc.io.IOWrapper<K>
{
  public IOWrapper() {}
  






  public IOWrapper(K target, String filename)
  {
    super(target, filename);
  }
  
  public String getPath(String fileName)
  {
    return IdiscApp.getInstance().getAbsolutePath(fileName);
  }
}
