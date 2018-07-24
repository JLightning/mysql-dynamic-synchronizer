package com.jhl.mds.util.pipeline;

import java.util.function.Consumer;

public interface PipeLineTaskRunner<T, I, R> {

    void execute(T context, I input, Consumer<R> next, Consumer<Exception> errorHandler) throws Exception;

    interface SelfHandleThread {
    }
}
