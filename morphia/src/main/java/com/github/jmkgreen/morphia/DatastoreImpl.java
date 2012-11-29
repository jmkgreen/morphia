package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.annotations.CappedAt;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.annotations.Index;
import com.github.jmkgreen.morphia.annotations.Indexed;
import com.github.jmkgreen.morphia.annotations.Indexes;
import com.github.jmkgreen.morphia.annotations.NotSaved;
import com.github.jmkgreen.morphia.annotations.PostPersist;
import com.github.jmkgreen.morphia.annotations.Reference;
import com.github.jmkgreen.morphia.annotations.Serialized;
import com.github.jmkgreen.morphia.annotations.Version;
import com.github.jmkgreen.morphia.logging.Logr;
import com.github.jmkgreen.morphia.logging.MorphiaLoggerFactory;
import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.github.jmkgreen.morphia.mapping.lazy.DatastoreHolder;
import com.github.jmkgreen.morphia.mapping.lazy.proxy.ProxyHelper;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.QueryException;
import com.github.jmkgreen.morphia.query.QueryImpl;
import com.github.jmkgreen.morphia.query.UpdateException;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.github.jmkgreen.morphia.query.UpdateOpsImpl;
import com.github.jmkgreen.morphia.query.UpdateResults;
import com.github.jmkgreen.morphia.utils.Assert;
import com.github.jmkgreen.morphia.utils.IndexDirection;
import com.github.jmkgreen.morphia.utils.IndexFieldDef;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBDecoderFactory;
import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceCommand.OutputType;
import com.mongodb.MapReduceOutput;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A generic (type-safe) wrapper around mongodb collections.
 *
 * @author Scott Hernandez
 */
@SuppressWarnings({"unchecked", "deprecation"})
public class DatastoreImpl implements Datastore, AdvancedDatastore {
    /**
     *
     */
    private static final Logr log = MorphiaLoggerFactory.get(DatastoreImpl.class);

    //	final protected Morphia morphia;
    /**
     *
     */
    protected Mapper mapr;

    /**
     *
     */
    protected Mongo mongo;

    /**
     *
     */
    protected DB db;

    /**
     *
     */
    protected WriteConcern defConcern = WriteConcern.SAFE;

    /**
     *
     */
    protected DBDecoderFactory decoderFactory = null;

    /**
     *
     * @param mapr
     * @param mongo
     * @param dbName
     */
    public DatastoreImpl(Mapper mapr, Mongo mongo, String dbName) {
        this.mapr = mapr;
        this.mongo = mongo;
        this.db = mongo.getDB(dbName);

        // VERY discussable
        DatastoreHolder.getInstance().set(this);
    }

    /**
     *
     * @param morphia
     * @param mongo
     */
    public DatastoreImpl(Morphia morphia, Mongo mongo) {
        this(morphia, mongo, null);
    }

    /**
     *
     * @param morphia
     * @param mongo
     * @param dbName
     * @param username
     * @param password
     */
    public DatastoreImpl(Morphia morphia, Mongo mongo, String dbName, String username, char[] password) {
        this(morphia.getMapper(), mongo, dbName);

        if (username != null)
            if (!this.db.authenticate(username, password))
                throw new AuthenticationException("User '" + username
                        + "' cannot be authenticated with the given password for database '" + dbName + "'");

    }

    /**
     *
     * @param morphia
     * @param mongo
     * @param dbName
     */
    public DatastoreImpl(Morphia morphia, Mongo mongo, String dbName) {
        this(morphia.getMapper(), mongo, dbName);
    }

    /**
     *
     * @param db
     * @return
     */
    public Datastore copy(String db) {
        return new DatastoreImpl(mapr, mongo, db);
    }

    /**
     *
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> DBRef createRef(Class<T> clazz, V id) {
        if (id == null)
            throw new MappingException("Could not get id for " + clazz.getName());
        return new DBRef(getDB(), getCollection(clazz).getName(), id);
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> DBRef createRef(T entity) {
        entity = ProxyHelper.unwrap(entity);
        Object id = getId(entity);
        if (id == null)
            throw new MappingException("Could not get id for " + entity.getClass().getName());
        return createRef(entity.getClass(), id);
    }

    /**
     *
     * @param entity
     * @return
     */
    @Deprecated
    protected Object getId(Object entity) {
        return mapr.getId(entity);
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    @Deprecated // use mapper instead.
    public <T> Key<T> getKey(T entity) {
        return mapr.getKey(entity);
    }

    /**
     *
     * @param kind
     * @param id
     * @param <T>
     * @return
     */
    public <T> WriteResult delete(String kind, T id) {
        DBCollection dbColl = getCollection(kind);
        WriteResult wr = dbColl.remove(BasicDBObjectBuilder.start().add(Mapper.ID_KEY, id).get());
        throwOnError(null, wr);
        return wr;
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> WriteResult delete(String kind, Class<T> clazz, V id) {
        return delete(find(kind, clazz).filter(Mapper.ID_KEY, id));
    }

    /**
     *
     * @param clazz
     * @param id
     * @param wc
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> WriteResult delete(Class<T> clazz, V id, WriteConcern wc) {
        return delete(createQuery(clazz).filter(Mapper.ID_KEY, id), wc);
    }

    /**
     *
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> WriteResult delete(Class<T> clazz, V id) {
        return delete(clazz, id, getWriteConcern(clazz));
    }

    /**
     *
     * @param clazz
     * @param ids
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> WriteResult delete(Class<T> clazz, Iterable<V> ids) {
        Query<T> q = find(clazz).disableValidation().filter(Mapper.ID_KEY + " in", ids);
        return delete(q);
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> WriteResult delete(T entity) {
        return delete(entity, getWriteConcern(entity));
    }

    /**
     *
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    public <T> WriteResult delete(T entity, WriteConcern wc) {
        entity = ProxyHelper.unwrap(entity);
        if (entity instanceof Class<?>)
            throw new MappingException("Did you mean to delete all documents? -- delete(ds.createQuery(???.class))");
        try {
            Object id = getId(entity);
            return delete(entity.getClass(), id, wc);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param query
     * @param <T>
     * @return
     */
    public <T> WriteResult delete(Query<T> query) {
        return delete(query, getWriteConcern(query.getEntityClass()));
    }

    /**
     *
     * @param query
     * @param wc
     * @param <T>
     * @return
     */
    public <T> WriteResult delete(Query<T> query, WriteConcern wc) {
        QueryImpl<T> q = (QueryImpl<T>) query;

        DBCollection dbColl = q.getCollection();
        //TODO remove this after testing.
        if (dbColl == null)
            dbColl = getCollection(q.getEntityClass());

        WriteResult wr;

        if (q.getSortObject() != null || q.getOffset() != 0 || q.getLimit() > 0)
            throw new QueryException("Delete does not allow sort/offset/limit query options.");

        if (q.getQueryObject() != null)
            if (wc == null)
                wr = dbColl.remove(q.getQueryObject());
            else
                wr = dbColl.remove(q.getQueryObject(), wc);
        else if (wc == null)
            wr = dbColl.remove(new BasicDBObject());
        else
            wr = dbColl.remove(new BasicDBObject(), wc);

        throwOnError(wc, wr);

        return wr;
    }

    /**
     *
     * @param type
     * @param fields
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> type, String fields) {
        ensureIndex(type, null, fields, false, false);
    }

    /**
     *
     * @param clazz
     * @param name
     * @param defs
     * @param unique
     * @param dropDupsOnCreate
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> clazz, String name, IndexFieldDef[] defs, boolean unique, boolean dropDupsOnCreate) {
        ensureIndex(clazz, name, defs, unique, dropDupsOnCreate, false);
    }

    /**
     *
     * @param clazz
     * @param name
     * @param fields
     * @param unique
     * @param dropDupsOnCreate
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> clazz, String name, String fields, boolean unique, boolean dropDupsOnCreate) {
        ensureIndex(clazz, name, QueryImpl.parseFieldsString(fields, clazz, mapr, true), unique, dropDupsOnCreate, false, false);
    }

    /**
     *
     * @param clazz
     * @param name
     * @param fields
     * @param unique
     * @param dropDupsOnCreate
     * @param background
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> clazz, String name, String fields, boolean unique, boolean dropDupsOnCreate, boolean background) {
        ensureIndex(clazz, name, QueryImpl.parseFieldsString(fields, clazz, mapr, true), unique, dropDupsOnCreate, background, false);
    }

    /**
     *
     * @param clazz
     * @param name
     * @param fields
     * @param unique
     * @param dropDupsOnCreate
     * @param background
     * @param sparse
     * @param <T>
     */
    protected <T> void ensureIndex(Class<T> clazz, String name, BasicDBObject fields, boolean unique, boolean dropDupsOnCreate, boolean background, boolean sparse) {
        BasicDBObjectBuilder keyOpts = new BasicDBObjectBuilder();
        if (name != null && name.length() > 0) {
            keyOpts.add("name", name);
        }
        if (unique) {
            keyOpts.add("unique", true);
            if (dropDupsOnCreate)
                keyOpts.add("dropDups", true);
        }

        if (background)
            keyOpts.add("background", true);
        if (sparse)
            keyOpts.add("sparse", true);

        DBCollection dbColl = getCollection(clazz);

        BasicDBObject opts = (BasicDBObject) keyOpts.get();
        if (opts.isEmpty()) {
            log.debug("Ensuring index for " + dbColl.getName() + " with keys:" + fields);
            dbColl.ensureIndex(fields);
        } else {
            log.debug("Ensuring index for " + dbColl.getName() + " with keys:" + fields + " and opts:" + opts);
            dbColl.ensureIndex(fields, opts);
        }
    }

    /**
     *
     * @param clazz
     * @param name
     * @param defs
     * @param unique
     * @param dropDupsOnCreate
     * @param background
     */
    @SuppressWarnings({"rawtypes"})
    public void ensureIndex(Class clazz, String name, IndexFieldDef[] defs, boolean unique, boolean dropDupsOnCreate, boolean background) {
        BasicDBObjectBuilder keys = BasicDBObjectBuilder.start();

        for (IndexFieldDef def : defs) {
            String fieldName = def.getField();
            IndexDirection dir = def.getDirection();
            keys.add(fieldName, dir.toIndexValue());
        }

        ensureIndex(clazz, name, (BasicDBObject) keys.get(), unique, dropDupsOnCreate, background, false);
    }

    /**
     *
     * @param type
     * @param name
     * @param dir
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> type, String name, IndexDirection dir) {
        ensureIndex(type, new IndexFieldDef(name, dir));
    }

    /**
     *
     * @param type
     * @param fields
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> type, IndexFieldDef... fields) {
        ensureIndex(type, null, fields, false, false);
    }

    /**
     *
     * @param type
     * @param background
     * @param fields
     * @param <T>
     */
    public <T> void ensureIndex(Class<T> type, boolean background, IndexFieldDef... fields) {
        ensureIndex(type, null, fields, false, false, background);
    }

    /**
     *
     * @param mc
     * @param background
     */
    protected void ensureIndexes(MappedClass mc, boolean background) {
        ensureIndexes(mc, background, new ArrayList<MappedClass>(), new ArrayList<MappedField>());
    }

    /**
     *
     * @param mc
     * @param background
     * @param parentMCs
     * @param parentMFs
     */
    protected void ensureIndexes(MappedClass mc, boolean background, ArrayList<MappedClass> parentMCs, ArrayList<MappedField> parentMFs) {
        if (parentMCs.contains(mc))
            return;

        //skip embedded types
        if (mc.getEmbeddedAnnotation() != null && (parentMCs == null || parentMCs.isEmpty()))
            return;

        //Ensure indexes from class annotation
        ArrayList<Annotation> idxs = mc.getAnnotations(Indexes.class);
        if (idxs != null)
            for (Annotation ann : idxs) {
                Indexes idx = (Indexes) ann;
                if (idx != null && idx.value() != null && idx.value().length > 0)
                    for (Index index : idx.value()) {
                        BasicDBObject fields = QueryImpl.parseFieldsString(index.value(), mc.getClazz(), mapr, !index.disableValidation());
                        ensureIndex(mc.getClazz(), index.name(), fields, index.unique(), index.dropDups(), index.background() ? index.background() : background, index.sparse() ? index.sparse() : false);
                    }
            }
        //Ensure indexes from field annotations, and embedded entities
        for (MappedField mf : mc.getPersistenceFields()) {
            if (mf.hasAnnotation(Indexed.class)) {
                Indexed index = mf.getAnnotation(Indexed.class);
                StringBuilder field = new StringBuilder();
                Class<?> indexedClass = (parentMCs.isEmpty() ? mc : parentMCs.get(0)).getClazz();
                if (!parentMCs.isEmpty())
                    for (MappedField pmf : parentMFs)
                        field.append(pmf.getNameToStore()).append(".");

                field.append(mf.getNameToStore());

                ensureIndex(indexedClass, index.name(), new BasicDBObject(field.toString(), index.value().toIndexValue()), index.unique(), index.dropDups(), index.background() ? index.background() : background, index.sparse() ? index.sparse() : false);
            }

            if (!mf.isTypeMongoCompatible() && !mf.hasAnnotation(Reference.class) && !mf.hasAnnotation(Serialized.class)) {
                ArrayList<MappedClass> newParentClasses = (ArrayList<MappedClass>) parentMCs.clone();
                ArrayList<MappedField> newParents = (ArrayList<MappedField>) parentMFs.clone();
                newParentClasses.add(mc);
                newParents.add(mf);
                ensureIndexes(mapr.getMappedClass(mf.isSingleValue() ? mf.getType() : mf.getSubClass()), background, newParentClasses, newParents);
            }
        }
    }

    /**
     *
     * @param clazz
     * @param <T>
     */
    public <T> void ensureIndexes(Class<T> clazz) {
        ensureIndexes(clazz, false);
    }

    /**
     *
     * @param clazz
     * @param background
     * @param <T>
     */
    public <T> void ensureIndexes(Class<T> clazz, boolean background) {
        MappedClass mc = mapr.getMappedClass(clazz);
        ensureIndexes(mc, background);
    }

    /**
     *
     */
    public void ensureIndexes() {
        ensureIndexes(false);
    }

    /**
     *
     * @param background
     */
    public void ensureIndexes(boolean background) {
        // loops over mappedClasses and call ensureIndex for each @Entity object
        // (for now)
        for (MappedClass mc : mapr.getMappedClasses()) {
            ensureIndexes(mc, background);
        }
    }

    /**
     *
     */
    public void ensureCaps() {
        for (MappedClass mc : mapr.getMappedClasses())
            if (mc.getEntityAnnotation() != null && mc.getEntityAnnotation().cap().value() > 0) {
                CappedAt cap = mc.getEntityAnnotation().cap();
                String collName = mapr.getCollectionName(mc.getClazz());
                BasicDBObjectBuilder dbCapOpts = BasicDBObjectBuilder.start("capped", true);
                if (cap.value() > 0)
                    dbCapOpts.add("size", cap.value());
                if (cap.count() > 0)
                    dbCapOpts.add("max", cap.count());
                DB db = getDB();
                if (db.getCollectionNames().contains(collName)) {
                    DBObject dbResult = db.command(BasicDBObjectBuilder.start("collstats", collName).get());
                    if (dbResult.containsField("capped")) {
                        // TODO: check the cap options.
                        log.warning("DBCollection already exists is cap'd already; doing nothing. " + dbResult);
                    } else {
                        log.warning("DBCollection already exists with same name(" + collName
                                + ") and is not cap'd; not creating cap'd version!");
                    }
                } else {
                    getDB().createCollection(collName, dbCapOpts.get());
                    log.debug("Created cap'd DBCollection (" + collName + ") with opts " + dbCapOpts);
                }
            }
    }

    /**
     *
     * @param ex
     * @param <T>
     * @return
     */
    public <T> Query<T> queryByExample(T ex) {
        return queryByExample(getCollection(ex), ex);
    }

    /**
     *
     * @param kind
     * @param ex
     * @param <T>
     * @return
     */
    public <T> Query<T> queryByExample(String kind, T ex) {
        return queryByExample(db.getCollection(kind), ex);
    }

    /**
     *
     * @param coll
     * @param example
     * @param <T>
     * @return
     */
    private <T> Query<T> queryByExample(DBCollection coll, T example) {
        //TODO: think about remove className from baseQuery param below.
        return new QueryImpl<T>((Class<T>) example.getClass(), coll, this, entityToDBObj(example, new HashMap<Object, DBObject>()));

    }

    /**
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Query<T> createQuery(Class<T> clazz) {
        return new QueryImpl<T>(clazz, getCollection(clazz), this);
    }

    /**
     *
     * @param kind
     * @param q
     * @param <T>
     * @return
     */
    public <T> Query<T> createQuery(Class<T> kind, DBObject q) {
        return new QueryImpl<T>(kind, getCollection(kind), this, q);
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param q
     * @param <T>
     * @return
     */
    public <T> Query<T> createQuery(String kind, Class<T> clazz, DBObject q) {
        return new QueryImpl<T>(clazz, db.getCollection(kind), this, q);
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Query<T> createQuery(String kind, Class<T> clazz) {
        return new QueryImpl<T>(clazz, db.getCollection(kind), this);
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Query<T> find(String kind, Class<T> clazz) {
        return new QueryImpl<T>(clazz, getCollection(kind), this);
    }

    /**
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Query<T> find(Class<T> clazz) {
        return createQuery(clazz);
    }

    /**
     *
     * @param clazz
     * @param property
     * @param value
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> Query<T> find(Class<T> clazz, String property, V value) {
        Query<T> query = createQuery(clazz);
        return query.filter(property, value);
    }

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
    public <T, V> Query<T> find(String kind, Class<T> clazz, String property, V value, int offset, int size) {
        return find(kind, clazz, property, value, offset, size, true);
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param property
     * @param value
     * @param offset
     * @param size
     * @param validate
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> Query<T> find(String kind, Class<T> clazz, String property, V value, int offset, int size,
                                boolean validate) {
        Query<T> query = find(kind, clazz);
        if (!validate)
            query.disableValidation();
        query.offset(offset);
        query.limit(size);
        return query.filter(property, value).enableValidation();
    }

    /**
     *
     * @param clazz
     * @param property
     * @param value
     * @param offset
     * @param size
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> Query<T> find(Class<T> clazz, String property, V value, int offset, int size) {
        Query<T> query = createQuery(clazz);
        query.offset(offset);
        query.limit(size);
        return query.filter(property, value);
    }

    /**
     *
     * @param clazz
     * @param ref
     * @param <T>
     * @return
     */
    public <T> T get(Class<T> clazz, DBRef ref) {
        return (T) mapr.fromDBObject(clazz, ref.fetch(), createCache());
    }

    /**
     *
     * @param clazz
     * @param ids
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> Query<T> get(Class<T> clazz, Iterable<V> ids) {
        return find(clazz).disableValidation().filter(Mapper.ID_KEY + " in", ids).enableValidation();
    }

    /**
     * Queries the server to check for each DBRef.
     */
    public <T> List<Key<T>> getKeysByRefs(List<DBRef> refs) {
        ArrayList<Key<T>> tempKeys = new ArrayList<Key<T>>(refs.size());

        Map<String, List<DBRef>> kindMap = new HashMap<String, List<DBRef>>();
        for (DBRef ref : refs) {
            if (kindMap.containsKey(ref.getRef()))
                kindMap.get(ref.getRef()).add(ref);
            else
                kindMap.put(ref.getRef(), new ArrayList<DBRef>(Collections.singletonList((DBRef) ref)));
        }
        for (String kind : kindMap.keySet()) {
            List<Object> objIds = new ArrayList<Object>();
            List<DBRef> kindRefs = kindMap.get(kind);
            for (DBRef key : kindRefs) {
                objIds.add(key.getId());
            }
            List<Key<T>> kindResults = this.<T>find(kind, null).disableValidation().filter("_id in", objIds).asKeyList();
            tempKeys.addAll(kindResults);
        }

        //put them back in order, minus the missing ones.
        ArrayList<Key<T>> keys = new ArrayList<Key<T>>(refs.size());
        for (DBRef ref : refs) {
            Key<T> testKey = mapr.refToKey(ref);
            if (tempKeys.contains(testKey))
                keys.add(testKey);
        }
        return keys;
    }

    /**
     *
     * @param keys
     * @param <T>
     * @return
     */
    public <T> List<T> getByKeys(Iterable<Key<T>> keys) {
        return this.getByKeys((Class<T>) null, keys);
    }

    /**
     *
     * @param clazz
     * @param keys
     * @param <T>
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <T> List<T> getByKeys(Class<T> clazz, Iterable<Key<T>> keys) {

        Map<String, List<Key>> kindMap = new HashMap<String, List<Key>>();
        List<T> entities = new ArrayList<T>();
        // String clazzKind = (clazz==null) ? null :
        // getMapper().getCollectionName(clazz);
        for (Key<?> key : keys) {
            mapr.updateKind(key);

            // if (clazzKind != null && !key.getKind().equals(clazzKind))
            // throw new IllegalArgumentException("Types are not equal (" +
            // clazz + "!=" + key.getKindClass() +
            // ") for key and method parameter clazz");
            //
            if (kindMap.containsKey(key.getKind()))
                kindMap.get(key.getKind()).add(key);
            else
                kindMap.put(key.getKind(), new ArrayList<Key>(Collections.singletonList((Key) key)));
        }
        for (String kind : kindMap.keySet()) {
            List<Object> objIds = new ArrayList<Object>();
            List<Key> kindKeys = kindMap.get(kind);
            for (Key key : kindKeys) {
                objIds.add(key.getId());
            }
            List kindResults = find(kind, null).disableValidation().filter("_id in", objIds).asList();
            entities.addAll(kindResults);
        }

        //TODO: order them based on the incoming Keys.
        return entities;
    }

    /**
     *
     * @param kind
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> T get(String kind, Class<T> clazz, V id) {
        List<T> results = find(kind, clazz, Mapper.ID_KEY, id, 0, 1).asList();
        if (results == null || results.size() == 0)
            return null;
        return results.get(0);
    }

    /**
     *
     * @param clazz
     * @param id
     * @param <T>
     * @param <V>
     * @return
     */
    public <T, V> T get(Class<T> clazz, V id) {
        return find(getCollection(clazz).getName(), clazz, Mapper.ID_KEY, id, 0, 1, true).get();
    }

    /**
     *
     * @param clazz
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getByKey(Class<T> clazz, Key<T> key) {
        String kind = mapr.getCollectionName(clazz);
        String keyKind = mapr.updateKind(key);
        if (!kind.equals(keyKind))
            throw new RuntimeException("collection names don't match for key and class: " + kind + " != " + keyKind);

        return get(clazz, key.getId());
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> T get(T entity) {
        entity = ProxyHelper.unwrap(entity);
        Object id = getId(entity);
        if (id == null)
            throw new MappingException("Could not get id for " + entity.getClass().getName());
        return (T) get(entity.getClass(), id);
    }

    /**
     *
     * @param entityOrKey
     * @return
     */
    public Key<?> exists(Object entityOrKey) {
        entityOrKey = ProxyHelper.unwrap(entityOrKey);
        Key<?> key = getKey(entityOrKey);
        Object id = key.getId();
        if (id == null)
            throw new MappingException("Could not get id for " + entityOrKey.getClass().getName());

        String collName = key.getKind();
        if (collName == null)
            collName = getCollection(key.getKindClass()).getName();

        return find(collName, key.getKindClass()).filter(Mapper.ID_KEY, key.getId()).getKey();
    }

    /**
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("rawtypes")
    public DBCollection getCollection(Class clazz) {
        String collName = mapr.getCollectionName(clazz);
        DBCollection dbC = getDB().getCollection(collName);
        return dbC;
    }

    /**
     *
     * @param obj
     * @return
     */
    public DBCollection getCollection(Object obj) {
        if (obj == null) return null;
        return getCollection(obj.getClass());
    }

    /**
     *
     * @param kind
     * @return
     */
    protected DBCollection getCollection(String kind) {
        if (kind == null) return null;
        return getDB().getCollection(kind);
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> long getCount(T entity) {
        entity = ProxyHelper.unwrap(entity);
        return getCollection(entity).count();
    }

    /**
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> long getCount(Class<T> clazz) {
        return getCollection(clazz).count();
    }

    /**
     *
     * @param kind
     * @return
     */
    public long getCount(String kind) {
        return getCollection(kind).count();
    }

    /**
     *
     * @param query
     * @param <T>
     * @return
     */
    public <T> long getCount(Query<T> query) {
        return query.countAll();
    }

    /**
     *
     * @return
     */
    public Mongo getMongo() {
        return this.mongo;
    }

    /**
     *
     * @return
     */
    public DB getDB() {
        return db;
    }

    /**
     *
     * @return
     */
    public Mapper getMapper() {
        return mapr;
    }

    /**
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> insert(Iterable<T> entities) {
        //TODO: try not to create two iterators...
        Object first = entities.iterator().next();
        return insert(entities, getWriteConcern(first));
    }

    /**
     *
     * @param kind
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> insert(String kind, Iterable<T> entities, WriteConcern wc) {
        DBCollection dbColl = db.getCollection(kind);
        return insert(dbColl, entities, wc);
    }

    /**
     *
     * @param kind
     * @param entities
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> insert(String kind, Iterable<T> entities) {
        return insert(kind, entities, getWriteConcern(entities.iterator().next()));
    }

    /**
     *
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> insert(Iterable<T> entities, WriteConcern wc) {
        //TODO: Do this without creating another iterator
        DBCollection dbColl = getCollection(entities.iterator().next());
        return insert(dbColl, entities, wc);
    }

    /**
     *
     * @param dbColl
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    private <T> Iterable<Key<T>> insert(DBCollection dbColl, Iterable<T> entities, WriteConcern wc) {
        ArrayList<DBObject> ents = entities instanceof List ? new ArrayList<DBObject>(((List<T>) entities).size()) : new ArrayList<DBObject>();

        Map<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        for (T ent : entities) {
            MappedClass mc = mapr.getMappedClass(ent);
            if (mc.getAnnotation(NotSaved.class) != null)
                throw new MappingException("Entity type: " + mc.getClazz().getName() + " is marked as NotSaved which means you should not try to save it!");
            ents.add(entityToDBObj(ent, involvedObjects));
        }

        WriteResult wr = null;

        DBObject[] dbObjs = new DBObject[ents.size()];
        dbColl.insert(ents.toArray(dbObjs), wc);

        throwOnError(wc, wr);

        ArrayList<Key<T>> savedKeys = new ArrayList<Key<T>>();
        Iterator<T> entitiesIT = entities.iterator();
        Iterator<DBObject> dbObjsIT = ents.iterator();

        while (entitiesIT.hasNext()) {
            T entity = entitiesIT.next();
            DBObject dbObj = dbObjsIT.next();
            savedKeys.add(postSaveGetKey(entity, dbObj, dbColl, involvedObjects));
        }

        return savedKeys;
    }

    /**
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> insert(T... entities) {
        return insert(Arrays.asList(entities), getWriteConcern(entities[0]));
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> Key<T> insert(T entity) {
        return insert(entity, getWriteConcern(entity));
    }

    /**
     *
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Key<T> insert(T entity, WriteConcern wc) {
        entity = ProxyHelper.unwrap(entity);
        DBCollection dbColl = getCollection(entity);
        return insert(dbColl, entity, wc);
    }

    /**
     *
     * @param kind
     * @param entity
     * @param <T>
     * @return
     */
    public <T> Key<T> insert(String kind, T entity) {
        entity = ProxyHelper.unwrap(entity);
        DBCollection dbColl = getCollection(kind);
        return insert(dbColl, entity, getWriteConcern(entity));
    }

    /**
     *
     * @param kind
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Key<T> insert(String kind, T entity, WriteConcern wc) {
        entity = ProxyHelper.unwrap(entity);
        DBCollection dbColl = getCollection(kind);
        return insert(dbColl, entity, wc);
    }

    /**
     *
     * @param dbColl
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    protected <T> Key<T> insert(DBCollection dbColl, T entity, WriteConcern wc) {
        LinkedHashMap<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        DBObject dbObj = entityToDBObj(entity, involvedObjects);
        WriteResult wr;
        if (wc == null)
            wr = dbColl.insert(dbObj);
        else
            wr = dbColl.insert(dbObj, wc);

        throwOnError(wc, wr);

        return postSaveGetKey(entity, dbObj, dbColl, involvedObjects);

    }

    /**
     *
     * @param entity
     * @param involvedObjects
     * @return
     */
    protected DBObject entityToDBObj(Object entity, Map<Object, DBObject> involvedObjects) {
        entity = ProxyHelper.unwrap(entity);
        DBObject dbObj = mapr.toDBObject(entity, involvedObjects);
        return dbObj;
    }

    /**
     * call postSaveOperations and returns Key for entity.
     */
    protected <T> Key<T> postSaveGetKey(T entity, DBObject dbObj, DBCollection dbColl, Map<Object, DBObject> involvedObjects) {
        if (dbObj.get(Mapper.ID_KEY) == null)
            throw new MappingException("Missing _id after save!");

        postSaveOperations(entity, dbObj, involvedObjects);
        Key<T> key = new Key<T>(dbColl.getName(), getId(entity));
        key.setKindClass((Class<? extends T>) entity.getClass());

        return key;
    }

    /**
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> save(Iterable<T> entities) {
        Object first = null;
        try {
            first = entities.iterator().next();
        } catch (Exception e) {
            //do nothing
        }
        return save(entities, getWriteConcern(first));
    }

    /**
     *
     * @param entities
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> save(Iterable<T> entities, WriteConcern wc) {
        ArrayList<Key<T>> savedKeys = new ArrayList<Key<T>>();
        for (T ent : entities)
            savedKeys.add(save(ent, wc));
        return savedKeys;

    }

    /**
     *
     * @param entities
     * @param <T>
     * @return
     */
    public <T> Iterable<Key<T>> save(T... entities) {
        ArrayList<Key<T>> savedKeys = new ArrayList<Key<T>>();
        for (T ent : entities)
            savedKeys.add(save(ent));
        return savedKeys;
    }

    /**
     *
     * @param dbColl
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    protected <T> Key<T> save(DBCollection dbColl, T entity, WriteConcern wc) {
        MappedClass mc = mapr.getMappedClass(entity);
        if (mc.getAnnotation(NotSaved.class) != null)
            throw new MappingException("Entity type: " + mc.getClazz().getName() + " is marked as NotSaved which means you should not try to save it!");

        WriteResult wr = null;

        //involvedObjects is used not only as a cache but also as a list of what needs to be called for life-cycle methods at the end.
        LinkedHashMap<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        DBObject dbObj = entityToDBObj(entity, involvedObjects);

        //try to do an update if there is a @Version field
        wr = tryVersionedUpdate(dbColl, entity, dbObj, wc, db, mc);

        if (wr == null)
            if (wc == null)
                wr = dbColl.save(dbObj);
            else
                wr = dbColl.save(dbObj, wc);

        throwOnError(wc, wr);
        return postSaveGetKey(entity, dbObj, dbColl, involvedObjects);
    }

    /**
     *
     * @param dbColl
     * @param entity
     * @param dbObj
     * @param wc
     * @param db
     * @param mc
     * @param <T>
     * @return
     */
    protected <T> WriteResult tryVersionedUpdate(DBCollection dbColl, T entity, DBObject dbObj, WriteConcern wc, DB db, MappedClass mc) {
        WriteResult wr = null;
        if (mc.getFieldsAnnotatedWith(Version.class).isEmpty())
            return wr;


        MappedField mfVersion = mc.getFieldsAnnotatedWith(Version.class).get(0);
        String versionKeyName = mfVersion.getNameToStore();
        Long oldVersion = (Long) mfVersion.getFieldValue(entity);
        long newVersion = VersionHelper.nextValue(oldVersion);
        dbObj.put(versionKeyName, newVersion);
        if (oldVersion != null && oldVersion > 0) {
            Object idValue = dbObj.get(Mapper.ID_KEY);

            UpdateResults<T> res = update(find(dbColl.getName(), (Class<T>) entity.getClass()).filter(Mapper.ID_KEY, idValue).filter(versionKeyName, oldVersion),
                    dbObj,
                    false,
                    false,
                    wc);

            wr = res.getWriteResult();

            if (res.getUpdatedCount() != 1)
                throw new ConcurrentModificationException("Entity of class " + entity.getClass().getName()
                        + " (id='" + idValue + "',version='" + oldVersion + "') was concurrently updated.");
        } else if (wc == null)
            wr = dbColl.save(dbObj);
        else
            wr = dbColl.save(dbObj, wc);

        //update the version.
        mfVersion.setFieldValue(entity, newVersion);
        return wr;
    }

    /**
     *
     * @param wc
     * @param wr
     */
    protected void throwOnError(WriteConcern wc, WriteResult wr) {
        if (wc == null && wr.getLastConcern() == null) {
            CommandResult cr = wr.getLastError();
            if (cr != null && cr.getErrorMessage() != null && cr.getErrorMessage().length() > 0)
                cr.throwOnError();
        }
    }

    /**
     *
     * @param kind
     * @param entity
     * @param <T>
     * @return
     */
    public <T> Key<T> save(String kind, T entity) {
        entity = ProxyHelper.unwrap(entity);
        DBCollection dbColl = getCollection(kind);
        return save(dbColl, entity, getWriteConcern(entity));
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> Key<T> save(T entity) {
        return save(entity, getWriteConcern(entity));
    }

    /**
     *
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Key<T> save(T entity, WriteConcern wc) {
        entity = ProxyHelper.unwrap(entity);
        DBCollection dbColl = getCollection(entity);
        return save(dbColl, entity, wc);
    }

    /**
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> UpdateOperations<T> createUpdateOperations(Class<T> clazz) {
        return new UpdateOpsImpl<T>(clazz, getMapper());
    }

    /**
     *
     * @param kind
     * @param ops
     * @param <T>
     * @return
     */
    public <T> UpdateOperations<T> createUpdateOperations(Class<T> kind, DBObject ops) {
        UpdateOpsImpl<T> upOps = (UpdateOpsImpl<T>) createUpdateOperations(kind);
        upOps.setOps(ops);
        return upOps;
    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> update(Query<T> query, UpdateOperations<T> ops, boolean createIfMissing) {
        return update(query, ops, createIfMissing, getWriteConcern(query.getEntityClass()));
    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param wc
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> update(Query<T> query, UpdateOperations<T> ops, boolean createIfMissing, WriteConcern wc) {
        return update(query, ops, createIfMissing, true, wc);
    }

    /**
     *
     * @param ent
     * @param ops
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> update(T ent, UpdateOperations<T> ops) {
        if (ent instanceof Query)
            return update((Query<T>) ent, ops);

        MappedClass mc = mapr.getMappedClass(ent);
        Query<T> q = (Query<T>) createQuery(mc.getClazz());
        q.disableValidation().filter(Mapper.ID_KEY, getId(ent));

        if (mc.getFieldsAnnotatedWith(Version.class).size() > 0) {
            MappedField versionMF = mc.getFieldsAnnotatedWith(Version.class).get(0);
            Long oldVer = (Long) versionMF.getFieldValue(ent);
            q.filter(versionMF.getNameToStore(), oldVer);
            ops.set(versionMF.getNameToStore(), VersionHelper.nextValue(oldVer));
        }

        return update(q, ops);
    }

    /**
     *
     * @param key
     * @param ops
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> update(Key<T> key, UpdateOperations<T> ops) {
        Class<T> clazz = (Class<T>) key.getKindClass();
        if (clazz == null)
            clazz = (Class<T>) mapr.getClassFromKind(key.getKind());
        return updateFirst(createQuery(clazz).disableValidation().filter(Mapper.ID_KEY, key.getId()), ops);
    }

    /**
     *
     * @param query
     * @param ops
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> update(Query<T> query, UpdateOperations<T> ops) {
        return update(query, ops, false, true);
    }

    /**
     *
     * @param query
     * @param ops
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> updateFirst(Query<T> query, UpdateOperations<T> ops) {
        return update(query, ops, false, false);
    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> updateFirst(Query<T> query, UpdateOperations<T> ops, boolean createIfMissing) {
        return update(query, ops, createIfMissing, getWriteConcern(query.getEntityClass()));

    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param wc
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> updateFirst(Query<T> query, UpdateOperations<T> ops, boolean createIfMissing, WriteConcern wc) {
        return update(query, ops, createIfMissing, false, wc);
    }

    /**
     *
     * @param query
     * @param entity
     * @param createIfMissing
     * @param <T>
     * @return
     */
    public <T> UpdateResults<T> updateFirst(Query<T> query, T entity, boolean createIfMissing) {
        LinkedHashMap<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        DBObject dbObj = mapr.toDBObject(entity, involvedObjects);

        UpdateResults<T> res = update(query, dbObj, createIfMissing, false, getWriteConcern(entity));

        //update _id field
        CommandResult gle = res.getWriteResult().getCachedLastError();
        if (gle != null && res.getInsertedCount() > 0)
            dbObj.put(Mapper.ID_KEY, res.getNewId());

        postSaveOperations(entity, dbObj, involvedObjects);
        return res;
    }

    /**
     *
     * @param entity
     * @param <T>
     * @return
     */
    public <T> Key<T> merge(T entity) {
        return merge(entity, getWriteConcern(entity));
    }

    /**
     *
     * @param entity
     * @param wc
     * @param <T>
     * @return
     */
    public <T> Key<T> merge(T entity, WriteConcern wc) {
        LinkedHashMap<Object, DBObject> involvedObjects = new LinkedHashMap<Object, DBObject>();
        DBObject dbObj = mapr.toDBObject(entity, involvedObjects);
        Key<T> key = getKey(entity);
        entity = ProxyHelper.unwrap(entity);
        Object id = getId(entity);
        if (id == null)
            throw new MappingException("Could not get id for " + entity.getClass().getName());

        //remove (immutable) _id field for update.
        dbObj.removeField(Mapper.ID_KEY);

        WriteResult wr = null;

        MappedClass mc = mapr.getMappedClass(entity);
        DBCollection dbColl = getCollection(entity);

        //try to do an update if there is a @Version field
        wr = tryVersionedUpdate(dbColl, entity, dbObj, wc, db, mc);

        if (wr == null) {
            Query<T> query = (Query<T>) createQuery(entity.getClass()).filter(Mapper.ID_KEY, id);
            wr = update(query, new BasicDBObject("$set", dbObj), false, false, wc).getWriteResult();
        }

        UpdateResults<T> res = new UpdateResults<T>(wr);

        throwOnError(wc, wr);

        //check for updated count if we have a gle
        CommandResult gle = wr.getCachedLastError();
        if (gle != null && res.getUpdatedCount() == 0)
            throw new UpdateException("Not updated: " + gle);

        postSaveOperations(entity, dbObj, involvedObjects);
        return key;
    }

    /**
     *
     * @param entity
     * @param dbObj
     * @param involvedObjects
     * @param <T>
     */
    private <T> void postSaveOperations(Object entity, DBObject dbObj, Map<Object, DBObject> involvedObjects) {
        mapr.updateKeyInfo(entity, dbObj, createCache());

        //call PostPersist on all involved entities (including the entity)
        for (Map.Entry<Object, DBObject> e : involvedObjects.entrySet()) {
            Object ent = e.getKey();
            DBObject dbO = e.getValue();
            MappedClass mc = mapr.getMappedClass(ent);
            mc.callLifecycleMethods(PostPersist.class, ent, dbO, mapr);
        }
    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param multi
     * @param wc
     * @param <T>
     * @return
     */
    @SuppressWarnings("rawtypes")
    private <T> UpdateResults<T> update(Query<T> query, UpdateOperations ops, boolean createIfMissing, boolean multi, WriteConcern wc) {
        DBObject u = ((UpdateOpsImpl) ops).getOps();
        if (((UpdateOpsImpl) ops).isIsolated()) {
            Query<T> q = query.clone();
            q.disableValidation().filter("$atomic", true);
            return update(q, u, createIfMissing, multi, wc);
        }
        return update(query, u, createIfMissing, multi, wc);
    }

    /**
     *
     * @param query
     * @param ops
     * @param createIfMissing
     * @param multi
     * @param <T>
     * @return
     */
    @SuppressWarnings("rawtypes")
    private <T> UpdateResults<T> update(Query<T> query, UpdateOperations ops, boolean createIfMissing, boolean multi) {
        return update(query, ops, createIfMissing, multi, getWriteConcern(query.getEntityClass()));
    }

    /**
     *
     * @param query
     * @param u
     * @param createIfMissing
     * @param multi
     * @param wc
     * @param <T>
     * @return
     */
    private <T> UpdateResults<T> update(Query<T> query, DBObject u, boolean createIfMissing, boolean multi, WriteConcern wc) {
        QueryImpl<T> qi = (QueryImpl<T>) query;

        DBCollection dbColl = qi.getCollection();
        //TODO remove this after testing.
        if (dbColl == null)
            dbColl = getCollection(qi.getEntityClass());

        if (qi.getSortObject() != null && qi.getSortObject().keySet() != null && !qi.getSortObject().keySet().isEmpty())
            throw new QueryException("sorting is not allowed for updates.");
        if (qi.getOffset() > 0)
            throw new QueryException("a query offset is not allowed for updates.");
        if (qi.getLimit() > 0)
            throw new QueryException("a query limit is not allowed for updates.");

        DBObject q = qi.getQueryObject();
        if (q == null)
            q = new BasicDBObject();

        if (log.isTraceEnabled())
            log.trace("Executing update(" + dbColl.getName() + ") for query: " + q + ", ops: " + u + ", multi: " + multi + ", upsert: " + createIfMissing);

        WriteResult wr;
        if (wc == null)
            wr = dbColl.update(q, u, createIfMissing, multi);
        else
            wr = dbColl.update(q, u, createIfMissing, multi, wc);

        throwOnError(wc, wr);

        return new UpdateResults<T>(wr);
    }

    /**
     *
     * @param query
     * @param <T>
     * @return
     */
    public <T> T findAndDelete(Query<T> query) {
        DBCollection dbColl = ((QueryImpl<T>) query).getCollection();
        //TODO remove this after testing.
        if (dbColl == null)
            dbColl = getCollection(((QueryImpl<T>) query).getEntityClass());

        QueryImpl<T> qi = ((QueryImpl<T>) query);
        EntityCache cache = createCache();

        if (log.isTraceEnabled())
            log.trace("Executing findAndModify(" + dbColl.getName() + ") with delete ...");

        DBObject result = dbColl.findAndModify(qi.getQueryObject(), qi.getFieldsObject(), qi.getSortObject(), true, null, false, false);

        if (result != null) {
            T entity = (T) mapr.fromDBObject(qi.getEntityClass(), result, cache);
            return entity;
        }

        return null;
    }

    /**
     *
     * @param q
     * @param ops
     * @param <T>
     * @return
     */
    public <T> T findAndModify(Query<T> q, UpdateOperations<T> ops) {
        return findAndModify(q, ops, false);
    }

    /**
     *
     * @param query
     * @param ops
     * @param oldVersion indicated the old version of the Entity should be returned
     * @param <T>
     * @return
     */
    public <T> T findAndModify(Query<T> query, UpdateOperations<T> ops, boolean oldVersion) {
        return findAndModify(query, ops, oldVersion, false);
    }

    /**
     *
     * @param query
     * @param ops
     * @param oldVersion      indicated the old version of the Entity should be returned
     * @param createIfMissing if the query returns no results, then a new object will be created (sets upsert=true)
     * @param <T>
     * @return
     */
    public <T> T findAndModify(Query<T> query, UpdateOperations<T> ops, boolean oldVersion, boolean createIfMissing) {
        QueryImpl<T> qi = (QueryImpl<T>) query;

        DBCollection dbColl = qi.getCollection();
        //TODO remove this after testing.
        if (dbColl == null)
            dbColl = getCollection(qi.getEntityClass());

        if (log.isTraceEnabled())
            log.info("Executing findAndModify(" + dbColl.getName() + ") with update ");
        DBObject res = null;
        try {
            res = dbColl.findAndModify(qi.getQueryObject(),
                    qi.getFieldsObject(),
                    qi.getSortObject(),
                    false,
                    ((UpdateOpsImpl<T>) ops).getOps(), !oldVersion,
                    createIfMissing);
        } catch (MongoException e) {
            if (e.getMessage() == null || !e.getMessage().contains("matching"))
                throw e;
        }

        if (res == null)
            return null;
        else
            return (T) mapr.fromDBObject(qi.getEntityClass(), res, createCache());
    }

    /**
     *
     * @param type        MapreduceType
     * @param q           The query (only the criteria, limit and sort will be used)
     * @param outputType  The type of resulting data; inline is not working yet
     * @param baseCommand The base command to fill in and send to the server
     * @param <T>
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <T> MapreduceResults<T> mapReduce(MapreduceType type, Query q, Class<T> outputType, MapReduceCommand baseCommand) {

        Assert.parametersNotNull("map", baseCommand.getMap());
        Assert.parameterNotEmpty(baseCommand.getMap(), "map");
        Assert.parametersNotNull("reduce", baseCommand.getReduce());
        Assert.parameterNotEmpty(baseCommand.getMap(), "reduce");


        if (MapreduceType.INLINE.equals(type))
            throw new IllegalArgumentException("Inline map/reduce is not supported.");

        QueryImpl<T> qi = (QueryImpl<T>) q;
        if (qi.getOffset() != 0 || qi.getFieldsObject() != null)
            throw new QueryException("mapReduce does not allow the offset/retrievedFields query options.");


        OutputType outType = OutputType.REPLACE;
        switch (type) {
            case REDUCE:
                outType = OutputType.REDUCE;
                break;
            case MERGE:
                outType = OutputType.MERGE;
                break;
            case INLINE:
                outType = OutputType.INLINE;
                break;
            default:
                outType = OutputType.REPLACE;
                break;
        }

        DBCollection dbColl = qi.getCollection();

        MapReduceCommand cmd = new MapReduceCommand(dbColl, baseCommand.getMap(), baseCommand.getReduce(), baseCommand.getOutputTarget(), outType, qi.getQueryObject());
        cmd.setFinalize(baseCommand.getFinalize());
        cmd.setScope(baseCommand.getScope());

        if (qi.getLimit() > 0)
            cmd.setLimit(qi.getLimit());
        if (qi.getSortObject() != null)
            cmd.setSort(qi.getSortObject());

        if (log.isTraceEnabled())
            log.info("Executing " + cmd.toString());

        MapReduceOutput mpo = dbColl.mapReduce(baseCommand);
        MapreduceResults mrRes = (MapreduceResults) mapr.fromDBObject(MapreduceResults.class, mpo.getRaw(), createCache());

        QueryImpl baseQ = null;
        if (!MapreduceType.INLINE.equals(type))
            baseQ = new QueryImpl(outputType, db.getCollection(mrRes.getOutputCollectionName()), this);
        //TODO Handle inline case and create an iterator/able.

        mrRes.setBits(type, baseQ);
        return mrRes;

    }

    /**
     *
     * @param type
     * @param query
     * @param map
     * @param reduce
     * @param finalize
     * @param scopeFields
     * @param outputType
     * @param <T>
     * @return
     */
    @SuppressWarnings("rawtypes")
    public <T> MapreduceResults<T> mapReduce(MapreduceType type, Query query, String map, String reduce, String finalize, Map<String, Object> scopeFields, Class<T> outputType) {

        QueryImpl<T> qi = (QueryImpl<T>) query;
        DBCollection dbColl = qi.getCollection();

        String outColl = mapr.getCollectionName(outputType);

        OutputType outType = OutputType.REPLACE;
        switch (type) {
            case REDUCE:
                outType = OutputType.REDUCE;
                break;
            case MERGE:
                outType = OutputType.MERGE;
                break;
            case INLINE:
                outType = OutputType.INLINE;
                break;
            default:
                outType = OutputType.REPLACE;
                break;
        }

        MapReduceCommand cmd = new MapReduceCommand(dbColl, map, reduce, outColl, outType, qi.getQueryObject());

        if (qi.getLimit() > 0)
            cmd.setLimit(qi.getLimit());
        if (qi.getSortObject() != null)
            cmd.setSort(qi.getSortObject());

        if (finalize != null && finalize.length() > 0)
            cmd.setFinalize(finalize);

        if (scopeFields != null && scopeFields.size() > 0)
            cmd.setScope(scopeFields);

        return mapReduce(type, query, outputType, cmd);
    }

    /**
     * Converts a list of keys to refs.
     */
    public static <T> List<DBRef> keysAsRefs(List<Key<T>> keys, Mapper mapr) {
        ArrayList<DBRef> refs = new ArrayList<DBRef>(keys.size());
        for (Key<T> key : keys)
            refs.add(mapr.keyToRef(key));
        return refs;
    }

    /**
     * Converts a list of refs to keys.
     */
    public static <T> List<Key<T>> refsToKeys(Mapper mapr, List<DBRef> refs, Class<T> c) {
        ArrayList<Key<T>> keys = new ArrayList<Key<T>>(refs.size());
        for (DBRef ref : refs) {
            keys.add((Key<T>) mapr.refToKey(ref));
        }
        return keys;
    }

    /**
     *
     * @return
     */
    private EntityCache createCache() {
        return mapr.createEntityCache();
    }

    /**
     * Gets the write concern for entity or returns the default write concern for this datastore.
     */
    public WriteConcern getWriteConcern(Object clazzOrEntity) {
        WriteConcern wc = defConcern;
        if (clazzOrEntity != null) {
            Entity entityAnn = getMapper().getMappedClass(clazzOrEntity).getEntityAnnotation();
            if (entityAnn != null && !"".equals(entityAnn.concern()))
                wc = WriteConcern.valueOf(entityAnn.concern());
        }

        return wc;
    }

    /**
     *
     * @return
     */
    public WriteConcern getDefaultWriteConcern() {
        return defConcern;
    }

    /**
     *
     * @param wc
     */
    public void setDefaultWriteConcern(WriteConcern wc) {
        defConcern = wc;
    }

    /**
     *
     * @param fact
     * @return
     */
    public DBDecoderFactory setDecoderFact(DBDecoderFactory fact) {
        return decoderFactory = fact;
    }

    /**
     *
     * @return
     */
    public DBDecoderFactory getDecoderFact() {
        return decoderFactory != null ? decoderFactory : mongo.getMongoOptions().dbDecoderFactory;
    }
}
