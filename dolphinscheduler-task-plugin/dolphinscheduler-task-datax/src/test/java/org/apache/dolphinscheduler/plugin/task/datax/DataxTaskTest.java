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
import org.apache.dolphinscheduler.plugin.datasource.api.utils.DataSourceUtils;
import org.apache.dolphinscheduler.plugin.task.api.ShellCommandExecutor;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskRunStatus;
import org.apache.dolphinscheduler.plugin.task.api.model.ApplicationInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.model.TaskResponse;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.resource.ResourceContext;
import org.apache.dolphinscheduler.spi.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.node.ObjectNode;

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
            when(md.getColumnLabel(eq(1))).thenReturn("something");

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

    @Test
    public void testCustomConfigReadsJobDefinitionFromResourceFile() throws Exception {
        // a real resource file carrying the job definition, with a formatted empty object
        // placeholder inline (the semantic-absence rule, not a literal "{}" compare)
        String resourceJson = "{\"job\":{\"content\":[{\"reader\":{\"name\":\"mysqlreader\"}}]}}";
        File resourceFile = File.createTempFile("datax-job", ".json");
        resourceFile.deleteOnExit();
        java.nio.file.Files.write(resourceFile.toPath(),
                resourceJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        DataxParameters parameters = new DataxParameters();
        parameters.setCustomConfig(1);
        parameters.setJson("{\n  \n}");
        parameters.setXms(1);
        parameters.setXmx(1);
        ResourceInfo resourceInfo = new ResourceInfo();
        resourceInfo.setResourceName("/datax/job.json");
        parameters.setResourceList(java.util.Collections.singletonList(resourceInfo));

        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();
        // own app id so the generated file cannot collide with other tests' job files
        taskExecutionContext.setTaskAppId("app-id-resource");
        taskExecutionContext.setPrepareParamsMap(null);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(parameters));
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(ResourceContext.ResourceItem.builder()
                .resourceAbsolutePathInStorage("/datax/job.json")
                .resourceAbsolutePathInLocal(resourceFile.getAbsolutePath())
                .build());
        taskExecutionContext.setResourceContext(resourceContext);

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

        // the generated job file must carry the resource content, not the "{}" placeholder
        File jsonFile = new File("/tmp/execution/app-id-resource_job.json");
        String generated = FileUtils.readFile2Str(Files.newInputStream(jsonFile.toPath()));
        Assertions.assertTrue(generated.contains("mysqlreader"),
                "generated job file should contain the resource file definition, was: " + generated);
        Assertions.assertTrue(jsonFile.delete());
    }

    @Test
    public void testCustomConfigReadsJobFromJsonResourceNotFirstAuxiliaryResource() throws Exception {
        // resourceList carries a keytab BEFORE the job file. The worker must read the .json job
        // definition, not resourceList.get(0) which is the keytab (issue #18389, review by SbloodyS)
        String jobJson = "{\"job\":{\"content\":[{\"reader\":{\"name\":\"mysqlreader\"}}]}}";
        File jobFile = File.createTempFile("datax-job", ".json");
        jobFile.deleteOnExit();
        java.nio.file.Files.write(jobFile.toPath(),
                jobJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        File keytabFile = File.createTempFile("hdfs", ".keytab");
        keytabFile.deleteOnExit();
        java.nio.file.Files.write(keytabFile.toPath(),
                "keytab-binary-not-json".getBytes(java.nio.charset.StandardCharsets.UTF_8));

        DataxParameters parameters = new DataxParameters();
        parameters.setCustomConfig(1);
        parameters.setJson("{}");
        parameters.setXms(1);
        parameters.setXmx(1);
        ResourceInfo keytab = new ResourceInfo();
        keytab.setResourceName("/datax/hdfs.keytab");
        ResourceInfo job = new ResourceInfo();
        job.setResourceName("/datax/job.json");
        parameters.setResourceList(java.util.Arrays.asList(keytab, job));

        TaskExecutionContext taskExecutionContext = buildTestTaskExecutionContext();
        taskExecutionContext.setTaskAppId("app-id-multi-resource");
        taskExecutionContext.setPrepareParamsMap(null);
        taskExecutionContext.setTaskParams(JSONUtils.toJsonString(parameters));
        ResourceContext resourceContext = new ResourceContext();
        resourceContext.addResourceItem(ResourceContext.ResourceItem.builder()
                .resourceAbsolutePathInStorage("/datax/hdfs.keytab")
                .resourceAbsolutePathInLocal(keytabFile.getAbsolutePath())
                .build());
        resourceContext.addResourceItem(ResourceContext.ResourceItem.builder()
                .resourceAbsolutePathInStorage("/datax/job.json")
                .resourceAbsolutePathInLocal(jobFile.getAbsolutePath())
                .build());
        taskExecutionContext.setResourceContext(resourceContext);

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

        File jsonFile = new File("/tmp/execution/app-id-multi-resource_job.json");
        String generated = FileUtils.readFile2Str(Files.newInputStream(jsonFile.toPath()));
        Assertions.assertTrue(generated.contains("mysqlreader"),
                "generated job file should carry the .json resource content, was: " + generated);
        Assertions.assertFalse(generated.contains("keytab-binary-not-json"),
                "generated job file must not read the auxiliary keytab as the job definition");
        Assertions.assertTrue(jsonFile.delete());
    }

    private TaskExecutionContext buildTestTaskExecutionContext() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("app-id");
        taskExecutionContext.setExecutePath("/tmp/execution");
        return taskExecutionContext;
    }

    @Test
    public void testBuildDataxJobContentJsonWithBatchSize() throws Exception {
        // set batchSize > 0 via reflection
        Field dataXParametersField = DataxTask.class.getDeclaredField("dataXParameters");
        dataXParametersField.setAccessible(true);
        DataxParameters params = (DataxParameters) dataXParametersField.get(dataxTask);
        params.setBatchSize(1024);
        params.setDsType("MYSQL");
        params.setDtType("MYSQL");

        // set dataxTaskExecutionContext via reflection
        DataxTaskExecutionContext ctx = new DataxTaskExecutionContext();
        ctx.setSourcetype(DbType.MYSQL);
        ctx.setTargetType(DbType.MYSQL);
        ctx.setSourceConnectionParams(
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://localhost:3306\"}");
        ctx.setTargetConnectionParams(
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://localhost:3306\"}");

        Field ctxField = DataxTask.class.getDeclaredField("dataxTaskExecutionContext");
        ctxField.setAccessible(true);
        ctxField.set(dataxTask, ctx);

        BaseConnectionParam mockConnParam = mock(BaseConnectionParam.class);
        when(mockConnParam.getUser()).thenReturn("root");
        when(mockConnParam.getPassword()).thenReturn("123456");
        when(mockConnParam.getCompatibleMode()).thenReturn(null);

        try (
                MockedStatic<DataSourceUtils> mockedDataSourceUtils = mockStatic(DataSourceUtils.class);
                MockedStatic<DataSourceClientProvider> mockedProvider = mockStatic(DataSourceClientProvider.class)) {

            mockedDataSourceUtils
                    .when(() -> DataSourceUtils.buildConnectionParams(Mockito.any(DbType.class), Mockito.anyString()))
                    .thenReturn(mockConnParam);
            mockedDataSourceUtils.when(() -> DataSourceUtils.getJdbcUrl(Mockito.any(DbType.class), Mockito.any()))
                    .thenReturn("jdbc:mysql://localhost:3306/test");

            Connection connection = mock(Connection.class);
            mockedProvider.when(() -> DataSourceClientProvider.getAdHocConnection(Mockito.any(), Mockito.any()))
                    .thenReturn(connection);

            PreparedStatement stmt = mock(PreparedStatement.class);
            when(connection.prepareStatement(anyString())).thenReturn(stmt);
            ResultSetMetaData md = mock(ResultSetMetaData.class);
            when(md.getColumnCount()).thenReturn(1);
            when(md.getColumnLabel(eq(1))).thenReturn("col1");
            ResultSet resultSet = mock(ResultSet.class);
            when(resultSet.getMetaData()).thenReturn(md);
            when(stmt.executeQuery()).thenReturn(resultSet);

            Method method = DataxTask.class.getDeclaredMethod("buildDataxJobContentJson");
            method.setAccessible(true);
            Object invokeResult = method.invoke(dataxTask);
            Assertions.assertNotNull(invokeResult);
            List<?> result = (List<?>) invokeResult;

            Assertions.assertEquals(1, result.size());
            ObjectNode contentNode = (ObjectNode) result.get(0);
            ObjectNode writerParam = (ObjectNode) contentNode.get("writer").get("parameter");
            Assertions.assertTrue(writerParam.has("batchSize"));
            Assertions.assertEquals(1024, writerParam.get("batchSize").asInt());
        }
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
