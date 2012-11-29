package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * <p>
 * This interface exposes advanced {@link Datastore} features,
 * like interacting with DBObject and low-level options.
 * <p/>
 * <ul>
 * <li>Implements matching methods from the {@code Datastore} interface
 * but with a specified kind (collection name), or raw types (DBObject).
 * </li>
 * </ul>
 * </p>
 *
 * @author ScottHernandez
 */
public interface AdvancedDatastore extends Datastore {
    /**
     * Creates a reference to the entity (using the current DB -can be null-,
     * the collectionName, and id).
     */
    <T, V> DBRef createRef(Class<T> clazz, V id);

    /**
     * Creates a reference to the entity (using the current DB -can be null-,
     * the collectionName, and id).
     */
    <T> DBRef createRef(T entity);

    /**
     * Find the given entity (by collectionName/id).
     */
    <T> T get(Class<T> clazz, DBRef ref);

    /**
     * Gets the count this kind.
     */
    long getCount(String kind);

    /**
     *
     * @param kind
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    <T, V> T get(String kind, Class<T> clazz, V id);

    /**
     *
     * @param kind
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Query<T> find(String kind, Class<T> clazz);

    /**
     *
     * @param kind
     * @param clazz
     * @param property
     * @param value
     * @param offset
     * @param size
     * @param <T>
     * @param <V>
     * @return
     */
    <T, V> Query<T> find(String kind, Class<T> clazz, String property,
                         V value, int offset, int size);

    /**
     *
     * @param kind
     * @param entity
     * @param <T>
     * @return
     */
    <T> Key<T> save(String kind, T entity);

    /**
     * No validation or conversion is done to the id.
     */
    @Deprecated
    <T> WriteResult delete(String kind, T id);

    /**
     *
     * @param kind
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    <T, V> WriteResult delete(String kind, Class<T> clazz, V id);

    /**
     *
     * @param kind
     * @param entity
     * @param <T>
     * @return
     */
    <T> Key<T> insert(String kind, T entity);

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    <T> Key<T> insert(T entity);

    /**
     *
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    <T> Key<T> insert(T entity, WriteConcern wc);

    /**
     *
     * @param entities
     * @param <T>
     * @return
     */
    <T> Iterable<Key<T>> insert(T... entities);

    /**
     *
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    <T> Iterable<Key<T>> insert(Iterable<T> entities, WriteConcern wc);

    /**
     *
     * @param kind
     * @param entities
     * @param <T>
     * @return
     */
    <T> Iterable<Key<T>> insert(String kind, Iterable<T> entities);

    /**
     *
     * @param kind
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    <T> Iterable<Key<T>> insert(String kind, Iterable<T> entities,
                                WriteConcern wc);

    /**
     *
     * @param kind
     * @param clazz
     * @param <T>
     * @return
     */
    <T> Query<T> createQuery(String kind, Class<T> clazz);

    /**
     * DBObject implementations; in case we don't have features impl'd yet.
     */
    <T> Query<T> createQuery(Class<T> kind, DBObject q);

    /**
     * @param kind
     * @param clazz
     * @param q
     * @param <T>
     * @return
     */
    <T> Query<T> createQuery(String kind, Class<T> clazz, DBObject q);

    /**
     * Returns a new query based on the example object.
     */
    <T> Query<T> queryByExample(String kind, T example);

    /**
     * @param kind
     * @param ops
     * @param <T>
     * @return
     */
    <T> UpdateOperations<T> createUpdateOperations(Class<T> kind,
                                                   DBObject ops);

    /**
     * @param fact
     * @return
     */
    DBDecoderFactory setDecoderFact(DBDecoderFactory fact);

    /**
     * @return
     */
    DBDecoderFactory getDecoderFact();
}