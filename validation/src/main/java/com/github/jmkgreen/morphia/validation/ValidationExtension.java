/**
 *
 */
package com.github.jmkgreen.morphia.validation;

import com.github.jmkgreen.morphia.AbstractEntityInterceptor;
import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.mapping.Mapper;
import com.mongodb.DBObject;

import javax.validation.Configuration;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

/**
 * @author us@thomas-daily.de
 */
public class ValidationExtension extends AbstractEntityInterceptor {
    /**
     *
     */
    private ValidatorFactory validationFactory;
    //private Mapper mapper;

    /**
     *
     */
    @Deprecated
    public ValidationExtension() {
        // use the new ValidationExtension(morphia) convention
    }

    /**
     *
     * @param m
     */
    public ValidationExtension(final Morphia m) {
        final Configuration<?> configuration =
                Validation.byDefaultProvider().configure();
        this.validationFactory = configuration.buildValidatorFactory();

        m.getMapper().addInterceptor(this);
    }

    /**
     *
     * @param ent
     * @param dbObj
     * @param mapr
     */
    @Override
    public void prePersist(final Object ent,
                           final DBObject dbObj, final Mapper mapr) {
        final Set validate =
                this.validationFactory.getValidator().validate(ent);
        if (!validate.isEmpty()) {
            throw new VerboseJSR303ConstraintViolationException(validate);
        }
    }
}
