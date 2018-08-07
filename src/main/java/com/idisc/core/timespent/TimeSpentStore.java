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

package com.idisc.core.timespent;

import com.bc.timespent.TimeSpent;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 7:33:24 PM
 */
public interface TimeSpentStore<K> {
    
    TimeSpent getOrDefault(K key, TimeSpent outputIfNone);

    default TimeSpent putIfAbsent(K key, TimeSpent value) {
        TimeSpent v = getOrDefault(key, null);
        if (v == null) {
            v = put(key, value);
        }
        return v;
    }
    
    TimeSpent put(K key, TimeSpent timeSpent);
    
    TimeSpent remove(K key);
}
