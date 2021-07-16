package org.apache.dolphinscheduler.server.master.runner.task;

import static org.junit.Assert.*;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;

import org.junit.Test;

public class TaskProcessorFactoryTest {


    @Test
    public void testFactory(){

        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType("shell");

        ITaskProcessor iTaskProcessor = TaskProcessorFactory.getTaskProcessor(taskInstance.getTaskType());

        assertNotNull(iTaskProcessor);
    }

}