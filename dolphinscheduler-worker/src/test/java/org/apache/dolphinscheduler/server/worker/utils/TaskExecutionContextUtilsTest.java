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

package org.apache.dolphinscheduler.server.worker.utils;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TaskExecutionContextUtilsTest {

    @Test
    void createTaskInstanceWorkingDirectory() throws IOException {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTenantCode("tenantCode");
        taskExecutionContext.setProjectCode(1);
        taskExecutionContext.setWorkflowDefinitionCode(1L);
        taskExecutionContext.setWorkflowDefinitionVersion(1);
        taskExecutionContext.setWorkflowInstanceId(1);
        taskExecutionContext.setTaskInstanceId(1);

        String taskWorkingDirectory =
                FileUtils.getTaskInstanceWorkingDirectory(taskExecutionContext.getTaskInstanceId());
        try {
            // Test if the working directory is exist
            // will delete it and recreate
            FileUtils.createDirectoryWith755(Paths.get(taskWorkingDirectory));
            Files.createFile(Paths.get(taskWorkingDirectory, "text.txt"));
            Assertions.assertTrue(Files.exists(Paths.get(taskWorkingDirectory, "text.txt")));

            TaskExecutionContextUtils.createTaskInstanceWorkingDirectory(taskExecutionContext);

            Assertions.assertEquals(taskWorkingDirectory, taskExecutionContext.getExecutePath());
            Assertions.assertFalse(Files.exists(Paths.get(taskWorkingDirectory, "text.txt")));
        } finally {
            FileUtils.deleteFile(taskWorkingDirectory);
        }
    }
}
