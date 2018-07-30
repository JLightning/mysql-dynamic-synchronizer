package com.jhl.mds.util.pipeline;

import java.util.function.Consumer;

public interface PipeLineTaskRunner<C, I, R> {

    void execute(C context, I input, Consumer<R> next, Consumer<Exception> errorHandler) throws Exception;

    interface SelfHandleThread {
    }
}
