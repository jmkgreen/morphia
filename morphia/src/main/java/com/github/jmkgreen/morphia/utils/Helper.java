package com.github.jmkgreen.morphia.utils;

import com.github.jmkgreen.morphia.Datastore;
import com.github.jmkgreen.morphia.query.MorphiaIterator;
import com.github.jmkgreen.morphia.query.Query;
import com.github.jmkgreen.morphia.query.QueryImpl;
import com.github.jmkgreen.morphia.query.UpdateOperations;
import com.github.jmkgreen.morphia.query.UpdateOpsImpl;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * Exposes driver related DBOBject stuff from Morphia objects
 *
 * @author scotthernandez
 */
@SuppressWarnings("rawtypes")
public class Helper {
    public static DBObject getCriteria(Query q) {
        QueryImpl qi = (QueryImpl) q;
        return qi.getQueryObject();
    }

    public static DBObject getSort(Query q) {
        QueryImpl qi = (QueryImpl) q;
        return qi.getSortObject();
    }

    public static DBObject getFields(Query q) {
        QueryImpl qi = (QueryImpl) q;
        return qi.getFieldsObject();
    }

    public static DBCollection getCollection(Query q) {
        QueryImpl qi = (QueryImpl) q;
        return qi.getCollection();
    }

    public static DBCursor getCursor(Iterable it) {
        return ((MorphiaIterator) it).getCursor();
    }

    public static DBObject getUpdateOperations(UpdateOperations ops) {
        UpdateOpsImpl uo = (UpdateOpsImpl) ops;
        return uo.getOps();
    }

    public static DB getDB(Datastore ds) {
        return ds.getDB();
    }
}