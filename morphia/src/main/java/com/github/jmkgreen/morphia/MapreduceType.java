package com.github.jmkgreen.morphia;

/**
 *
 */
public enum MapreduceType {
    /**
     *
     */
    REPLACE,
    /**
     *
     */
    MERGE,
    /**
     *
     */
    REDUCE,
    /**
     *
     */
    INLINE;

    /**
     *
     * @param val
     * @return
     */
    public static MapreduceType fromString(String val) {
        for (int i = 0; i < values().length; i++) {
            MapreduceType fo = values()[i];
            if (fo.equals(val)) return fo;
        }
        return null;
    }

}
