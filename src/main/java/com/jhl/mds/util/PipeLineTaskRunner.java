package com.jhl.mds.util;

import java.util.function.Consumer;

public interface PipeLineTaskRunner<T, G> {

    void queue(T context, G input, Consumer<Object> next);

    interface SelfHandleThread {
    }
}
