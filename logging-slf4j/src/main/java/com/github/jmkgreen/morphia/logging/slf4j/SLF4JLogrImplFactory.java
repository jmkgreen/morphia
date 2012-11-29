/**
 *
 */
package com.github.jmkgreen.morphia.logging.slf4j;

import com.github.jmkgreen.morphia.logging.Logr;
import com.github.jmkgreen.morphia.logging.LogrFactory;

/**
 * @author doc
 */
public class SLF4JLogrImplFactory implements LogrFactory {
    /**
     *
     * @param c
     * @return
     */
    public Logr get(final Class<?> c) {
        return new SLF4JLogr(c);
    }

}
