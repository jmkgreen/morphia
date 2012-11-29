/**
 *
 */
package com.github.jmkgreen.morphia;

/**
 * @author Uwe Schaefer, (us@thomas-daily.de)
 */
public class VersionHelper {

    /**
     *
     * @param oldVersion
     * @return
     */
    public static long nextValue(Long oldVersion) {
        long newVersion = oldVersion == null ? 1 : oldVersion + 1;
        return newVersion;
    }

}
