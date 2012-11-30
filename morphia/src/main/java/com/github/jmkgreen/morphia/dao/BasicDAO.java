package com.github.jmkgreen.morphia.dao;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.DatastoreImpl;
import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.QueryResults;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.github.jmkgreen.morphia.query.UpdateResults;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * The base class for DAO style data access through Morphia.
 * <p/>
 * Imagine you have an Account entity. Build an AccountDAO that extends
 * this class and accepts Account entities for saving.
 * Your AccountDAO subclass inherits the generic methods here, and you
 * augment with custom finders and manipulators as required within your
 * subclasses.
 *
 * @author Olafur Gauti Gudmundsson
 * @author Scott Hernandez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class BasicDAO<T, K> implements DAO<T, K> {

    protected Class<T> entityClazz;
    protected Datastore ds;

    public BasicDAO(Class<T> entityClass, Mongo mongo, Morphia morphia, String dbName) {
        initDS(mongo, morphia, dbName);
        initType(entityClass);
    }

    public BasicDAO(Class<T> entityClass, Datastore ds) {
        this.ds = ds;
        initType(entityClass);
    }

    /**
     * <p> Only calls this from your derived class when you explicitly declare the generic types with concrete classes </p>
     * <p>
     * {@code class MyDao extends DAO<MyEntity, String>}
     * </p>
     */
    protected BasicDAO(Mongo mongo, Morphia morphia, String dbName) {
        initDS(mongo, morphia, dbName);
        initType(((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
    }

    protected BasicDAO(Datastore ds) {
        this.ds = ds;
        initType(((Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]));
    }

    protected void initType(Class<T> type) {
        this.entityClazz = type;
        ds.getMapper().addMappedClass(type);
    }

    protected void initDS(Mongo mon, Morphia mor, String db) {
        ds = new DatastoreImpl(mor, mon, db);
    }

    /**
     * Converts from a List<Key> to their id values
     *
     * @param keys
     * @return The ids of the keys
     */
    protected List<?> keysToIds(List<Key<T>> keys) {
        ArrayList ids = new ArrayList(keys.size() * 2);
        for (Key<T> key : keys)
            ids.add(key.getId());
        return ids;
    }

    /**
     * The underlying collection for this DAO
     */
    public DBCollection getCollection() {
        return ds.getCollection(entityClazz);
    }


    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#createQuery()
      */
    public Query<T> createQuery() {
        return ds.createQuery(entityClazz);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#createUpdateOperations()
      */
    public UpdateOperations<T> createUpdateOperations() {
        return ds.createUpdateOperations(entityClazz);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#getEntityClass()
      */
    public Class<T> getEntityClass() {
        return entityClazz;
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#save(T)
      */
    public Key<T> save(T entity) {
        return ds.save(entity);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#save(T, com.mongodb.WriteConcern)
      */
    public Key<T> save(T entity, WriteConcern wc) {
        return ds.save(entity, wc);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#updateFirst(com.github.jmkgreen.morphia.query.Query, com.github.jmkgreen.morphia.query.UpdateOperations)
      */
    public UpdateResults<T> updateFirst(Query<T> q, UpdateOperations<T> ops) {
        return ds.updateFirst(q, ops);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#update(com.github.jmkgreen.morphia.query.Query, com.github.jmkgreen.morphia.query.UpdateOperations)
      */
    public UpdateResults<T> update(Query<T> q, UpdateOperations<T> ops) {
        return ds.update(q, ops);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#delete(T)
      */
    public WriteResult delete(T entity) {
        return ds.delete(entity);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#delete(T, com.mongodb.WriteConcern)
      */
    public WriteResult delete(T entity, WriteConcern wc) {
        return ds.delete(entity, wc);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#deleteById(K)
      */
    public WriteResult deleteById(K id) {
        return ds.delete(entityClazz, id);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#deleteByQuery(com.github.jmkgreen.morphia.query.Query)
      */
    public WriteResult deleteByQuery(Query<T> q) {
        return ds.delete(q);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#get(K)
      */
    public T get(K id) {
        return ds.get(entityClazz, id);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#findIds(java.lang.String, java.lang.Object)
      */
    public List<K> findIds(String key, Object value) {
        return (List<K>) keysToIds(ds.find(entityClazz, key, value).asKeyList());
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#findIds()
      */
    public List<K> findIds() {
        return (List<K>) keysToIds(ds.find(entityClazz).asKeyList());
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#findIds(com.github.jmkgreen.morphia.query.Query)
      */
    public List<K> findIds(Query<T> q) {
        return (List<K>) keysToIds(q.asKeyList());
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#exists(java.lang.String, java.lang.Object)
      */
    public boolean exists(String key, Object value) {
        return exists(ds.find(entityClazz, key, value));
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#exists(com.github.jmkgreen.morphia.query.Query)
      */
    public boolean exists(Query<T> q) {
        return ds.getCount(q) > 0;
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#count()
      */
    public long count() {
        return ds.getCount(entityClazz);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#count(java.lang.String, java.lang.Object)
      */
    public long count(String key, Object value) {
        return count(ds.find(entityClazz, key, value));
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#count(com.github.jmkgreen.morphia.query.Query)
      */
    public long count(Query<T> q) {
        return ds.getCount(q);
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#findOne(java.lang.String, java.lang.Object)
      */
    public T findOne(String key, Object value) {
        return ds.find(entityClazz, key, value).get();
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#findOne(com.github.jmkgreen.morphia.query.Query)
      */
    public T findOne(Query<T> q) {
        return q.get();
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#find()
      */
    public QueryResults<T> find() {
        return createQuery();
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#find(com.github.jmkgreen.morphia.query.Query)
      */
    public QueryResults<T> find(Query<T> q) {
        return q;
    }

    /* (non-Javadoc)
      * @see com.github.jmkgreen.morphia.DAO#getDatastore()
      */
    public Datastore getDatastore() {
        return ds;
    }

    public void ensureIndexes() {
        ds.ensureIndexes(entityClazz);
    }

}
