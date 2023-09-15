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

import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;

import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ProcessInstanceExecCacheManagerImplTest {

    @InjectMocks
    private ProcessInstanceExecCacheManagerImpl processInstanceExecCacheManager;

    @Mock
    private WorkflowExecuteRunnable workflowExecuteThread;

    @BeforeEach
    public void before() {
        processInstanceExecCacheManager.cache(1, workflowExecuteThread);
    }

    @Test
    public void testGetByProcessInstanceId() {
        WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(1);
        Assertions.assertNotNull(workflowExecuteThread);
    }

    @Test
    public void testContains() {
        Assertions.assertTrue(processInstanceExecCacheManager.contains(1));
    }

    @Test
    public void testCacheNull() {
        Assertions.assertThrows(NullPointerException.class, () -> processInstanceExecCacheManager.cache(2, null));
        WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(2);
        Assertions.assertNull(workflowExecuteThread);
    }

    @Test
    public void testRemoveByProcessInstanceId() {
        processInstanceExecCacheManager.removeByProcessInstanceId(1);
        WorkflowExecuteRunnable workflowExecuteThread = processInstanceExecCacheManager.getByProcessInstanceId(1);
        Assertions.assertNull(workflowExecuteThread);
    }

    @Test
    public void testGetAll() {
        Collection<WorkflowExecuteRunnable> workflowExecuteThreads = processInstanceExecCacheManager.getAll();
        Assertions.assertEquals(1, workflowExecuteThreads.size());
    }
}
