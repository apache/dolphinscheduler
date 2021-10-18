/**
 * Aloudata.com Inc.
 * Copyright (c) 2021-2021 All Rights Reserved.
 */

package org.apache.dolphinscheduler.server.master.cache.impl;

import org.apache.dolphinscheduler.remote.command.TaskExecuteAckCommand;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author eye.gu@aloudata.com
 * @version 1
 * @date 2021-10-18 14:39
 */
@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceExecCacheManagerImplTest {

    @InjectMocks
    private ProcessInstanceExecCacheManagerImpl processInstanceExecCacheManager;

    @Mock
    private WorkflowExecuteThread workflowExecuteThread;

    @Before
    public void before() {
        Mockito.when(workflowExecuteThread.getKey()).thenReturn("workflowExecuteThread1");
        processInstanceExecCacheManager.cache(1, workflowExecuteThread);
    }

    @Test
    public void testGetByProcessInstanceId() {
        WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(1);
        Assert.assertEquals("workflowExecuteThread1", workflowExecuteThread.getKey());
    }

    @Test
    public void testContains() {
        Assert.assertTrue(processInstanceExecCacheManager.contains(1));
    }

    @Test
    public void testRemoveByProcessInstanceId() {
        processInstanceExecCacheManager.removeByProcessInstanceId(1);
        WorkflowExecuteThread workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(1);
        Assert.assertNull(workflowExecuteThread);
    }

    @Test
    public void testGetAll() {
        Collection<WorkflowExecuteThread> workflowExecuteThreads = processInstanceExecCacheManager.getAll();
        Assert.assertEquals(1, workflowExecuteThreads.size());
    }
}