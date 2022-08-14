package org.apache.dolphinscheduler.service.queue;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service(Constants.TASK_DISPATCH_FAILED_QUEUE)
public class TaskDispatchFailedQueue implements TaskPriorityQueue<TaskPriority> {

    /**
     * task dispatch failed queue
     */
    private final PriorityBlockingQueue<TaskPriority> taskDispatchFailedQueue = new PriorityBlockingQueue<>(1000);

    @Override
    public void put(TaskPriority taskInfo) {
        taskDispatchFailedQueue.put(taskInfo);
    }

    @Override
    public TaskPriority take() throws TaskPriorityQueueException, InterruptedException {
        return taskDispatchFailedQueue.take();
    }

    @Override
    public TaskPriority poll(long timeout, TimeUnit unit) throws TaskPriorityQueueException, InterruptedException {
        return taskDispatchFailedQueue.poll(timeout, unit);
    }

    @Override
    public int size() throws TaskPriorityQueueException {
        return taskDispatchFailedQueue.size();
    }
}
