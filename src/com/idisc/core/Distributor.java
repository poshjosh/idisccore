package com.idisc.core;

import java.util.List;

public abstract interface Distributor<T>
{
  public abstract List<T> distribute(List<T> paramList);
}
