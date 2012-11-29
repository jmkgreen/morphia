/**
 *
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import com.github.jmkgreen.morphia.mapping.MappedField;
import java.util.Arrays;
import java.util.List;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class FieldEnumString {
    private final String display;

    public FieldEnumString(MappedField... fields) {
        this(Arrays.asList(fields));
    }

    public FieldEnumString(List<MappedField> fields) {
        StringBuffer sb = new StringBuffer(128);
        for (MappedField mappedField : fields) {
            if (sb.length() > 0)
                sb.append(", ");
            sb.append(mappedField.getNameToStore());
        }
        this.display = sb.toString();
    }

    @Override
    public String toString() {
        return display;
    }
}
