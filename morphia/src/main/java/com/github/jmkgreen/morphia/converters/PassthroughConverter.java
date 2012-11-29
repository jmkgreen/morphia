/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class PassthroughConverter extends TypeConverter {

    public PassthroughConverter() {
    }

    public PassthroughConverter(Class... types) {
        super(types);
    }

    @Override
    protected boolean isSupported(Class c, MappedField optionalExtraInfo) {
        return true;
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
        return fromDBObject;
    }
}
