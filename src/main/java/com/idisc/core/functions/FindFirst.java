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

package com.idisc.core.functions;

import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * @author Chinomso Bassey Ikwuagwu on Aug 6, 2018 12:07:00 PM
 */
public class FindFirst<T> implements BiFunction<Predicate<T>, T[], T>, BiPredicate<Predicate<T>, T[]> {

    @Override
    public boolean test(Predicate<T> test, T[] arr) {
        
        final T found = this.apply(test, arr);

        return found != null;
    }

    @Override
    public T apply(Predicate<T> test, T[] arr) {
        
        for(T t : arr) {
            
            if(test.test(t)) {
                
                return t;
            }
            
        }
        
        return null;
    }
}
