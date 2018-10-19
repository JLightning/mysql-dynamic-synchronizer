package com.jhl.mds.util;

import com.jhl.mds.BaseTest;
import com.jhl.mds.util.pipeline.Pipeline;
import com.jhl.mds.util.pipeline.PipelineGrouperService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PipelineTest extends BaseTest {

    @Test
    public void simpleTest() throws InterruptedException {
        Pipeline<Object, Object, Object> pipeline = Pipeline.of(new Object(), Object.class);

        AtomicInteger test = new AtomicInteger();

        pipeline
                .append((context, input, next, errorHandler) -> {
                    for (int i = 0; i < 10000; i++) {
                        next.accept(i);
                    }
                })
                .append((context, input, next, errorHandler) -> {
                    test.incrementAndGet();
                })
                .execute()
                .waitForFinish();

        Assert.assertEquals(10000, test.get());
    }

    @Test
    public void grouperTest() throws InterruptedException {
        Pipeline<Object, Object, Object> pipeline = Pipeline.of(new Object(), Object.class);

        AtomicInteger test = new AtomicInteger();

        pipeline
                .append((context, input, next, errorHandler) -> {
                    for (int i = 0; i < 10000; i++) {
                        next.accept(i);
                    }
                })
                .append(new PipelineGrouperService<>(939))
                .append((context, input, next, errorHandler) -> {
                    int x = test.addAndGet(input.size());
                    log.info("x = " + x);
                })
                .execute()
                .waitForFinish();

        Assert.assertEquals(10000, test.get());
    }
}
