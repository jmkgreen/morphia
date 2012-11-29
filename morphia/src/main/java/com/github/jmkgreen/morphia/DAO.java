package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.dao.BasicDAO;
import com.mongodb.Mongo;

/**
 *
 * @param <T>
 * @param <K>
 */
@Deprecated //use dao.BasicDAO
public class DAO<T, K> extends BasicDAO<T, K> {
    /**
     *
     * @param entityClass
     * @param mongo
     * @param morphia
     * @param dbName
     */
    public DAO(Class<T> entityClass, Mongo mongo, Morphia morphia,
               String dbName) {
        super(entityClass, mongo, morphia, dbName);
    }

    /**
     *
     * @param entityClass
     * @param ds
     */
    public DAO(Class<T> entityClass, Datastore ds) {
        super(entityClass, ds);
    }

}
