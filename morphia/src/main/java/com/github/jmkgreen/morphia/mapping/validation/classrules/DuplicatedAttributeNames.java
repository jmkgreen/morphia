package com.github.jmkgreen.morphia.mapping.validation.classrules;

import com.github.jmkgreen.morphia.mapping.MappedClass;
import com.github.jmkgreen.morphia.mapping.MappedField;
import com.github.jmkgreen.morphia.mapping.validation.ClassConstraint;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation;
import com.github.jmkgreen.morphia.mapping.validation.ConstraintViolation.Level;
import java.util.HashSet;
import java.util.Set;

/**
 * @author josephpachod
 */
public class DuplicatedAttributeNames implements ClassConstraint {

    public void check(MappedClass mc, Set<ConstraintViolation> ve) {
        Set<String> foundNames = new HashSet<String>();
        Set<String> duplicates = new HashSet<String>();
        for (MappedField mappedField : mc.getPersistenceFields()) {
            for (String name : mappedField.getLoadNames()) {
//				if (duplicates.contains(name)) {
//					continue;
//				}
                if (!foundNames.add(name)) {
                    ve.add(new ConstraintViolation(Level.FATAL, mc, mappedField, this.getClass(), "Mapping to MongoDB field name '" + name
                            + "' is duplicated; you cannot map different java fields to the same MongoDB field."));
                    duplicates.add(name);
                }
            }
        }
    }
}
