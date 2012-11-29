/**
 *
 */
package com.github.jmkgreen.morphia.converters;

import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.MappingException;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 * @author scotthernandez
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class LocaleConverter extends TypeConverter implements SimpleValueConverter {

    public LocaleConverter() {
        super(Locale.class);
    }

    @Override
    public Object decode(Class targetClass, Object fromDBObject, MappedField optionalExtraInfo) throws MappingException {
        return parseLocale(fromDBObject.toString());
    }

    @Override
    public Object encode(Object val, MappedField optionalExtraInfo) {
        if (val == null) return null;

        return val.toString();
    }

    public static Locale parseLocale(final String localeString) {
        if ((localeString != null) && (localeString.length() > 0)) {
            StringTokenizer st = new StringTokenizer(localeString, "_");
            String language = st.hasMoreElements() ? st.nextToken() : Locale.getDefault().getLanguage();
            String country = st.hasMoreElements() ? st.nextToken() : "";
            String variant = st.hasMoreElements() ? st.nextToken() : "";
            return new Locale(language, country, variant);
        }
        return null;
    }
}
