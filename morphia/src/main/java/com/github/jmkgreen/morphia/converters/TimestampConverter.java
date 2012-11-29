/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @author scotthernandez
 */
@SuppressWarnings({"rawtypes"})
public class TimestampConverter extends DateConverter implements SimpleValueConverter {

    public TimestampConverter() {
        super(Timestamp.class);
    }

    ;

    @Override
    public Object decode(Class targetClass, Object val, MappedField optionalExtraInfo) throws MappingException {
        Date d = (Date) super.decode(targetClass, val, optionalExtraInfo);
        return new Timestamp(d.getTime());
    }

    @Override
    public Object encode(Object val, MappedField optionalExtraInfo) {
        if (val == null)
            return null;
        return new Date(((Timestamp) val).getTime());
    }

}
