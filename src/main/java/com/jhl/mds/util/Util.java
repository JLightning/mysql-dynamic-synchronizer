package com.jhl.mds.util;

public class Util {

    public static <T> T defaultIfNull(T input, T _default) {
        if (input == null) return _default;
        return input;
    }
}
