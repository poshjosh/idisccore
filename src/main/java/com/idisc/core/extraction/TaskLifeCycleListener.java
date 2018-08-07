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

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 26, 2018 9:14:38 PM
 */
public interface TaskLifeCycleListener {
    
    void onStarted(Object arg);
    void onException(Object arg, Exception e);
    void onSuccess(Object arg);

    void onUnitStarted(Object arg);
    void onUnitCompleted(Object arg);
}
/**
 * 
    TaskLifeCycleListener NO_OP = new TaskLifeCycleListener() {
        @Override
        public void onStarted(Object arg) { }
        @Override
        public void onException(Object arg, Exception e) { }
        @Override
        public void onSuccess(Object arg) { }
        @Override
        public void onUnitStarted(Object arg) { }
        @Override
        public void onUnitCompleted(Object arg) { }
    };
    
 * 
 */