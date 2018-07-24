package com.jhl.mds.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PipelineGrouperService<I> implements PipeLineTaskRunner<Object, I, List<I>> {

    private static final int CHUNK_SIZE = 123;
    private List<I> list = new ArrayList<>();
    private boolean beforeTaskFinished;
    private Consumer<List<I>> next;

    @Override
    public synchronized void execute(Object context, I input, Consumer<List<I>> next, Consumer<Exception> errorHandler) throws Exception {
        this.next = next;
        list.add(input);
        if (list.size() >= CHUNK_SIZE || beforeTaskFinished) {
            next.accept(new ArrayList<>(list));
            list.clear();
        }
    }

    public synchronized void beforeTaskFinished() {
        this.beforeTaskFinished = true;
        next.accept(new ArrayList<>(list));
        list.clear();
    }
}
