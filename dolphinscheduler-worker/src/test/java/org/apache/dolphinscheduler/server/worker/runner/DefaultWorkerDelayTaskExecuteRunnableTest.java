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

package org.apache.dolphinscheduler.server.worker.runner;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.plugin.storage.api.StorageOperate;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.TaskPluginManager;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.server.worker.config.WorkerConfig;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClient;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerMessageSender;
import org.apache.dolphinscheduler.server.worker.rpc.WorkerRpcClient;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class DefaultWorkerDelayTaskExecuteRunnableTest {

    private TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);

    private WorkerConfig workerConfig = Mockito.mock(WorkerConfig.class);

    private String masterAddress = "localhost:5678";

    private WorkerMessageSender workerMessageSender = Mockito.mock(WorkerMessageSender.class);

    private WorkerRpcClient alertClientService = Mockito.mock(WorkerRpcClient.class);

    private TaskPluginManager taskPluginManager = Mockito.mock(TaskPluginManager.class);

    private StorageOperate storageOperate = Mockito.mock(StorageOperate.class);

    private WorkerRegistryClient workerRegistryClient = Mockito.mock(WorkerRegistryClient.class);

    @Test
    public void testDryRun() {
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .dryRun(Constants.DRY_RUN_FLAG_YES)
                .taskInstanceId(0)
                .processDefineId(0)
                .firstSubmitTime(System.currentTimeMillis())
                .build();
        WorkerTaskExecuteRunnable workerTaskExecuteRunnable = new DefaultWorkerDelayTaskExecuteRunnable(
                taskExecutionContext,
                workerConfig,
                workerMessageSender,
                alertClientService,
                taskPluginManager,
                storageOperate,
                workerRegistryClient);

        Assertions.assertAll(workerTaskExecuteRunnable::run);
        Assertions.assertEquals(TaskExecutionStatus.SUCCESS, taskExecutionContext.getCurrentExecutionStatus());
    }

    @Test
    public void testErrorboundTestDataSource() {
        TaskExecutionContext taskExecutionContext = TaskExecutionContext.builder()
                .dryRun(Constants.DRY_RUN_FLAG_NO)
                .testFlag(Constants.TEST_FLAG_YES)
                .taskInstanceId(0)
                .processDefineId(0)
                .firstSubmitTime(System.currentTimeMillis())
                .taskType("SQL")
                .taskParams(
                        "{\"localParams\":[],\"resourceList\":[],\"type\":\"POSTGRESQL\",\"datasource\":null,\"sql\":\"select * from t_ds_user\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"segmentSeparator\":\"\",\"displayRows\":10,\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}")
                .build();
        WorkerTaskExecuteRunnable workerTaskExecuteRunnable = new DefaultWorkerDelayTaskExecuteRunnable(
                taskExecutionContext,
                workerConfig,
                workerMessageSender,
                alertClientService,
                taskPluginManager,
                storageOperate,
                workerRegistryClient);

        Assertions.assertAll(workerTaskExecuteRunnable::run);
        Assertions.assertEquals(TaskExecutionStatus.FAILURE, taskExecutionContext.getCurrentExecutionStatus());
    }
}
