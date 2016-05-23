package com.bc.jpa;

import com.bc.jpa.DefaultEntityController;
import com.bc.jpa.PersistenceMetaData;
import com.bc.jpa.exceptions.EntityInstantiationException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 * @author USER
 */
public class DefaultEntityControllerTestCase extends DefaultEntityController {
    
    public DefaultEntityControllerTestCase() { }

    public DefaultEntityControllerTestCase(PersistenceMetaData puMeta, Class entityClass) {
        super(puMeta, entityClass);
    }

    @Override
    public EntityManager getEntityManager() {
        return new EntityManagerTestCase(this.getDatabaseName());
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public List edit(String connector, Map where, Map update) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public int update(String connector, Map where, Map update) {
        return 0;
    }

    @Override
    public int update(Map where, Map update) {
        return 0;
    }

    @Override
    public List select(Map params, Map orderBy, String connector, int maxResults, int firstResult) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List select(Map parameters, String connector) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public long count(Map params) {
        return 0L;
    }

    @Override
    public Object editById(Object id, String col, Object val) {
        return null;
    }

    @Override
    public int updateById(Object id, String col, Object val) {
        return 0;
    }

    @Override
    public List edit(String searchCol, Object searchVal, String updateCol, Object updateVal) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public int update(String searchCol, Object searchVal, String updateCol, Object updateVal) {
        return 0;
    }

    @Override
    public Object selectId(String column, Object value) {
        return null;
    }

    @Override
    public Object selectValue(String selectCol, String column, Object value) {
        return null;
    }

    @Override
    public Object selectFirst(Map params) {
        return null;
    }

    @Override
    public Object selectFirst(String columnName, Object columnValue) {
        return null;
    }

    @Override
    public List selectColumns(Collection selectCols, Map whereClauseParameters, Map orderBy, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List selectColumn(String selectCol, Map params, Map orderBy, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List selectColumn(String selectCol, String column, Object value, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List select(String column, Object[] values, Map orderBy, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List select(String column, Object[] values) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List select(Map params, Map orderBy, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List select(String column, Object value, int offset, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public Object selectById(Object id) {
        return null;
    }

    @Override
    public Object persist(Map params) {
        return null;
    }

    @Override
    public int insert(Map params) {
        return 0;
    }

    @Override
    public int deleteById(Object id) {
        return 0;
    }

    @Override
    public int delete(Map params) {
        return 0;
    }

    @Override
    public int delete(String column, Object value) {
        return 0;
    }

    @Override
    public List toEntitiesList(List results, int limit) throws EntityInstantiationException {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List toMapList(List results, Collection columnNames, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List toMapList(List results, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List toList(List results, String column, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public List toList(List results, int columnIndex, int limit) {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void setDatabaseName(String databaseName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] getGeneratedKeys() {
        return new int[0];
    }
}
