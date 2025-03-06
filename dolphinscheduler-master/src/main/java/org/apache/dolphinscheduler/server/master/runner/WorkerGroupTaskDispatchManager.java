package org.apache.dolphinscheduler.server.master.runner;
import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.server.master.engine.task.client.ITaskExecutorClient;
import org.apache.dolphinscheduler.server.master.engine.task.runnable.ITaskExecutionRunnable;
import org.apache.dolphinscheduler.server.master.runner.queue.DelayEntry;
import org.apache.dolphinscheduler.server.master.runner.queue.PriorityDelayQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WorkerGroupTaskDispatchManager implements AutoCloseable {


    @Autowired
    private ITaskExecutorClient taskExecutorClient;

    private ConcurrentHashMap<String, WorkerGroupTaskDispatchWaitingQueueLooper> workerGroupTaskDispatchWaitingQueueLooperMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>>> workerGroupPriorityDelayQueueMap = new ConcurrentHashMap<>();

    public WorkerGroupTaskDispatchManager() {
        workerGroupTaskDispatchWaitingQueueLooperMap = new ConcurrentHashMap<>();
        workerGroupPriorityDelayQueueMap = new ConcurrentHashMap<>();
    }

    /**
     * Adds a task to the specified worker group queue and starts or wakes up the corresponding processing loop.
     *
     * @param workerGroup the identifier for the worker group, used to distinguish different task queues
     * @param taskExecutionRunnable an instance of ITaskExecutionRunnable representing the task to be executed
     * @param delayTimeMills the delay time before the task is executed, in milliseconds
     */
    public void add(String workerGroup, ITaskExecutionRunnable taskExecutionRunnable, long delayTimeMills) {
        PriorityDelayQueue<DelayEntry<ITaskExecutionRunnable>> workerGroupQueue =
                workerGroupPriorityDelayQueueMap.computeIfAbsent(workerGroup, j -> new PriorityDelayQueue<>());

        workerGroupQueue.add(new DelayEntry<>(delayTimeMills, taskExecutionRunnable));
        try (WorkerGroupTaskDispatchWaitingQueueLooper looper = workerGroupTaskDispatchWaitingQueueLooperMap.computeIfAbsent(
                workerGroup,
                k -> new WorkerGroupTaskDispatchWaitingQueueLooper(workerGroup, this.taskExecutorClient, workerGroupQueue))) {
            looper.start();
        } catch (Exception e) {
            log.error("Error occurred while shutting down the thread manager: {}", e.getMessage(), e);
        }
    }

    /**
     * 停止所有workerGroupTaskDispatchWaitingQueueLooperMaps 里所有AutoCloseable
     */
    @Override
    public void close() throws Exception {
        // Iterate over all worker group task dispatch waiting queue loopers
        for (WorkerGroupTaskDispatchWaitingQueueLooper looper : workerGroupTaskDispatchWaitingQueueLooperMap.values()) {
            // Close each looper to stop the task dispatching process
            if (looper != null) {
                looper.close();
            }
        }
    }
}