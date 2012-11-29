/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.Key;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.mongodb.DBRef;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class KeyConverter extends TypeConverter {

    public KeyConverter() {
        super(Key.class);
    }

    @Override
    public Object decode(Class targetClass, Object o, MappedField optionalExtraInfo) throws MappingException {
        if (o == null) return null;
        if (!(o instanceof DBRef))
            throw new ConverterException(String.format("cannot convert %s to Key because it isn't a DBRef", o.toString()));

        return mapr.refToKey((DBRef) o);
    }

    @Override
    public Object encode(Object t, MappedField optionalExtraInfo) {
        if (t == null)
            return null;
        return mapr.keyToRef((Key) t);
    }

}
