package org.apache.dolphinscheduler.service.queue;

import static org.junit.Assert.*;

import org.apache.dolphinscheduler.common.enums.Priority;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.service.exceptions.TaskPriorityQueueException;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;

public class TaskPriorityQueueImplTest {

    @Test
    public void put() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        Assert.assertEquals(2,queue.size());
    }

    @Test
    public void take() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        int peekBeforeLength = queue.size();
        queue.take();
        Assert.assertTrue(queue.size() < peekBeforeLength);
    }

    @Test
    public void poll() throws Exception {
        TaskPriorityQueue queue = getPriorityQueue();
        int peekBeforeLength = queue.size();
        queue.poll(1000, TimeUnit.MILLISECONDS);
        queue.poll(1000, TimeUnit.MILLISECONDS);
        Assert.assertTrue(queue.size() == 0);
        System.out.println(System.currentTimeMillis());
        queue.poll(1000, TimeUnit.MILLISECONDS);
        System.out.println(System.currentTimeMillis());
    }

    @Test
    public void size() throws Exception {
       Assert.assertTrue( getPriorityQueue().size() == 2);
    }


    /**
     * get queue
     *
     * @return queue
     * @throws Exception
     */
    private TaskPriorityQueue getPriorityQueue() throws Exception {
        TaskPriorityQueue queue = new TaskPriorityQueueImpl();
        TaskPriority taskInstanceHigPriority = createTaskPriority(Priority.HIGH.getCode(), 1);
        TaskPriority taskInstanceMediumPriority = createTaskPriority(Priority.MEDIUM.getCode(), 2);
        queue.put(taskInstanceHigPriority);
        queue.put(taskInstanceMediumPriority);
        return queue;
    }


    /**
     * create task priority
     * @param priority
     * @param processInstanceId
     * @return
     */
    private TaskPriority createTaskPriority(Integer priority, Integer processInstanceId) {
        TaskPriority priorityOne = new TaskPriority(priority, processInstanceId, 0, 0, "default");
        return priorityOne;
    }
}