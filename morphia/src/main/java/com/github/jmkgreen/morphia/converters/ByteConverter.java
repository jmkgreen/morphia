/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import org.bson.types.Binary;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"rawtypes"})
public class ByteConverter extends TypeConverter implements SimpleValueConverter {
    public ByteConverter() {
        super(Byte.class, byte.class);
    }

    @Override
    public Object decode(Class targetClass, Object val, MappedField optionalExtraInfo) throws MappingException {
        if (val == null) return null;

        if (val instanceof Number)
            return ((Number) val).byteValue();
        
        if (val instanceof Binary) {
        	return ((Binary)val).getData();
        }
        
        String sVal = val.toString();
        return Byte.parseByte(sVal);
    }
}
