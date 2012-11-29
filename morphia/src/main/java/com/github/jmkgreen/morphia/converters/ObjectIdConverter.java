/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import org.bson.types.ObjectId;

/**
 * Convert to an ObjectId from string
 *
 * @author scotthernandez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class ObjectIdConverter extends TypeConverter implements SimpleValueConverter {

    public ObjectIdConverter() {
        super(ObjectId.class);
    }

    ;

    @Override
    public Object decode(Class targetClass, Object val, MappedField optionalExtraInfo) throws MappingException {
        if (val == null) return null;

        if (val instanceof ObjectId)
            return val;

        return new ObjectId(val.toString()); // good luck
    }
}
