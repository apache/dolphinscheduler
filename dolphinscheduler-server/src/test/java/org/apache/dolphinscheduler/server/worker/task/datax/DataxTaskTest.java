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

package org.apache.dolphinscheduler.server.worker.task.datax;

import static org.apache.dolphinscheduler.common.enums.CommandType.START_PROCESS;

import org.apache.dolphinscheduler.common.datasource.BaseConnectionParam;
import org.apache.dolphinscheduler.common.datasource.DatasourceUtil;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.datax.DataxParameters;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.DataxUtils;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * DataxTask Tester.
 */
public class DataxTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DataxTaskTest.class);

    private static final String CONNECTION_PARAMS = " {\n"
                                                    + "    \"user\":\"root\",\n"
                                                    + "    \"password\":\"123456\",\n"
                                                    + "    \"address\":\"jdbc:mysql://127.0.0.1:3306\",\n"
                                                    + "    \"database\":\"test\",\n"
                                                    + "    \"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"\n"
                                                    + "}";

    private DataxTask dataxTask;

    private ProcessService processService;

    private ShellCommandExecutor shellCommandExecutor;

    private ApplicationContext applicationContext;

    private TaskExecutionContext taskExecutionContext;
    private final TaskProps props = new TaskProps();

    @Before
    public void before()
            throws Exception {
        setTaskParems(0);
    }

    private void setTaskParems(Integer customConfig) {

        processService = Mockito.mock(ProcessService.class);
        shellCommandExecutor = Mockito.mock(ShellCommandExecutor.class);

        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        if (customConfig == 1) {
            props.setTaskParams(
                    "{\n"
                            + "    \"customConfig\":1,\n"
                            + "    \"localParams\":[\n"
                            + "        {\n"
                            + "            \"prop\":\"test\",\n"
                            + "            \"value\":\"38294729\"\n"
                            + "        }\n"
                            + "    ],\n"
                            + "    \"json\":\""
                            + "{\"job\":{\"setting\":{\"speed\":{\"byte\":1048576},\"errorLimit\":{\"record\":0,\"percentage\":0.02}},\"content\":["
                            + "{\"reader\":{\"name\":\"rdbmsreader\",\"parameter\":{\"username\":\"xxx\",\"password\":\"${test}\",\"column\":[\"id\",\"name\"],\"splitPk\":\"pk\",\""
                            + "connection\":[{\"querySql\":[\"SELECT * from dual\"],\"jdbcUrl\":[\"jdbc:dm://ip:port/database\"]}],\"fetchSize\":1024,\"where\":\"1 = 1\"}},\""
                            + "writer\":{\"name\":\"streamwriter\",\"parameter\":{\"print\":true}}}]}}\"\n"
                            + "}");

        } else {
            props.setTaskParams(
                    "{\n"
                            + "    \"customConfig\":0,\n"
                            + "    \"targetTable\":\"test\",\n"
                            + "    \"postStatements\":[\n"
                            + "        \"delete from test\"\n"
                            + "    ],\n"
                            + "    \"jobSpeedByte\":0,\n"
                            + "    \"jobSpeedRecord\":1000,\n"
                            + "    \"dtType\":\"MYSQL\",\n"
                            + "    \"dataSource\":1,\n"
                            + "    \"dsType\":\"MYSQL\",\n"
                            + "    \"dataTarget\":2,\n"
                            + "    \"sql\":\"select 1 as test from dual\",\n"
                            + "    \"preStatements\":[\n"
                            + "        \"delete from test\"\n"
                            + "    ]\n"
                            + "}");
        }

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(taskExecutionContext.getTenantCode()).thenReturn("root");
        Mockito.when(taskExecutionContext.getStartTime()).thenReturn(new Date());
        Mockito.when(taskExecutionContext.getTaskTimeout()).thenReturn(10000);
        Mockito.when(taskExecutionContext.getLogPath()).thenReturn("/tmp/dx");

        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        dataxTaskExecutionContext.setSourcetype(0);
        dataxTaskExecutionContext.setTargetType(0);
        dataxTaskExecutionContext.setSourceConnectionParams(CONNECTION_PARAMS);
        dataxTaskExecutionContext.setTargetConnectionParams(CONNECTION_PARAMS);
        Mockito.when(taskExecutionContext.getDataxTaskExecutionContext()).thenReturn(dataxTaskExecutionContext);

        dataxTask = PowerMockito.spy(new DataxTask(taskExecutionContext, logger));
        dataxTask.init();
        props.setCmdTypeIfComplement(START_PROCESS);

        Mockito.when(processService.findDataSourceById(1)).thenReturn(getDataSource());
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        Mockito.when(processService.findProcessInstanceByTaskId(1)).thenReturn(getProcessInstance());

        String fileName = String.format("%s/%s_node.sh", props.getExecutePath(), props.getTaskAppId());
        try {
            Mockito.when(shellCommandExecutor.run(fileName)).thenReturn(null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dataxTask = PowerMockito.spy(new DataxTask(taskExecutionContext, logger));
        dataxTask.init();
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(CONNECTION_PARAMS);
        dataSource.setUserId(1);
        return dataSource;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(START_PROCESS);
        processInstance.setScheduleTime(new Date());
        return processInstance;
    }

    @After
    public void after()
            throws Exception {
    }

    /**
     * Method: DataxTask()
     */
    @Test
    public void testDataxTask()
            throws Exception {
        TaskProps props = new TaskProps();
        props.setExecutePath("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstanceId(1);
        props.setTenantCode("1");
        Assert.assertNotNull(new DataxTask(null, logger));
    }

    /**
     * Method: init
     */
    @Test
    public void testInit()
            throws Exception {
        try {
            dataxTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: handle()
     */
    @Test
    public void testHandle()
            throws Exception {
    }

    /**
     * Method: cancelApplication()
     */
    @Test
    public void testCancelApplication()
            throws Exception {
        try {
            dataxTask.cancelApplication(true);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: parsingSqlColumnNames(DbType dsType, DbType dtType, BaseDataSource
     * dataSourceCfg, String sql)
     */
    @Test
    public void testParsingSqlColumnNames()
            throws Exception {
        try {
            BaseConnectionParam dataSource = (BaseConnectionParam) DatasourceUtil.buildConnectionParams(
                    getDataSource().getType(),
                    getDataSource().getConnectionParams());

            Method method = DataxTask.class.getDeclaredMethod("parsingSqlColumnNames", DbType.class, DbType.class, BaseConnectionParam.class, String.class);
            method.setAccessible(true);
            String[] columns = (String[]) method.invoke(dataxTask, DbType.MYSQL, DbType.MYSQL, dataSource, "select 1 as a, 2 as `table` from dual");

            Assert.assertNotNull(columns);

            Assert.assertTrue(columns.length == 2);

            Assert.assertEquals("[`a`, `table`]", Arrays.toString(columns));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: tryGrammaticalParsingSqlColumnNames(DbType dbType, String sql)
     */
    @Test
    public void testTryGrammaticalAnalysisSqlColumnNames()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("tryGrammaticalAnalysisSqlColumnNames", DbType.class, String.class);
            method.setAccessible(true);
            String[] columns = (String[]) method.invoke(dataxTask, DbType.MYSQL, "select t1.a, t1.b from test t1 union all select a, t2.b from (select a, b from test) t2");

            Assert.assertNotNull(columns);

            Assert.assertTrue(columns.length == 2);

            Assert.assertEquals("[a, b]", Arrays.toString(columns));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: tryExecuteSqlResolveColumnNames(BaseDataSource baseDataSource,
     * String sql)
     */
    @Test
    public void testTryExecuteSqlResolveColumnNames()
            throws Exception {
        // TODO: Test goes here...
    }

    /**
     * Method: buildDataxJsonFile()
     */
    @Test
    @Ignore("method not found")
    public void testBuildDataxJsonFile()
            throws Exception {

        try {
            setTaskParems(1);
            Method method = DataxTask.class.getDeclaredMethod("buildDataxJsonFile");
            method.setAccessible(true);
            String filePath = (String) method.invoke(dataxTask, null);
            Assert.assertNotNull(filePath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildDataxJsonFile()
     */
    @Test
    @Ignore("method not found")
    public void testBuildDataxJsonFile0()
            throws Exception {
        try {
            setTaskParems(0);
            Method method = DataxTask.class.getDeclaredMethod("buildDataxJsonFile");
            method.setAccessible(true);
            String filePath = (String) method.invoke(dataxTask, null);
            Assert.assertNotNull(filePath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildDataxJobContentJson()
     */
    @Test
    public void testBuildDataxJobContentJson()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("buildDataxJobContentJson");
            method.setAccessible(true);
            List<ObjectNode> contentList = (List<ObjectNode>) method.invoke(dataxTask, null);
            Assert.assertNotNull(contentList);

            ObjectNode content = contentList.get(0);
            JsonNode reader = JSONUtils.parseObject(content.path("reader").toString());
            Assert.assertNotNull(reader);
            Assert.assertEquals("{\"name\":\"mysqlreader\",\"parameter\":{\"username\":\"root\","
                            + "\"password\":\"123456\",\"connection\":[{\"querySql\":[\"select 1 as test from dual\"],"
                            + "\"jdbcUrl\":[\"jdbc:mysql://127.0.0.1:3306/test?allowLoadLocalInfile=false"
                            + "&autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"]}]}}",
                    reader.toString());

            String readerPluginName = reader.path("name").asText();
            Assert.assertEquals(DataxUtils.DATAX_READER_PLUGIN_MYSQL, readerPluginName);

            JsonNode writer = JSONUtils.parseObject(content.path("writer").toString());
            Assert.assertNotNull(writer);
            Assert.assertEquals("{\"name\":\"mysqlwriter\",\"parameter\":{\"username\":\"root\","
                            + "\"password\":\"123456\",\"column\":[\"`test`\"],\"connection\":[{\"table\":[\"test\"],"
                            + "\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test?allowLoadLocalInfile=false&"
                            + "autoDeserialize=false&allowLocalInfile=false&allowUrlInLocalInfile=false\"}],"
                            + "\"preSql\":[\"delete from test\"],\"postSql\":[\"delete from test\"]}}",
                    writer.toString());

            String writerPluginName = writer.path("name").asText();
            Assert.assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_MYSQL, writerPluginName);

        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildDataxJobSettingJson()
     */
    @Test
    public void testBuildDataxJobSettingJson()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("buildDataxJobSettingJson");
            method.setAccessible(true);
            JsonNode setting = (JsonNode) method.invoke(dataxTask, null);
            Assert.assertNotNull(setting);
            Assert.assertEquals("{\"channel\":1,\"record\":1000}", setting.get("speed").toString());
            Assert.assertEquals("{\"record\":0,\"percentage\":0}", setting.get("errorLimit").toString());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildDataxCoreJson()
     */
    @Test
    public void testBuildDataxCoreJson()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("buildDataxCoreJson");
            method.setAccessible(true);
            ObjectNode coreConfig = (ObjectNode) method.invoke(dataxTask, null);
            Assert.assertNotNull(coreConfig);
            Assert.assertNotNull(coreConfig.get("transport"));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildShellCommandFile(String jobConfigFilePath)
     */
    @Test
    @Ignore("method not found")
    public void testBuildShellCommandFile()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("buildShellCommandFile", String.class);
            method.setAccessible(true);
            Assert.assertNotNull(method.invoke(dataxTask, "test.json"));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: getParameters
     */
    @Test
    public void testGetParameters()
            throws Exception {
        Assert.assertTrue(dataxTask.getParameters() != null);
    }

    /**
     * Method: notNull(Object obj, String message)
     */
    @Test
    public void testNotNull()
            throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("notNull", Object.class, String.class);
            method.setAccessible(true);
            method.invoke(dataxTask, "abc", "test throw RuntimeException");
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    @Test
    public void testGetPythonCommand()   {
        String pythonCommand = dataxTask.getPythonCommand();
        Assert.assertEquals("python2.7", pythonCommand);
        pythonCommand = dataxTask.getPythonCommand("");
        Assert.assertEquals("python2.7", pythonCommand);
        pythonCommand = dataxTask.getPythonCommand("/usr/bin/python");
        Assert.assertEquals("/usr/bin/python2.7", pythonCommand);
        pythonCommand = dataxTask.getPythonCommand("/usr/local/bin/python2");
        Assert.assertEquals("/usr/local/bin/python2.7", pythonCommand);
        pythonCommand = dataxTask.getPythonCommand("/opt/python/bin/python3.8");
        Assert.assertEquals("/opt/python/bin/python2.7", pythonCommand);
        pythonCommand = dataxTask.getPythonCommand("/opt/soft/python");
        Assert.assertEquals("/opt/soft/python/bin/python2.7", pythonCommand);
    }

    @Test
    public void testLoadJvmEnv()   {
        DataxTask dataxTask = new DataxTask(null,null);
        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setXms(0);
        dataxParameters.setXmx(-100);

        String actual =  dataxTask.loadJvmEnv(dataxParameters);

        String except = " --jvm=\"-Xms1G -Xmx1G\" ";
        Assert.assertEquals(except,actual);

        dataxParameters.setXms(13);
        dataxParameters.setXmx(14);
        actual =  dataxTask.loadJvmEnv(dataxParameters);
        except = " --jvm=\"-Xms13G -Xmx14G\" ";
        Assert.assertEquals(except,actual);

    }
}
