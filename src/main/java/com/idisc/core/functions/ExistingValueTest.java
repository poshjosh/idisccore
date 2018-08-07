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

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

/**
 * @author Chinomso Bassey Ikwuagwu on Jul 27, 2018 11:18:16 PM
 */
public class ExistingValueTest implements BiPredicate<String, Object>, AutoCloseable, Serializable {

    private static final Logger LOG = Logger.getLogger(ExistingValueTest.class.getName());
    
    private final Class entityClass;
    
    private final EntityManager entityManager;
    
//    private final char alias;

    public ExistingValueTest(EntityManager entityManager, Class entityClass) {
        this.entityManager = Objects.requireNonNull(entityManager);
        this.entityClass = Objects.requireNonNull(entityClass);
//        this.alias = Character.toLowerCase(this.entityClass.getSimpleName().charAt(0));
    }

    @Override
    public void close() {
        if(this.entityManager.isOpen()) {
          this.entityManager.close();
        }
    }
    
    @Override
    public boolean test(String column, Object value) {
        boolean found;
        try{
            final TypedQuery<?> query = this.entityManager.createQuery(this.createQuery(column), value.getClass());
            query.setParameter(column, value);
            query.setFirstResult(0).setMaxResults(1);
            found = query.getSingleResult() != null;
        }catch(NoResultException e) {
            
            found = false;
        }
        if (found) {
            LOG.log(Level.FINER, "Link is already in database: {0}", value);
        }else{
            LOG.log(Level.FINER, "Link not found in database: {0}", value);
        }

        return found;
    }
    
    private String createQuery(String col) {
        return "SELECT a."+col+" FROM "+entityClass.getSimpleName()+" a WHERE a."+col+" = :"+col;
    }
}
