/**
 *
 */
package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.mapping.Mapper;
import com.mongodb.DBObject;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class AbstractEntityInterceptor implements EntityInterceptor {

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    public void postLoad(Object ent, DBObject dbObj, Mapper mapr) {
    }

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    public void postPersist(Object ent, DBObject dbObj, Mapper mapr) {
    }

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    public void preLoad(Object ent, DBObject dbObj, Mapper mapr) {
    }

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    public void prePersist(Object ent, DBObject dbObj, Mapper mapr) {
    }

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    public void preSave(Object ent, DBObject dbObj, Mapper mapr) {
    }
}
