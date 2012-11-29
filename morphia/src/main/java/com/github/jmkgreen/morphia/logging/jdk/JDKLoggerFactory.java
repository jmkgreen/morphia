package com.github.jmkgreen.morphia.logging.jdk;

import com.github.jmkgreen.morphia.logging.Logr;
import com.github.jmkgreen.morphia.logging.LogrFactory;

public class JDKLoggerFactory implements LogrFactory {

    public Logr get(Class<?> c) {
        return new JDKLogger(c);
    }

}
