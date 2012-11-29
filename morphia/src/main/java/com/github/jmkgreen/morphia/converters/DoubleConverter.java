/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import com.github.jmkgreen.morphia.utils.ReflectionUtils;
import java.util.ArrayList;
import java.util.List;
import org.bson.LazyBSONList;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"rawtypes"})
public class DoubleConverter extends TypeConverter implements SimpleValueConverter {

    public DoubleConverter() {
        super(double.class, Double.class, Double[].class);
    }

    @Override
    public Object decode(Class targetClass, Object val, MappedField optionalExtraInfo) throws MappingException {
        if (val == null)
            return null;

        if (val instanceof Double)
            return (Double) val;

        if (val instanceof Number)
            return ((Number) val).doubleValue();

        // Fixes https://github.com/jmkgreen/morphia/issues/4
        if (val instanceof ArrayList)
            return ReflectionUtils.convertToArray(Double.class, (List<?>) val);

        //FixMe: super-hacky
        if (val instanceof LazyBSONList || val instanceof ArrayList)
            return ReflectionUtils.convertToArray(double.class, (List<?>) val);

        String sVal = val.toString();
        return Double.parseDouble(sVal);
    }
}
