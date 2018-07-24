package com.jhl.mds.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class Pipeline<T, R> {

    @NonNull
    private T context;
    private List<PipeLineTaskRunner> taskList = new ArrayList<>();
    private List<Boolean> taskFinished = new ArrayList<>();
    @Setter
    private Consumer<R> finalNext = System.out::println;
    @Setter
    private Consumer<Exception> errorHandler = Exception::printStackTrace;
    @Setter
    private boolean threadEnable = true;
    private List<PipelineGrouperService> pipelineGrouperServiceList = new ArrayList<>();

    public Pipeline<T, R> append(PipeLineTaskRunner taskRunner) {
        taskList.add(taskRunner);
        taskFinished.add(false);
        if (taskRunner instanceof PipelineGrouperService) {
            pipelineGrouperServiceList.add((PipelineGrouperService) taskRunner);
        }
        return this;
    }

    public void execute() {
        execute(null);
    }

    @SuppressWarnings("unchecked")
    public void execute(Object input) {
        ExecutorService[] executorServices = new ExecutorService[taskList.size() + 1];
        for (int i = 0; i < taskList.size(); i++) {
            executorServices[i] = Executors.newFixedThreadPool(4);
        }
        Consumer[] nextList = new Consumer[taskList.size()];
        int[] invokeCount = new int[taskList.size()];
        nextList[taskList.size() - 1] = finalNext;
        for (int i = taskList.size() - 2; i >= 0; i--) {
            int finalI = i;

            Consumer next = o -> {
                try {
                    taskList.get(finalI + 1).execute(context, o, nextList[finalI + 1], errorHandler);
                } catch (Exception e) {
                    errorHandler.accept(e);
                } finally {
                    synchronized (invokeCount) {
                        invokeCount[finalI + 1]--;
                        System.out.println("invokeCount = " + Arrays.toString(invokeCount));
                        if (invokeCount[finalI + 1] == 0 && taskFinished.get(finalI)) {
                            System.out.println("task " + (finalI + 1) + " finished: " + taskList.get(finalI + 1).getClass());
                            taskFinished.set(finalI + 1, true);

                            if (taskList.get(finalI + 2) instanceof PipelineGrouperService) {
                                ((PipelineGrouperService) taskList.get(finalI + 2)).beforeTaskFinished();
                            }
                        }
                    }
                }
            };

            if (!(taskList.get(finalI) instanceof PipeLineTaskRunner.SelfHandleThread) && threadEnable) {
                Consumer tmpNext = next;
                next = o -> {
                    synchronized (invokeCount) {
                        invokeCount[finalI + 1]++;
                    }
                    executorServices[finalI + 1].submit(() -> tmpNext.accept(o));
                };
            }

            nextList[i] = next;
        }

        executorServices[0].submit(() -> {
            try {
                taskList.get(0).execute(context, input, nextList[0], errorHandler);
            } catch (Exception e) {
                errorHandler.accept(e);
            } finally {
                taskFinished.set(0, true);
            }
        });
    }
}

