package com.jhl.mds.util;

import java.util.function.Consumer;

public interface PipeLineTaskRunner<T> {

    void queue(T context, Object input, Consumer<Object> next);
}
