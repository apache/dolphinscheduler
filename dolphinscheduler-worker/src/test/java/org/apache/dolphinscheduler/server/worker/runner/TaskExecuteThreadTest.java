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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.processor.TaskCallbackService;
import org.apache.dolphinscheduler.server.worker.registry.WorkerRegistryClientTest;
import org.apache.dolphinscheduler.service.alert.AlertClientService;
import org.apache.dolphinscheduler.service.task.TaskPluginManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(PowerMockRunner.class)
public class TaskExecuteThreadTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkerRegistryClientTest.class);

    @Mock
    private TaskExecutionContext taskExecutionContext;

    @Mock
    private TaskCallbackService taskCallbackService;

    @Mock
    private AlertClientService alertClientService;

    @Mock
    private TaskPluginManager taskPluginManager;

    @Test
    public void checkTest(){
        TaskExecuteThread taskExecuteThread = new TaskExecuteThread(taskExecutionContext, taskCallbackService, alertClientService, taskPluginManager);

        String path = "/";
        Map<String, String> projectRes = new HashMap<>();
        projectRes.put("shell", "shell.sh");
        List<Pair<String, String>> downloads = new ArrayList<>();
        try{
            downloads = taskExecuteThread.downloadCheck(path, projectRes);
        }catch (Exception e){
            Assert.assertNotNull(e);
        }
        downloads.add(Pair.of("shell", "shell.sh"));
        try{
            taskExecuteThread.downloadResource(path, LOGGER, downloads);
        }catch (Exception e){

        }
    }
}
