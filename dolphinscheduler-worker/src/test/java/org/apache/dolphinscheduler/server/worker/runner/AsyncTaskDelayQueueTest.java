package org.apache.dolphinscheduler.server.worker.runner;

import lombok.NonNull;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskCallbackFunction;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecuteFunction;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.async.AsyncTaskExecutionStatus;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;

public class AsyncTaskDelayQueueTest {

    private Logger logger = LoggerFactory.getLogger(AsyncTaskDelayQueueTest.class);

    @Test
    public void testFirstTimeAddToDelayQueueWillNotDelay() {
        // assert the first time add to delay queue will not block
        Assertions.assertTimeout(Duration.ofSeconds(2), this::testAddAndPool);
    }

    private void testAddAndPool() throws InterruptedException {
        Thread addThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                AsyncTaskDelayQueue.addAsyncTask(createAsyncTaskExecutionContext());
                logger.info("Add a context: {}", i);
            }
        });
        Thread poolThread = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                try {
                    AsyncTaskExecutionContext asyncTaskExecutionContext = AsyncTaskDelayQueue.pollAsyncTask();
                    if (asyncTaskExecutionContext == null) {
                        throw new RuntimeException("Get a null context");
                    }
                    logger.info("Pool a context: {}", i);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    Assert.fail("Meet a interrupted exception");
                }
            }
        });
        poolThread.start();
        addThread.start();
        poolThread.join();
        addThread.join();
    }

    private AsyncTaskExecutionContext createAsyncTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        AsyncTaskExecuteFunction asyncTaskExecuteFunction = new AsyncTaskExecuteFunction() {

            @Override
            public @NonNull AsyncTaskExecutionStatus getTaskExecuteStatus() {
                return AsyncTaskExecutionStatus.SUCCESS;
            }

            @Override
            public @NonNull Duration getTaskExecuteInterval() {
                return Duration.ofSeconds(10);
            }
        };
        AsyncTaskCallbackFunction asyncTaskCallbackFunction = new AsyncTaskCallbackFunction() {

            @Override
            public void executeRunning() {
            }

            @Override
            public void executeSuccess() {
            }

            @Override
            public void executeFailed() {
            }

            @Override
            public void executeThrowing(Throwable throwable) {
            }
        };
        return new AsyncTaskExecutionContext(
                taskExecutionContext,
                asyncTaskExecuteFunction,
                asyncTaskCallbackFunction);
    }
}
