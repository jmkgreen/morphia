/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import java.util.Date;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"rawtypes"})
public class DateConverter extends TypeConverter implements SimpleValueConverter {

    public DateConverter() {
        this(Date.class);
    }

    ;

    protected DateConverter(Class clazz) {
        super(clazz);
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object decode(Class targetClass, Object val, MappedField optionalExtraInfo) throws MappingException {
        if (val == null) return null;

        if (val instanceof Date)
            return val;

        if (val instanceof Number)
            return new Date(((Number) val).longValue());

        return new Date(Date.parse(val.toString())); // good luck
    }
}
