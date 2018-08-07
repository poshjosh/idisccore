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
import com.bc.io.IOWrapper;
import java.util.Objects;
import java.util.function.UnaryOperator;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 30, 2018 6:18:38 PM
 */
public class TimeSpentLocalDiscStore implements TimeSpentStore<String> {

    private final IOWrapper<TimeSpent> io;

    public TimeSpentLocalDiscStore() {
        this(new IOWrapper<>());
    }
    
    public TimeSpentLocalDiscStore(UnaryOperator<String> getPathForName) {
        this(new IOWrapper<>(getPathForName));
    }
    
    public TimeSpentLocalDiscStore(IOWrapper<TimeSpent> io) {
        this.io = Objects.requireNonNull(io);
    }
    
    @Override
    public TimeSpent getOrDefault(String name, TimeSpent outputIfNone) {
        io.setFilename(name);
        final TimeSpent found = io.getTarget();
        return found == null ? outputIfNone : found;
    }
    
    @Override
    public TimeSpent put(String name, TimeSpent timeSpent) {
        io.setFilename(name);
        final TimeSpent prev = io.getTarget();
        io.setTarget(timeSpent);
        return prev;
    }

    @Override
    public TimeSpent remove(String key) {
        io.setFilename(key);
        final TimeSpent remove = io.getTarget();
        io.setTarget(null); 
        return remove;
    }
}
