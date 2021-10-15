/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.cache.impl;

import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThread;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;


@RunWith(MockitoJUnitRunner.class)
public class ProcessInstanceExecCacheManagerImplTest {

    ProcessInstanceExecCacheManager processInstanceExecCacheManager = new ProcessInstanceExecCacheManagerImpl();

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
        Assert.assertNotNull(workflowExecuteThread);
        Assert.assertEquals("workflowExecuteThread1", workflowExecuteThread.getKey());
    }

    @Test
    public void testContains() {
        Assert.assertTrue(processInstanceExecCacheManager.contains(1));
    }

    @Test
    public void testRemoveByProcessInstanceId() {
        processInstanceExecCacheManager.removeByProcessInstanceId(1);
        Assert.assertNull(processInstanceExecCacheManager.getByProcessInstanceId(1));
    }

    @Test
    public void testGetAll() {
        Collection<WorkflowExecuteThread> workflowExecuteThreads = processInstanceExecCacheManager.getAll();
        Assert.assertEquals(1, workflowExecuteThreads.size());
        Assert.assertEquals("workflowExecuteThread1", workflowExecuteThreads.stream().findFirst().get().getKey());
    }
}