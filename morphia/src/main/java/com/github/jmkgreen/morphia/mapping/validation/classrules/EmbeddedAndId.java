/**
 *
 */
package com.github.jmkgreen.morphia.mapping.validation.classrules;

import com.github.jmkgreen.morphia.annotations.Embedded;
import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.github.jmkgreen.morphia.mapping.validation.ClassConstraint;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation.Level;
import java.util.Set;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class EmbeddedAndId implements ClassConstraint {

    public void check(MappedClass mc, Set<ConstraintViolation> ve) {
        if (mc.getEmbeddedAnnotation() != null && mc.getIdField() != null) {
            ve.add(new ConstraintViolation(Level.FATAL, mc, this.getClass(), "@" + Embedded.class.getSimpleName()
                    + " classes cannot specify a @Id field"));
        }
    }

}
