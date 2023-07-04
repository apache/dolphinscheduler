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

package org.apache.dolphinscheduler.plugin.task.hivecli;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import org.apache.commons.io.FileUtils;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class HiveCliTaskTest {

    public static final String EXPECTED_HIVE_CLI_TASK_EXECUTE_FROM_SCRIPT_COMMAND =
            "hive -f 123_node.sql";

    public static final String EXPECTED_HIVE_CLI_TASK_EXECUTE_FROM_FILE_COMMAND =
            "hive -f 123_node.sql";

    public static final String EXPECTED_HIVE_CLI_TASK_EXECUTE_WITH_OPTIONS =
            "hive -f 123_node.sql --verbose";

    private MockedStatic<FileUtils> mockedStaticFileUtils;

    @BeforeEach
    public void setUp() {
        mockedStaticFileUtils = Mockito.mockStatic(FileUtils.class);
    }

    @AfterEach
    public void after() {
        mockedStaticFileUtils.close();
    }

    @Test
    public void hiveCliTaskExecuteSqlFromScript() throws Exception {
        String hiveCliTaskParameters = buildHiveCliTaskExecuteSqlFromScriptParameters();
        HiveCliTask hiveCliTask = prepareHiveCliTaskForTest(hiveCliTaskParameters);
        hiveCliTask.init();
        Assertions.assertEquals(hiveCliTask.buildCommand(), EXPECTED_HIVE_CLI_TASK_EXECUTE_FROM_SCRIPT_COMMAND);
    }

    @Test
    public void hiveCliTaskExecuteSqlFromFile() throws Exception {
        String hiveCliTaskParameters = buildHiveCliTaskExecuteSqlFromFileParameters();
        HiveCliTask hiveCliTask = prepareHiveCliTaskForTest(hiveCliTaskParameters);
        hiveCliTask.init();
        Assertions.assertEquals(hiveCliTask.buildCommand(), EXPECTED_HIVE_CLI_TASK_EXECUTE_FROM_FILE_COMMAND);
    }

    @Test
    public void hiveCliTaskExecuteWithOptions() throws Exception {
        String hiveCliTaskParameters = buildHiveCliTaskExecuteWithOptionsParameters();
        HiveCliTask hiveCliTask = prepareHiveCliTaskForTest(hiveCliTaskParameters);
        hiveCliTask.init();
        Assertions.assertEquals(hiveCliTask.buildCommand(), EXPECTED_HIVE_CLI_TASK_EXECUTE_WITH_OPTIONS);
    }

    private HiveCliTask prepareHiveCliTaskForTest(final String hiveCliTaskParameters) {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        when(taskExecutionContext.getTaskParams()).thenReturn(hiveCliTaskParameters);
        HiveCliTask hiveCliTask = spy(new HiveCliTask(taskExecutionContext));
        doReturn("123_node.sql").when(hiveCliTask).generateSqlScriptFile(Mockito.any());
        return hiveCliTask;
    }

    private String buildHiveCliTaskExecuteSqlFromScriptParameters() {
        final HiveCliParameters hiveCliParameters = new HiveCliParameters();
        hiveCliParameters.setHiveCliTaskExecutionType("SCRIPT");
        hiveCliParameters.setHiveSqlScript("SHOW DATABASES;");
        return JSONUtils.toJsonString(hiveCliParameters);
    }

    private String buildHiveCliTaskExecuteSqlFromFileParameters() {
        final HiveCliParameters hiveCliParameters = new HiveCliParameters();
        hiveCliParameters.setHiveCliTaskExecutionType("FILE");
        List<ResourceInfo> resources = new ArrayList<>();
        ResourceInfo sqlResource = new ResourceInfo();
        sqlResource.setResourceName("/sql_tasks/hive_task.sql");
        resources.add(sqlResource);
        hiveCliParameters.setResourceList(resources);
        return JSONUtils.toJsonString(hiveCliParameters);
    }

    private String buildHiveCliTaskExecuteWithOptionsParameters() {
        final HiveCliParameters hiveCliParameters = new HiveCliParameters();
        hiveCliParameters.setHiveCliTaskExecutionType("SCRIPT");
        hiveCliParameters.setHiveSqlScript("SHOW DATABASES;");
        hiveCliParameters.setHiveCliOptions("--verbose");
        return JSONUtils.toJsonString(hiveCliParameters);
    }

}
