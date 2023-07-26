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

package org.apache.dolphinscheduler.plugin.task.datax;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.common.utils.FileUtils;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.datasource.api.plugin.DataSourceClientProvider;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskRunStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataxTaskTest {

    private DataxTask dataxTask;

    private final TaskCallBack taskCallBack = new TaskCallBack() {

        @Override
        public void updateRemoteApplicationInfo(int taskInstanceId, ApplicationInfo applicationInfo) {

        }

        @Override
        public void updateTaskInstanceInfo(int taskInstanceId) {

        }
    };

    @BeforeEach
    public void before() throws Exception {
        TaskExecutionContext taskExecutionContext = mock(TaskExecutionContext.class);
        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        String parameters = JSONUtils.toJsonString(createDataxParameters());
        when(taskExecutionContext.getTaskParams()).thenReturn(parameters);
        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);
        this.dataxTask = new DataxTask(taskExecutionContext);
        this.dataxTask.init();
    }

    @Test
    public void testHandleNullParamsMap() throws Exception {
        String parameters = JSONUtils.toJsonString(createDataxParameters());
        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();
        taskExecutionContext.setPrepareParamsMap(null);
        taskExecutionContext.setTaskParams(parameters);
        DataxTask dataxTask = new DataxTask(taskExecutionContext);
        dataxTask.init();

        ShellCommandExecutor shellCommandExecutor = mock(ShellCommandExecutor.class);
        Field shellCommandExecutorFiled = DataxTask.class.getDeclaredField("shellCommandExecutor");
        shellCommandExecutorFiled.setAccessible(true);
        shellCommandExecutorFiled.set(dataxTask, shellCommandExecutor);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setStatus(TaskRunStatus.SUCCESS);
        taskResponse.setExitStatusCode(0);
        taskResponse.setProcessId(1);
        when(shellCommandExecutor.run(any(), eq(taskCallBack))).thenReturn(taskResponse);

        dataxTask.handle(taskCallBack);
        Assertions.assertEquals(0, dataxTask.getExitStatusCode());

        File jsonFile = new File("/tmp/execution/app-id_job.json");
        InputStream json = Files.newInputStream(jsonFile.toPath());
        String resultStr = FileUtils.readFile2Str(json);
        Assertions.assertEquals(resultStr, getJsonString());
        boolean delete = jsonFile.delete();
        Assertions.assertTrue(delete);

        Assertions.assertEquals(dataxTask.buildCommand("/tmp/execution/app-id_job.json", null),
                "${PYTHON_LAUNCHER} ${DATAX_LAUNCHER} --jvm=\"-Xms1G -Xmx1G\"  /tmp/execution/app-id_job.json");
    }

    @Test
    public void testHandleParamsMap() throws Exception {
        String parameters = JSONUtils.toJsonString(createDataxParameters());
        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();

        taskExecutionContext.setPrepareParamsMap(createPrepareParamsMap());
        taskExecutionContext.setTaskParams(parameters);
        DataxTask dataxTask = new DataxTask(taskExecutionContext);
        dataxTask.init();

        ShellCommandExecutor shellCommandExecutor = mock(ShellCommandExecutor.class);
        Field shellCommandExecutorFiled = DataxTask.class.getDeclaredField("shellCommandExecutor");
        shellCommandExecutorFiled.setAccessible(true);
        shellCommandExecutorFiled.set(dataxTask, shellCommandExecutor);

        TaskResponse taskResponse = new TaskResponse();
        taskResponse.setStatus(TaskRunStatus.SUCCESS);
        taskResponse.setExitStatusCode(0);
        taskResponse.setProcessId(1);
        when(shellCommandExecutor.run(any(), eq(taskCallBack))).thenReturn(taskResponse);

        dataxTask.handle(taskCallBack);
        Assertions.assertEquals(0, dataxTask.getExitStatusCode());

        File jsonFile = new File("/tmp/execution/app-id_job.json");
        InputStream json = Files.newInputStream(jsonFile.toPath());
        String resultStr = FileUtils.readFile2Str(json);
        Assertions.assertEquals(resultStr, getJsonString());
        boolean delete = jsonFile.delete();
        Assertions.assertTrue(delete);

        Assertions.assertEquals(dataxTask.buildCommand("/tmp/execution/app-id_job.json", createPrepareParamsMap()),
                "${PYTHON_LAUNCHER} ${DATAX_LAUNCHER} --jvm=\"-Xms1G -Xmx1G\" -p \"-DDT='DT' -DDS='DS'\" /tmp/execution/app-id_job.json");
    }

    @Test
    public void testHandleInterruptedException() throws Exception {
        String parameters = JSONUtils.toJsonString(createDataxParameters());
        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();
        taskExecutionContext.setPrepareParamsMap(null);
        taskExecutionContext.setTaskParams(parameters);
        DataxTask dataxTask = new DataxTask(taskExecutionContext);
        dataxTask.init();

        ShellCommandExecutor shellCommandExecutor = mock(ShellCommandExecutor.class);
        Field shellCommandExecutorFiled = DataxTask.class.getDeclaredField("shellCommandExecutor");
        shellCommandExecutorFiled.setAccessible(true);
        shellCommandExecutorFiled.set(dataxTask, shellCommandExecutor);

        when(shellCommandExecutor.run(any(), eq(taskCallBack)))
                .thenThrow(new InterruptedException("Command execution failed"));
        Assertions.assertThrows(TaskException.class, () -> dataxTask.handle(taskCallBack));
    }

    @Test
    public void testHandleIOException() throws Exception {
        String parameters = JSONUtils.toJsonString(createDataxParameters());
        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();
        taskExecutionContext.setPrepareParamsMap(null);
        taskExecutionContext.setTaskParams(parameters);
        DataxTask dataxTask = new DataxTask(taskExecutionContext);
        dataxTask.init();

        ShellCommandExecutor shellCommandExecutor = mock(ShellCommandExecutor.class);
        Field shellCommandExecutorFiled = DataxTask.class.getDeclaredField("shellCommandExecutor");
        shellCommandExecutorFiled.setAccessible(true);
        shellCommandExecutorFiled.set(dataxTask, shellCommandExecutor);

        when(shellCommandExecutor.run(any(), eq(taskCallBack)))
                .thenThrow(new IOException("Command execution failed"));
        Assertions.assertThrows(TaskException.class, () -> dataxTask.handle(taskCallBack));
    }

    @Test
    public void testTryExecuteSqlResolveColumnNames() throws Exception {
        BaseConnectionParam baseConnectionParam = mock(BaseConnectionParam.class);
        try (
                MockedStatic<DataSourceClientProvider> mockedStaticDataSourceClientProvider =
                        mockStatic(DataSourceClientProvider.class)) {

            Connection connection = mock(Connection.class);
            when(DataSourceClientProvider.getAdHocConnection(Mockito.any(), Mockito.any())).thenReturn(connection);

            PreparedStatement stmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(stmt);

            ResultSetMetaData md = mock(ResultSetMetaData.class);
            when(md.getColumnCount()).thenReturn(1);
            when(md.getColumnName(eq(1))).thenReturn("something");

            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getMetaData()).thenReturn(md);
            when(stmt.executeQuery()).thenReturn(resultSet);

            String[] rows = this.dataxTask.tryExecuteSqlResolveColumnNames(DbType.MYSQL, baseConnectionParam, "");
            Assertions.assertEquals(rows.length, 1);
            Assertions.assertEquals(rows[0], "something");

            when(connection.prepareStatement(anyString())).thenThrow(new SQLException("Connection failed"));
            String[] nullRows = this.dataxTask.tryExecuteSqlResolveColumnNames(DbType.MYSQL, baseConnectionParam, "");
            Assertions.assertNull(nullRows);
        }
    }

    @Test
    public void testLoadJvmEnv() {
        DataxParameters dataXParameters = createDataxParameters();
        dataXParameters.setXms(3);
        dataXParameters.setXmx(4);
        Assertions.assertEquals(dataxTask.loadJvmEnv(dataXParameters), "--jvm=\"-Xms3G -Xmx4G\" ");
    }

    private DataxParameters createDataxParameters() {
        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setCustomConfig(1);
        dataxParameters.setDsType("mysql");
        dataxParameters.setDataSource(1);
        dataxParameters.setJson(getJsonString());
        dataxParameters.setDataTarget(2);
        dataxParameters.setSql("SELECT count(*) FROM table");
        dataxParameters.setTargetTable("user.name");
        return dataxParameters;
    }

    private Map<String, Property> createPrepareParamsMap() {
        Map<String, Property> paramsMap = new HashMap<>();
        Property dtProperty = new Property();
        dtProperty.setProp("DT");
        dtProperty.setDirect(Direct.IN);
        dtProperty.setType(DataType.VARCHAR);
        dtProperty.setValue("DT");
        Property dsProperty = new Property();
        dsProperty.setProp("DS");
        dsProperty.setDirect(Direct.IN);
        dsProperty.setType(DataType.VARCHAR);
        dsProperty.setValue("DS");
        paramsMap.put("DT", dtProperty);
        paramsMap.put("DS", dsProperty);
        return paramsMap;
    }

    private TaskExecutionContext buildTestTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("app-id");
        taskExecutionContext.setExecutePath("/tmp/execution");
        return taskExecutionContext;
    }

    private String getJsonString() {
        return "{\n" +
                "  \"job\": {\n" +
                "    \"content\": [\n" +
                "      {\n" +
                "        \"reader\": {\n" +
                "          \"name\": \"stream reader\",\n" +
                "          \"parameter\": {\n" +
                "            \"sliceRecordCount\": 10,\n" +
                "            \"column\": [\n" +
                "              {\n" +
                "                \"type\": \"long\",\n" +
                "                \"value\": \"10\"\n" +
                "              },\n" +
                "              {\n" +
                "                \"type\": \"string\",\n" +
                "                \"value\": \"Hello DataX\"\n" +
                "              }\n" +
                "            ]\n" +
                "          }\n" +
                "        },\n" +
                "        \"writer\": {\n" +
                "          \"name\": \"stream writer\",\n" +
                "          \"parameter\": {\n" +
                "            \"encoding\": \"UTF-8\",\n" +
                "            \"print\": true\n" +
                "          }\n" +
                "        }\n" +
                "      }\n" +
                "    ],\n" +
                "    \"setting\": {\n" +
                "      \"speed\": {\n" +
                "        \"channel\": 5\n" +
                "       }\n" +
                "    }\n" +
                "  }\n" +
                "}";
    }
}
