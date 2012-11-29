/**
 *
 */
package com.github.jmkgreen.morphia.mapping.lazy.proxy;

import com.github.jmkgreen.morphia.Key;
import java.util.Collection;
import java.util.List;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public interface ProxiedEntityReferenceList extends ProxiedReference {

    void __add(Key<?> key);

    void __addAll(Collection<? extends Key<?>> keys);

    List<Key<?>> __getKeysAsList();

}
