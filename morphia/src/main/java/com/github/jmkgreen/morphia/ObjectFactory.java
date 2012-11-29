package com.github.jmkgreen.morphia;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.mongodb.DBObject;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 */
@SuppressWarnings("rawtypes")
public interface ObjectFactory {
    /**
     *
     * @param clazz
     * @return
     */
    public Object createInstance(Class clazz);

    /**
     *
     * @param clazz
     * @param dbObj
     * @return
     */
    public Object createInstance(Class clazz, DBObject dbObj);

    /**
     *
     * @param mapr
     * @param mf
     * @param dbObj
     * @return
     */
    public Object createInstance(Mapper mapr, MappedField mf, DBObject dbObj);

    /**
     *
     * @param mf
     * @return
     */
    public Map createMap(MappedField mf);

    /**
     *
     * @param mf
     * @return
     */
    public List createList(MappedField mf);

    /**
     *
     * @param mf
     * @return
     */
    public Set createSet(MappedField mf);
}
