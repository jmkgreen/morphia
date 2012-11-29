/**
 *
 */
package com.github.jmkgreen.morphia.ext.entityscanner;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.scannotation.ClasspathUrlFinder;

import com.github.jmkgreen.morphia.Morphia;
import com.github.jmkgreen.morphia.annotations.Entity;
import com.github.jmkgreen.morphia.utils.Assert;
import com.google.common.base.Predicates;

/**
 * @author us@thomas-daily.de
 */
public class EntityScanner {

    /**
     * @param m
     */
    public EntityScanner(final Morphia m) {
        this(m, null);

    }

    /**
     * @param m
     * @param predicate
     */
    public EntityScanner(final Morphia m,
                         com.google.common.base.Predicate<String> predicate) {
        if (predicate == null) {
            predicate = Predicates.alwaysTrue();
        }
        Assert.parametersNotNull("m, predicate", m, predicate);
        final ConfigurationBuilder conf = new ConfigurationBuilder();
        conf.setScanners(new TypesScanner(), new TypeAnnotationsScanner());

        final Set<URL> s = new HashSet<URL>();
        s.addAll(ClasspathHelper.forJavaClassPath());
        s.addAll(Arrays.asList(ClasspathUrlFinder.findClassPaths()));
        conf.setUrls(new ArrayList(s));

        conf.filterInputsBy(predicate);

        final Reflections r = new Reflections(conf);

        final Set<Class<?>> entities = r.getTypesAnnotatedWith(Entity.class);
        for (final Class<?> c : entities) {
            m.map(c);
        }
    }
}
