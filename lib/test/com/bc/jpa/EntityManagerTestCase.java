package com.bc.jpa;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityGraph;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.StoredProcedureQuery;
import javax.persistence.SynchronizationType;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.metamodel.Metamodel;
import org.eclipse.persistence.descriptors.ClassDescriptor;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate;
import org.eclipse.persistence.internal.jpa.EntityManagerImpl;
import org.eclipse.persistence.internal.jpa.QueryImpl;
import org.eclipse.persistence.internal.sessions.AbstractSession;
import org.eclipse.persistence.internal.sessions.DatabaseSessionImpl;
import org.eclipse.persistence.internal.sessions.RepeatableWriteUnitOfWork;
import org.eclipse.persistence.queries.AttributeGroup;
import org.eclipse.persistence.queries.Call;
import org.eclipse.persistence.queries.DatabaseQuery;
import org.eclipse.persistence.queries.ReadObjectQuery;
import org.eclipse.persistence.sessions.Session;
import org.eclipse.persistence.sessions.UnitOfWork;
import org.eclipse.persistence.sessions.broker.SessionBroker;
import org.eclipse.persistence.sessions.server.ServerSession;

/**
 * @author USER
 */
public class EntityManagerTestCase extends EntityManagerImpl {

    public EntityManagerTestCase(String sessionName) {
        super(sessionName);
    }

    public EntityManagerTestCase(AbstractSession databaseSession, SynchronizationType syncType) {
        super(databaseSession, syncType);
    }

    public EntityManagerTestCase(AbstractSession databaseSession, Map properties, SynchronizationType syncType) {
        super(databaseSession, properties, syncType);
    }

    public EntityManagerTestCase(EntityManagerFactoryDelegate factory, Map properties, SynchronizationType syncType) {
        super(factory, properties, syncType);
    }

    @Override
    public SynchronizationType getSyncType() {
        return super.getSyncType(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> List<EntityGraph<? super T>> getEntityGraphs(Class<T> entityClass) {
        return super.getEntityGraphs(entityClass); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityGraph getEntityGraph(String graphName) {
        return super.getEntityGraph(graphName); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityGraph createEntityGraph(String graphName) {
        return super.createEntityGraph(graphName); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> EntityGraph<T> createEntityGraph(Class<T> rootType) {
        return super.createEntityGraph(rootType); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isJoinedToTransaction() {
        return super.isJoinedToTransaction(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(CriteriaDelete deleteQuery) {
        return super.createQuery(deleteQuery); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(CriteriaUpdate updateQuery) {
        return super.createQuery(updateQuery); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object copy(Object entityOrEntities, AttributeGroup group) {
        return super.copy(entityOrEntities, group); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(Object entityOrEntities, AttributeGroup group) {
        super.load(entityOrEntities, group); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T unwrap(Class<T> cls) {
        return super.unwrap(cls); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<String> getSupportedProperties() {
        return super.getSupportedProperties(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, Object> getProperties() {
        return super.getProperties(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public LockModeType getLockMode(Object entity) {
        return super.getLockMode(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return super.getEntityManagerFactory(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Metamodel getMetamodel() {
        return super.getMetamodel(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HashMap<String, Object> getQueryHints(Object entity, OperationType operation) {
        return super.getQueryHints(entity, operation); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CriteriaBuilder getCriteriaBuilder() {
        return super.getCriteriaBuilder(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void detach(Object entity) {
        super.detach(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isBroker() {
        return super.isBroker(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void createConnectionPolicies(Map mapOfProperties) {
        super.createConnectionPolicies(mapOfProperties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void createConnectionPolicy() {
        super.createConnectionPolicy(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setJTATransactionWrapper() {
        super.setJTATransactionWrapper(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setFlushMode(FlushModeType flushMode) {
        super.setFlushMode(flushMode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setEntityTransactionWrapper() {
        super.setEntityTransactionWrapper(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected String getPropertiesHandlerProperty(String name) {
        return super.getPropertiesHandlerProperty(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void processProperties() {
        super.processProperties(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void setRollbackOnly() {
        super.setRollbackOnly(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void joinTransaction() {
        super.joinTransaction(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldBeginEarlyTransaction() {
        return super.shouldBeginEarlyTransaction(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean shouldFlushBeforeQuery() {
        return super.shouldFlushBeforeQuery(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Object checkForTransaction(boolean validateExistence) {
        return super.checkForTransaction(validateExistence); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean hasActivePersistenceContext() {
        return super.hasActivePersistenceContext(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperty(String propertyName, Object value) {
        super.setProperty(propertyName, value); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setProperties(Map properties) {
        super.setProperties(properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RepeatableWriteUnitOfWork getActivePersistenceContext(Object txn) {
        return super.getActivePersistenceContext(txn); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyOpenWithSetRollbackOnly() {
        super.verifyOpenWithSetRollbackOnly(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void verifyOpen() {
        super.verifyOpen(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lock(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        super.lock(entity, lockMode, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void lock(Object entity, LockModeType lockMode) {
        super.lock(entity, lockMode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isOpen() {
        return super.isOpen(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isFlushModeAUTO() {
        return super.isFlushModeAUTO(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void closeOpenQueries() {
        super.closeOpenQueries(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        super.close(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, String... resultSetMappings) {
        return super.createStoredProcedureQuery(procedureName, resultSetMappings); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName, Class... resultClasses) {
        return super.createStoredProcedureQuery(procedureName, resultClasses); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StoredProcedureQuery createStoredProcedureQuery(String procedureName) {
        return super.createStoredProcedureQuery(procedureName); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected DatabaseQuery createQueryInternal(Expression expression, Class resultType) {
        return super.createQueryInternal(expression, resultType); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> TypedQuery<T> createQuery(String qlString, Class<T> resultClass) {
        return super.createQuery(qlString, resultClass); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(String jpqlString) {
        return super.createQuery(jpqlString); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(Call call, Class entityClass) {
        return super.createQuery(call, entityClass); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(Call call) {
        return super.createQuery(call); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQueryByExample(Object exampleObject) {
        return super.createQueryByExample(exampleObject); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> TypedQuery<T> createQuery(CriteriaQuery<T> criteriaQuery) {
        return super.createQuery(criteriaQuery); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(DatabaseQuery databaseQuery) {
        return super.createQuery(databaseQuery); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createQuery(Expression expression, Class resultType) {
        return super.createQuery(expression, resultType); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getMemberSessionName(Class cls) {
        return super.getMemberSessionName(cls); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServerSession getMemberServerSession(Class cls) {
        return super.getMemberServerSession(cls); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseSessionImpl getMemberDatabaseSession(Class cls) {
        return super.getMemberDatabaseSession(cls); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SessionBroker getSessionBroker() {
        return super.getSessionBroker(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ServerSession getServerSession() {
        return super.getServerSession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setAbstractSession(AbstractSession session) {
        super.setAbstractSession(session); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractSession getAbstractSession() {
        return super.getAbstractSession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public DatabaseSessionImpl getDatabaseSession() {
        return super.getDatabaseSession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Session getReadOnlySession() {
        return super.getReadOnlySession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T getReference(Class<T> entityClass, Object primaryKey) {
        return super.getReference(entityClass, primaryKey); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ReadObjectQuery getReadObjectQuery(Object entity, Map properties) {
        return super.getReadObjectQuery(entity, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ReadObjectQuery getReadObjectQuery(Map properties) {
        return super.getReadObjectQuery(properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ReadObjectQuery getReadObjectQuery(Class referenceClass, Object primaryKey, Map properties) {
        return super.getReadObjectQuery(referenceClass, primaryKey, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getProperty(String name) {
        return super.getProperty(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public EntityTransaction getTransaction() {
        return super.getTransaction(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Session getSession() {
        return super.getSession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public UnitOfWork getUnitOfWork() {
        return super.getUnitOfWork(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FlushModeType getFlushMode() {
        return super.getFlushMode(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object getDelegate() {
        return super.getDelegate(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AbstractSession getActiveSessionIfExists() {
        return super.getActiveSessionIfExists(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Session getActiveSession() {
        return super.getActiveSession(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createNativeQuery(String sqlString, String resultSetMapping) {
        return super.createNativeQuery(sqlString, resultSetMapping); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createNativeQuery(String sqlString, Class resultType) {
        return super.createNativeQuery(sqlString, resultType); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createNativeQuery(String sqlString) {
        return super.createNativeQuery(sqlString); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public StoredProcedureQuery createNamedStoredProcedureQuery(String name) {
        return super.createNamedStoredProcedureQuery(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> TypedQuery<T> createNamedQuery(String name, Class<T> resultClass) {
        return super.createNamedQuery(name, resultClass); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createNamedQuery(String name) {
        return super.createNamedQuery(name); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createDescriptorNamedQuery(String queryName, Class descriptorClass, List argumentTypes) {
        return super.createDescriptorNamedQuery(queryName, descriptorClass, argumentTypes); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Query createDescriptorNamedQuery(String queryName, Class descriptorClass) {
        return super.createDescriptorNamedQuery(queryName, descriptorClass); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean contains(Object entity, UnitOfWork uow) {
        return super.contains(entity, uow); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(Object entity) {
        return super.contains(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode, Map<String, Object> properties) {
        super.refresh(entity, lockMode, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Object entity, LockModeType lockMode) {
        super.refresh(entity, lockMode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Object entity, Map<String, Object> properties) {
        super.refresh(entity, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh(Object entity) {
        super.refresh(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void detectTransactionWrapper() {
        super.detectTransactionWrapper(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void flush() {
        super.flush(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Object findInternal(ClassDescriptor descriptor, AbstractSession session, Object id, LockModeType lockMode, Map<String, Object> properties) {
        return super.findInternal(descriptor, session, id, lockMode, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Object find(String entityName, Object primaryKey) {
        return super.find(entityName, primaryKey); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode, Map<String, Object> properties) {
        return super.find(entityClass, primaryKey, lockMode, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, LockModeType lockMode) {
        return super.find(entityClass, primaryKey, lockMode); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey, Map<String, Object> properties) {
        return super.find(entityClass, primaryKey, properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T find(Class<T> entityClass, Object primaryKey) {
        return super.find(entityClass, primaryKey); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void remove(Object entity) {
        super.remove(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Object mergeInternal(Object entity) {
        return super.mergeInternal(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public <T> T merge(T entity) {
        return super.merge(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void persist(Object entity) {
        super.persist(entity); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeExtendedPersistenceContext() {
        super.removeExtendedPersistenceContext(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        super.clear(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected void initialize(Map properties) {
        super.initialize(properties); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addOpenQuery(QueryImpl query) {
        super.addOpenQuery(query); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Set<QueryImpl> getOpenQueriesSet() {
        return super.getOpenQueriesSet(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Map<QueryImpl, QueryImpl> getOpenQueriesMap() {
        return super.getOpenQueriesMap(); //To change body of generated methods, choose Tools | Templates.
    }
    
}
