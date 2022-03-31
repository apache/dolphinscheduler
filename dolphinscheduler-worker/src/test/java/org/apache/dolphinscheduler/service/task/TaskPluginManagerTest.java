package org.apache.dolphinscheduler.service.task;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskPluginManagerTest {

    @Autowired
    private TaskPluginManager taskPluginManager;

    @Before
    public void before() throws Exception {
        taskPluginManager = new TaskPluginManager();
        taskPluginManager.installPlugin(null);
        Assert.assertNotNull(taskPluginManager.getTaskChannelMap());
    }

    @Test
    public void testGetTaskChannel() {
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_BLOCKING));
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_CONDITIONS));
        Assert.assertNotNull(taskPluginManager.getTaskChannel(TaskConstants.TASK_TYPE_SQL));
        Assert.assertNull(taskPluginManager.getTaskChannel("NUlL_NAME"));
    }
}
