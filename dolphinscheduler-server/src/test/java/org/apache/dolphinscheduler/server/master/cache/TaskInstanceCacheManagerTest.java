package org.apache.dolphinscheduler.server.master.cache;

import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

/**
 * @author 离歌笑
 * @desc
 * @date 2020-05-22
 */
public class TaskInstanceCacheManagerTest {

    private TaskInstanceCacheManager taskInstanceCacheManager;

    @Before
    public void setup(){
        taskInstanceCacheManager= Mockito.mock(TaskInstanceCacheManager.class);
    }

    @Test
    public void getByTaskInstanceId() {
        // when
        TaskInstance taskInstance = taskInstance();
        Mockito.when(taskInstanceCacheManager.getByTaskInstanceId(1)).thenReturn(taskInstance);

        TaskInstance result = taskInstanceCacheManager.getByTaskInstanceId(1);
        // assert
        Assert.assertEquals(taskInstance,result);
    }

    @Test
    public void cacheTaskInstance() {
        TaskInstance taskInstance=taskInstance();
        Mockito.doNothing().when(taskInstanceCacheManager).cacheTaskInstance(taskInstance);
    }

    @Test
    public void removeByTaskInstanceId() {
        // when
        TaskInstance taskInstance=new TaskInstance();
        taskInstanceCacheManager.cacheTaskInstance(taskInstance);

        Mockito.doNothing().when(taskInstanceCacheManager).removeByTaskInstanceId(taskInstance.getId());
    }

    private TaskInstance taskInstance(){
        TaskInstance taskInstance=new TaskInstance();
        taskInstance.setId(1);
        taskInstance.setStartTime(new Date());
        return taskInstance;
    }

}