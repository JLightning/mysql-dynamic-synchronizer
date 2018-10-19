package com.jhl.mds.util.pipeline;

import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class Pipeline<Context, FirstInput, Input> {

    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final int id;
    @NonNull
    private Context context;
    private List<PipeLineTaskRunner> taskList = new ArrayList<>();
    @Setter
    private Consumer<Exception> errorHandler = e -> {
        if (!(e instanceof PipelineCancelException)) {
            e.printStackTrace();
        }
    };
    @Setter
    private boolean threadEnable = true;
    private AtomicInteger invokeCount = new AtomicInteger();
    private List<PipelineGrouperService> pipelineGrouperServiceList = new ArrayList<>();
    private Instant startTime;
    private ExecutorService[] executorServices;

    private Pipeline(Context context) {
        this.context = context;
        id = new Random().nextInt(10000);
    }

    public static <C, I> Pipeline<C, I, I> of(C context, Class<I> firstInputClass) {
        return new Pipeline<>(context);
    }

    @SuppressWarnings("unchecked")
    public <R> Pipeline<Context, FirstInput, R> append(PipeLineTaskRunner<? super Context, ? super Input, R> taskRunner) {
        taskList.add(taskRunner);
        if (taskRunner instanceof PipelineGrouperService) {
            pipelineGrouperServiceList.add((PipelineGrouperService) taskRunner);
        }
        return (Pipeline<Context, FirstInput, R>) this;
    }

    public Pipeline<Context, FirstInput, Input> execute() {
        return execute(null);
    }

    // TODO: fix data race
    @SuppressWarnings("unchecked")
    public Pipeline<Context, FirstInput, Input> execute(FirstInput input) {
        startTime = Instant.now();

        executorServices = new ExecutorService[taskList.size() + 1];
        for (int i = 0; i < taskList.size(); i++) {
            executorServices[i] = Executors.newFixedThreadPool(4);
        }
        Consumer[] nextList = new Consumer[taskList.size()];
        nextList[taskList.size() - 1] = o -> {
        };
        for (int i = taskList.size() - 2; i >= 0; i--) {
            int finalI = i;

            Consumer tmpNext = o -> {
                try {
                    taskList.get(finalI + 1).execute(context, o, nextList[finalI + 1], errorHandler);
                } catch (Exception e) {
                    errorHandler.accept(e);
                } finally {
                    invokeCount.decrementAndGet();
                    checkInvokeCount();
                }
            };

            Consumer next = o -> {
                invokeCount.incrementAndGet();
                if (!(taskList.get(finalI) instanceof PipeLineTaskRunner.SelfHandleThread) && threadEnable) {
                    executorServices[finalI + 1].submit(() -> tmpNext.accept(o));
                } else {
                    tmpNext.accept(o);
                }
            };

            nextList[i] = next;
        }

        executorServices[0].submit(() -> {
            try {
                invokeCount.incrementAndGet();
                taskList.get(0).execute(context, input, nextList[0], errorHandler);
            } catch (Exception e) {
                errorHandler.accept(e);
            } finally {
                invokeCount.decrementAndGet();
                checkInvokeCount();
            }
        });

        return this;
    }

    private void checkInvokeCount() {
        if (invokeCount.get() == 0) {
            if (pipelineGrouperServiceList.size() == 0 || allGrouperEmpty()) {
                synchronized (finished) {
                    finished.set(true);
                    finished.notifyAll();
                }
            } else {
                pipelineGrouperServiceList.forEach(PipelineGrouperService::beforeTaskFinished);
                pipelineGrouperServiceList.clear();
            }
        }
    }

    private boolean allGrouperEmpty() {
        for (PipelineGrouperService item : pipelineGrouperServiceList) {
            if (item.getListSize() > 0) return false;
        }
        return true;
    }

    public void waitForFinish() throws InterruptedException {
        synchronized (finished) {
            while (!finished.get()) {
                finished.wait();
            }
        }
        for (ExecutorService executorService : executorServices) {
            if (executorService != null) executorService.shutdownNow();
        }
        log.info("Pipeline for: " + context + " finished after: " + Duration.between(startTime, Instant.now()));
    }
}

