package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.mongodb.DBObject;
import java.util.Map;

/**
 * Simple mapper that just uses the Mapper.getOptions().converts
 *
 * @author Scott Hernnadez
 */
class ValueMapper implements CustomMapper {
    public void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, DefaultMapper mapr) {
        try {
            mapr.converters.toDBObject(entity, mf, dbObject, mapr.getOptions());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, DefaultMapper mapr) {
        try {
            mapr.converters.fromDBObject(dbObject, mf, entity);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
