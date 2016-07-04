package com.idisc.core;

import java.util.concurrent.Callable;

public interface TaskHasResult<T> extends Runnable, Callable<T>, HasResult<T> {

}
