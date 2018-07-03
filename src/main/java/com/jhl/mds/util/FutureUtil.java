package com.jhl.mds.util;

import java.util.concurrent.Future;

public class FutureUtil {

    public static void allOf(Iterable<Future<?>> futures) {
        synchronized (futures) {
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
