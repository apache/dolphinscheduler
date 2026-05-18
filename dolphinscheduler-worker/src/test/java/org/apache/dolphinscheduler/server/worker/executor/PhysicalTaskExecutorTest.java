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

package org.apache.dolphinscheduler.server.worker.executor;

import org.apache.dolphinscheduler.plugin.storage.api.StorageOperator;
import org.apache.dolphinscheduler.plugin.task.api.TaskChannel;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.server.worker.utils.TaskExecutionContextUtils;
import org.apache.dolphinscheduler.server.worker.utils.TaskFilesTransferUtils;
import org.apache.dolphinscheduler.server.worker.utils.TenantUtils;
import org.apache.dolphinscheduler.task.executor.TaskExecutorState;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhysicalTaskExecutorTest {

    private final String tenantCode = "ubuntu";

    @Mock
    private StorageOperator storageOperator;
    @Mock
    private PhysicalTaskPluginFactory pluginFactory;
    @Mock
    private TaskChannel taskChannel;

    private PhysicalTaskExecutor executor;
    private TaskExecutionContext context;

    @BeforeEach
    void setUp() {
        long processDefineCode = 123;
        String taskName = "test";
        int processDefineVersion = 456;
        int processInstanceId = 678;
        int taskInstanceId = 789;
        context = TaskExecutionContext.builder()
                .workflowInstanceId(processInstanceId)
                .workflowDefinitionVersion(processDefineVersion)
                .workflowDefinitionCode(processDefineCode)
                .taskInstanceId(taskInstanceId)
                .taskName(taskName)
                .tenantCode(tenantCode)
                .startTime(System.currentTimeMillis())
                .build();
        PhysicalTaskExecutorBuilder physicalTaskExecutorBuilder = PhysicalTaskExecutorBuilder.builder()
                .taskExecutionContext(context)
                .storageOperator(storageOperator)
                .physicalTaskPluginFactory(pluginFactory)
                .build();
        executor = new PhysicalTaskExecutor(physicalTaskExecutorBuilder);
    }

    @Test
    void testInitializeTaskContext_shouldDownloadUpstreamFiles() {
        try (
                MockedStatic<TenantUtils> tenantUtilsMock = Mockito.mockStatic(TenantUtils.class);
                MockedStatic<TaskExecutionContextUtils> contextUtilsMock =
                        Mockito.mockStatic(TaskExecutionContextUtils.class);
                MockedStatic<TaskFilesTransferUtils> transferUtilsMock =
                        Mockito.mockStatic(TaskFilesTransferUtils.class)) {

            tenantUtilsMock.when(() -> TenantUtils.getOrCreateActualTenant(Mockito.any(), Mockito.any()))
                    .thenReturn(tenantCode);
            contextUtilsMock
                    .when(() -> TaskExecutionContextUtils.downloadResourcesIfNeeded(Mockito.any(), Mockito.any(),
                            Mockito.any()))
                    .thenReturn(new ResourceContext());

            Mockito.when(pluginFactory.getTaskChannel(Mockito.any(PhysicalTaskExecutor.class))).thenReturn(taskChannel);

            List<Property> downloadedFiles = new ArrayList<>();
            transferUtilsMock.when(() -> TaskFilesTransferUtils.tryDownloadUpstreamFiles(
                    Mockito.eq(taskChannel), Mockito.eq(context), Mockito.eq(storageOperator)))
                    .thenReturn(downloadedFiles);

            executor.initializeTaskContext();

            transferUtilsMock.verify(() -> TaskFilesTransferUtils.tryDownloadUpstreamFiles(
                    Mockito.eq(taskChannel), Mockito.eq(context), Mockito.eq(storageOperator)), Mockito.times(1));
        }
    }

    @Test
    void testTrackTaskExecutorState_whenTaskSucceeded_shouldUploadOutputFiles() {
        try (
                MockedStatic<TaskFilesTransferUtils> transferUtilsMock =
                        Mockito.mockStatic(TaskFilesTransferUtils.class)) {
            List<Property> uploaded = new ArrayList<>();
            transferUtilsMock.when(() -> TaskFilesTransferUtils.tryUploadOutputFiles(
                    Mockito.eq(context), Mockito.eq(storageOperator))).thenReturn(uploaded);

            PhysicalTaskExecutor spyExecutor = Mockito.spy(executor);
            Mockito.doReturn(TaskExecutorState.SUCCEEDED).when(spyExecutor).doTrackTaskPluginStatus();

            spyExecutor.trackTaskExecutorState();

            transferUtilsMock.verify(() -> TaskFilesTransferUtils.tryUploadOutputFiles(
                    Mockito.eq(context), Mockito.eq(storageOperator)), Mockito.times(1));
        }
    }

    @Test
    void testTrackTaskExecutorState_whenTaskFailed_shouldNotUploadOutputFiles() {
        try (
                MockedStatic<TaskFilesTransferUtils> transferUtilsMock =
                        Mockito.mockStatic(TaskFilesTransferUtils.class)) {
            PhysicalTaskExecutor spyExecutor = Mockito.spy(executor);
            Mockito.doReturn(TaskExecutorState.FAILED).when(spyExecutor).doTrackTaskPluginStatus();

            spyExecutor.trackTaskExecutorState();

            transferUtilsMock.verify(() -> TaskFilesTransferUtils.tryUploadOutputFiles(
                    Mockito.any(), Mockito.any()), Mockito.never());
        }
    }
}
