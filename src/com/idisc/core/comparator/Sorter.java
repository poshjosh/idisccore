package com.idisc.core.comparator;

import java.util.List;

public interface Sorter<T> {
    
  List<T> sort(List<T> toSort);
}
