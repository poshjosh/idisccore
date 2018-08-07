/*
 * Copyright 2018 NUROX Ltd.
 *
 * Licensed under the NUROX Ltd Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.looseboxes.com/legal/licenses/software.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.idisc.core.extraction;

import java.util.function.Consumer;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 28, 2018 9:31:50 PM
 */
public class TaskLifeCycleListenerImpl implements TaskLifeCycleListener {
    
    private final Consumer onStart;
    private final Consumer onException;
    private final Consumer onSuccess;

    public TaskLifeCycleListenerImpl(Consumer onStart, Consumer onFinish) {
        this(onStart, onFinish, onFinish);
    }
    
    public TaskLifeCycleListenerImpl(Consumer onStart, Consumer onException, Consumer onSuccess) {
        this.onStart = onStart;
        this.onException = onException;
        this.onSuccess = onSuccess;
    }

    @Override
    public void onStarted(Object arg) { 
        if(onStart != null) {
            onStart.accept(arg);
        }
    }
    
    @Override
    public void onException(Object arg, Exception e) { 
        if(onException != null) {
            onException.accept(arg);
        }
    }
    
    @Override
    public void onSuccess(Object arg) { 
        if(onSuccess != null) {
            onSuccess.accept(arg);
        }
    }

    @Override
    public void onUnitStarted(Object arg) { }

    @Override
    public void onUnitCompleted(Object arg) { }
}
