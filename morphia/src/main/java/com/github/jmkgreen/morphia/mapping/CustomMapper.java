/**
 *
 */
package com.github.jmkgreen.morphia.mapping;

import com.github.jmkgreen.morphia.mapping.cache.EntityCache;
import com.mongodb.DBObject;
import java.util.Map;

/**
 * A CustomMapper if one that implements the methods needed to map to/from POJO/DBObject
 *
 * @author skot
 */
public interface CustomMapper {
    void toDBObject(Object entity, MappedField mf, DBObject dbObject, Map<Object, DBObject> involvedObjects, DefaultMapper mapr);

    void fromDBObject(DBObject dbObject, MappedField mf, Object entity, EntityCache cache, DefaultMapper mapr);
}
