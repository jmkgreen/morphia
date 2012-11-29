package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.EntityInterceptor;
import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.converters.DefaultConverters;
import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import java.util.Collection;
import java.util.Map;

/**
 * Maps POJOs and MongoDB DBObjects. Holds a list of {@link EntityInterceptor}s
 * that are invoked during lifecycle phases across all mapped entity classes.
 * <p/>
 * Implementations should be thread-safe.
 */
public interface Mapper {
    /**
     * The @{@link com.github.jmkgreen.morphia.annotations.Id} field name that is stored with mongodb.
     */
    String ID_KEY = "_id";
    /**
     * Special name that can never be used. Used as default for some fields to indicate default state.
     */
    String IGNORED_FIELDNAME = ".";
    /**
     * Special field used by morphia to support various possibly loading issues; will be replaced when discriminators are implemented to support polymorphism
     */
    String CLASS_NAME_FIELDNAME = "className";

    void addInterceptor(EntityInterceptor ei);

    Collection<EntityInterceptor> getInterceptors();

    MapperOptions getOptions();

    void setOptions(MapperOptions options);

    boolean isMapped(Class c);

    MappedClass addMappedClass(Class c);

    MappedClass addMappedClass(MappedClass mc);

    Collection<MappedClass> getMappedClasses();

    Map<String, MappedClass> getMCMap();

    MappedClass getMappedClass(Object obj);

    String getCollectionName(Object object);

    void updateKeyInfo(Object entity, DBObject dbObj, EntityCache cache);

    Object fromDBObject(Class entityClass, DBObject dbObject, EntityCache cache);

    Object toMongoObject(MappedField mf, MappedClass mc, Object value);

    Object getId(Object entity);

    <T> Key<T> getKey(T entity);

    DBObject toDBObject(Object entity);

    DBObject toDBObject(Object entity, Map<Object, DBObject> involvedObjects);

    // TODO might be better to expose via some "options" object?
    DefaultConverters getConverters();

    EntityCache createEntityCache();

    <T> Key<T> refToKey(DBRef ref);

    DBRef keyToRef(Key key);

    String updateKind(Key key);

    Class<?> getClassFromKind(String kind);
}
