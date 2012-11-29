/**
 *
 */
package com.github.jmkgreen.morphia.ext.guice;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.mapping.MapperOptions;
import com.github.jmkgreen.morphia.utils.Assert;
import com.google.inject.Injector;

/**
 * @author us@thomas-daily.de
 */
public class GuiceExtension {

    /**
     *
     * @param morphia
     * @param injector
     */
    public GuiceExtension(final Morphia morphia, final Injector injector) {
        Assert.parameterNotNull(morphia, "morphia");
        final MapperOptions options = morphia.getMapper().getOptions();
        options.objectFactory =
                new GuiceObjectFactory(options.objectFactory, injector);
    }
}
