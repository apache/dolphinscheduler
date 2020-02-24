package org.apache.dolphinscheduler.dao.entity;

import org.junit.Assert;
import org.junit.Test;

/**
 * @ClassName TaskInstanceTest
 * @Author HuangLi
 * @Version 2020/2/24
 */
public class TaskInstanceTest {

    /**
     * task instance sub process
     */
    @Test
    public void testTaskInstanceIsSubProcess() {
        TaskInstance taskInstance = new TaskInstance();

        //sub process
        taskInstance.setTaskType("sub process");
        Assert.assertTrue(taskInstance.isSubProcess());

        //not sub process
        taskInstance.setTaskType("http");
        Assert.assertFalse(taskInstance.isSubProcess());
    }
}
