/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.annotations.Serialized;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.mapping.Serializer;
import java.io.IOException;
import org.bson.types.Binary;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
@SuppressWarnings("unchecked")
public class SerializedObjectConverter extends TypeConverter {
    @Override
    protected boolean isSupported(Class c, MappedField optionalExtraInfo) {
        if (optionalExtraInfo != null)
            return (optionalExtraInfo.hasAnnotation(Serialized.class));
        else
            return false;
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField f) throws MappingException {
        if (fromDBObject == null) return null;

        if (!((fromDBObject instanceof Binary) || (fromDBObject instanceof byte[]))) {
            throw new MappingException("The stored data is not a DBBinary or byte[] instance for " + f.getFullName()
                    + " ; it is a " + fromDBObject.getClass().getName());
        }

        try {
            boolean useCompression = !f.getAnnotation(Serialized.class).disableCompression();
            return Serializer.deserialize(fromDBObject, useCompression);
        } catch (IOException e) {
            throw new MappingException("While deserializing to " + f.getFullName(), e);
        } catch (ClassNotFoundException e) {
            throw new MappingException("While deserializing to " + f.getFullName(), e);
        }
    }

    @Override
    public Object encode(Object value, MappedField f) {
        if (value == null)
            return null;
        try {
            boolean useCompression = !f.getAnnotation(Serialized.class).disableCompression();
            return Serializer.serialize(value, useCompression);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
