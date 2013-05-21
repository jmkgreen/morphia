package com.github.jmkgreen.morphia.utils;

public enum IndexDirection {
    ASC,
    DESC,
    GEO2D,
    GEO2DSPHERE;

    public Object toIndexValue() {
        switch (this) {
            case ASC:
                return 1;
            case DESC:
                return -1;
            case GEO2D:
                return "2d";
            case GEO2DSPHERE:
                return "2dsphere";
            default:
                throw new RuntimeException("Invalid!");
        }

    }
}
