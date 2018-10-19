package com.jhl.mds.util.pipeline;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PipelineGrouperService<I> implements PipeLineTaskRunner<Object, I, List<I>> {

    private final int chunkSize;
    private List<I> list = new ArrayList<>();
    private boolean beforeTaskFinished;
    private Consumer<List<I>> next;

    public PipelineGrouperService(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    @Override
    public synchronized void execute(Object context, I input, Consumer<List<I>> next, Consumer<Exception> errorHandler) throws Exception {
        this.next = next;
        if (input != null) {
            list.add(input);
        }
        if ((list.size() >= chunkSize || beforeTaskFinished) && list.size() > 0) {
            next.accept(new ArrayList<>(list));
            list.clear();
        }
    }

    synchronized void beforeTaskFinished() {
        this.beforeTaskFinished = true;
        next.accept(new ArrayList<>(list));
        list.clear();
    }

    synchronized int getListSize() {
        return list.size();
    }
}
