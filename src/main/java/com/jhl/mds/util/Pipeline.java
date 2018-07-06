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
        ExecutorService[] executorServices = new ExecutorService[taskList.size() + 1];
        for (int i = 0; i < taskList.size(); i++) {
            executorServices[i] = Executors.newFixedThreadPool(16);
        }
        Consumer[] nextList = new Consumer[taskList.size()];
        nextList[taskList.size() - 1] = o -> {
        };
        if (finalNext != null) {
            nextList[taskList.size() - 1] = o -> executorServices[taskList.size()].submit(() -> finalNext);
        }
        for (int i = taskList.size() - 2; i >= 0; i--) {
            int finalI = i;
            Consumer next = o -> executorServices[finalI + 1].submit(() -> {
                System.out.println("Thread.currentThread().getId() = " + Thread.currentThread().getId());
                taskList.get(finalI + 1).queue(context, o, nextList[finalI + 1]);
            });
            if (taskList.get(finalI) instanceof PipeLineTaskRunner.SelfHandleThread) {
                next = o -> taskList.get(finalI + 1).queue(context, o, nextList[finalI + 1]);
            }
            nextList[i] = next;
        }

        executorServices[0].submit(() -> taskList.get(0).queue(context, null, nextList[0]));
    }
}

