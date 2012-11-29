package com.github.jmkgreen.morphia;

/**
 * @author Scott Hernnadez
 */
public class DatastoreService {
    /**
     *
     */
    private static Morphia mor;

    /**
     *
     */
    private static Datastore ds;

    /**
     *
     */
    static {
        mor = new Morphia();
        ds = mor.createDatastore("test");
    }

    /**
     * Connects to "test" database on localhost by default.
     */
    public static Datastore getDatastore() {
        return ds;
    }

    /**
     *
     * @param dbName
     */
    public static void setDatabase(String dbName) {
        if (!ds.getDB().getName().equals(dbName))
            ds = mor.createDatastore(dbName);
    }

    /**
     *
     * @param c
     */
    @SuppressWarnings("unchecked")
    public static void mapClass(Class c) {
        mor.map(c);
    }

    /**
     *
     * @param classes
     */
    @SuppressWarnings("unchecked")
    public static void mapClasses(Class[] classes) {
        for (Class c : classes)
            mapClass(c);
    }

    /**
     *
     * @param pkg
     */
    public static void mapPackage(String pkg) {
        mor.mapPackage(pkg, true);
    }
}