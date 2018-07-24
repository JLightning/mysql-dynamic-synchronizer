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
public class Pipeline<T, R> {

    @NonNull
    private T context;
    private List<PipeLineTaskRunner> taskList = new ArrayList<>();
    private final List<Boolean> taskFinished = new ArrayList<>();
    @Setter
    private Consumer<R> finalNext = System.out::println;
    @Setter
    private Consumer<Exception> errorHandler = e -> {
        if (!(e instanceof PipelineCancelException)) {
            e.printStackTrace();
        }
    };
    @Setter
    private boolean threadEnable = true;
    private int[] invokeCount;

    public Pipeline<T, R> append(PipeLineTaskRunner taskRunner) {
        taskList.add(taskRunner);
        taskFinished.add(false);
        return this;
    }

    public Pipeline<T, R> execute() {
        return execute(null);
    }

    // TODO: fix data race
    @SuppressWarnings("unchecked")
    public Pipeline<T, R> execute(Object input) {
        ExecutorService[] executorServices = new ExecutorService[taskList.size() + 1];
        for (int i = 0; i < taskList.size(); i++) {
            executorServices[i] = Executors.newFixedThreadPool(4);
        }
        Consumer[] nextList = new Consumer[taskList.size()];
        invokeCount = new int[taskList.size()];
        nextList[taskList.size() - 1] = finalNext;
        for (int i = taskList.size() - 2; i >= 0; i--) {
            int finalI = i;

            Consumer next = o -> {
                boolean errorHappened = false;
                try {
                    taskList.get(finalI + 1).execute(context, o, nextList[finalI + 1], errorHandler);
                } catch (Exception e) {
                    errorHappened = true;
                    errorHandler.accept(e);
                } finally {
                    boolean thisTaskFinished = false;
                    synchronized (invokeCount) {
                        invokeCount[finalI + 1]--;
                        if (invokeCount[finalI + 1] == 0 && taskFinished.get(finalI)) {
                            thisTaskFinished = true;
                            updateTaskFinish(finalI + 1);
                        }
                    }

                    if (thisTaskFinished && taskList.get(finalI + 2) instanceof PipelineGrouperService) {
                        ((PipelineGrouperService) taskList.get(finalI + 2)).beforeTaskFinished();
                    }

                    if (errorHappened && thisTaskFinished) {
                        for (int j = finalI + 2; j < taskList.size(); j++) {
                            int _invokeCount;
                            synchronized (invokeCount) {
                                _invokeCount = invokeCount[j];
                            }
                            if (_invokeCount == 0) {
                                updateTaskFinish(j);

                                if (taskList.get(j + 1) instanceof PipelineGrouperService) {
                                    ((PipelineGrouperService) taskList.get(j + 1)).beforeTaskFinished();
                                }
                            } else break;
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

        return this;
    }

    private void updateTaskFinish(int index) {
        System.out.println("task " + index + " finished: " + taskList.get(index).getClass());
        synchronized (taskFinished) {
            taskFinished.set(index, true);
            taskFinished.notify();
        }
    }

    public void waitForFinish() throws InterruptedException {
        while (!taskFinished.get(taskFinished.size() - 1)) {
            synchronized (taskFinished) {
                taskFinished.wait();
            }
        }
    }
}

