/**
 *
 */
package com.github.jmkgreen.morphia.mapping.validation.fieldrules;

import com.github.jmkgreen.morphia.annotations.Property;
import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation.Level;
import java.util.Set;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class MisplacedProperty extends FieldConstraint {

    @Override
    protected void check(MappedClass mc, MappedField mf, Set<ConstraintViolation> ve) {
        // a field can be a Value, Reference, or Embedded
        if (mf.hasAnnotation(Property.class)) {
            // make sure that the property type is supported
            if (mf.isSingleValue() && !mf.isTypeMongoCompatible() && !mc.getMapper().getConverters().hasSimpleValueConverter(mf)) {
                ve.add(new ConstraintViolation(Level.WARNING, mc, mf, this.getClass(), mf.getFullName() + " is annotated as @"
                        + Property.class.getSimpleName() + " but is a type that cannot be mapped simply (type is "
                        + mf.getType().getName() + ")."));
            }
        }
    }

}
