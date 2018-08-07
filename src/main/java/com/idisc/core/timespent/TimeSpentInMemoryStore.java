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
import java.util.HashMap;
import java.util.Map;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 31, 2018 6:20:55 PM
 */
public class TimeSpentInMemoryStore<K> implements TimeSpentStore<K> {
    
    private final Map<K, TimeSpent> store = new HashMap<>();

    @Override
    public TimeSpent getOrDefault(K key, TimeSpent outputIfNone) {
        return store.getOrDefault(key, outputIfNone);
    }

    @Override
    public TimeSpent put(K key, TimeSpent timeSpent) {
        return store.put(key, timeSpent);
    }

    @Override
    public TimeSpent remove(K key) {
        return store.remove(key);
    }
}
