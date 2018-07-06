package com.jhl.mds.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Pipeline<T> {

    private static final ExecutorService executor = Executors.newFixedThreadPool(16);
    @NonNull
    private T context;
    private List<PipeLineTaskRunner> taskList = new ArrayList<>();
    @Setter
    private Consumer<Object> finalNext;

    public Pipeline<T> append(PipeLineTaskRunner taskRunner) {
        taskList.add(taskRunner);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void execute() {
        Consumer[] nextList = new Consumer[taskList.size()];
        nextList[taskList.size() - 1] = o -> {
        };
        if (finalNext != null) {
            nextList[taskList.size() - 1] = finalNext;
        }
        for (int i = taskList.size() - 2; i >= 0; i--) {
            int finalI = i;
            Consumer next = o -> executor.submit(() -> taskList.get(finalI + 1).queue(context, o, nextList[finalI + 1]));
            nextList[i] = next;
        }

        taskList.get(0).queue(context, null, nextList[0]);
    }
}

