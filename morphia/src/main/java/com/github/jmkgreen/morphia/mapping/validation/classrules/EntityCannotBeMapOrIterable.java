/**
 *
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.github.jmkgreen.morphia.mapping.validation.ClassConstraint;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation.Level;
import java.util.Map;
import java.util.Set;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class EntityCannotBeMapOrIterable implements ClassConstraint {

    public void check(MappedClass mc, Set<ConstraintViolation> ve) {

        if (mc.getEntityAnnotation() != null && (Map.class.isAssignableFrom(mc.getClazz()) || Iterable.class.isAssignableFrom(mc.getClazz()))) {
            ve.add(new ConstraintViolation(Level.FATAL, mc, this.getClass(), "Entities cannot implement Map/Iterable"));
        }

    }
}
