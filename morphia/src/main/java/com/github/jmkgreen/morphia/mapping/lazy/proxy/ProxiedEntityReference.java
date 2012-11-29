/**
 *
 */
package com.github.jmkgreen.morphia.mapping.lazy.proxy;

import com.github.jmkgreen.morphia.Key;

/**
 * @author Uwe Schaefer, (schaefer@thomas-daily.de)
 */
public interface ProxiedEntityReference extends ProxiedReference {
    Key<?> __getKey();
}
