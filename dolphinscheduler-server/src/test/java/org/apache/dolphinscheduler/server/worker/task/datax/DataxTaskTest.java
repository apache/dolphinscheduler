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


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.dao.datasource.BaseDataSource;
import org.apache.dolphinscheduler.dao.datasource.DataSourceFactory;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.entity.DataxTaskExecutionContext;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.DataxUtils;
import org.apache.dolphinscheduler.server.worker.task.ShellCommandExecutor;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import static org.apache.dolphinscheduler.common.enums.CommandType.START_PROCESS;

/**
 * DataxTask Tester.
 */
public class DataxTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DataxTaskTest.class);

    private static final String CONNECTION_PARAMS = "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}";

    private DataxTask dataxTask;

    private ProcessService processService;

    private ShellCommandExecutor shellCommandExecutor;

    private ApplicationContext applicationContext;

    private TaskExecutionContext taskExecutionContext;
    private TaskProps props = new TaskProps();

    @Before
    public void before()
        throws Exception {
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
        props.setTaskParams(
            "{\"targetTable\":\"test\",\"postStatements\":[],\"jobSpeedByte\":1024,\"jobSpeedRecord\":1000,\"dtType\":\"MYSQL\",\"datasource\":1,\"dsType\":\"MYSQL\",\"datatarget\":2,\"jobSpeedByte\":0,\"sql\":\"select 1 as test from dual\",\"preStatements\":[\"delete from test\"],\"postStatements\":[\"delete from test\"]}");

        taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(props.getTaskParams());
        Mockito.when(taskExecutionContext.getExecutePath()).thenReturn("/tmp");
        Mockito.when(taskExecutionContext.getTaskAppId()).thenReturn("1");
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
        setTaskParems(0);

        Mockito.when(processService.findDataSourceById(1)).thenReturn(getDataSource());
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        Mockito.when(processService.findProcessInstanceByTaskId(1)).thenReturn(getProcessInstance());

        String fileName = String.format("%s/%s_node.sh", props.getExecutePath(), props.getTaskAppId());
        Mockito.when(shellCommandExecutor.run(fileName)).thenReturn(null);
    }

    private void setTaskParems(Integer customConfig) {
        if (customConfig == 1) {
            props.setTaskParams(
                    "{\"customConfig\":1, \"localParams\":[{\"prop\":\"test\",\"value\":\"38294729\"}],\"json\":\"{\\\"job\\\":{\\\"setting\\\":{\\\"speed\\\":{\\\"byte\\\":1048576},\\\"errorLimit\\\":{\\\"record\\\":0,\\\"percentage\\\":0.02}},\\\"content\\\":[{\\\"reader\\\":{\\\"name\\\":\\\"rdbmsreader\\\",\\\"parameter\\\":{\\\"username\\\":\\\"xxx\\\",\\\"password\\\":\\\"${test}\\\",\\\"column\\\":[\\\"id\\\",\\\"name\\\"],\\\"splitPk\\\":\\\"pk\\\",\\\"connection\\\":[{\\\"querySql\\\":[\\\"SELECT * from dual\\\"],\\\"jdbcUrl\\\":[\\\"jdbc:dm://ip:port/database\\\"]}],\\\"fetchSize\\\":1024,\\\"where\\\":\\\"1 = 1\\\"}},\\\"writer\\\":{\\\"name\\\":\\\"streamwriter\\\",\\\"parameter\\\":{\\\"print\\\":true}}}]}}\"}");

//                    "{\"customConfig\":1,\"json\":\"{\\\"job\\\":{\\\"setting\\\":{\\\"speed\\\":{\\\"byte\\\":1048576},\\\"errorLimit\\\":{\\\"record\\\":0,\\\"percentage\\\":0.02}},\\\"content\\\":[{\\\"reader\\\":{\\\"name\\\":\\\"rdbmsreader\\\",\\\"parameter\\\":{\\\"username\\\":\\\"xxx\\\",\\\"password\\\":\\\"xxx\\\",\\\"column\\\":[\\\"id\\\",\\\"name\\\"],\\\"splitPk\\\":\\\"pk\\\",\\\"connection\\\":[{\\\"querySql\\\":[\\\"SELECT * from dual\\\"],\\\"jdbcUrl\\\":[\\\"jdbc:dm://ip:port/database\\\"]}],\\\"fetchSize\\\":1024,\\\"where\\\":\\\"1 = 1\\\"}},\\\"writer\\\":{\\\"name\\\":\\\"streamwriter\\\",\\\"parameter\\\":{\\\"print\\\":true}}}]}}\"}");
        } else {
            props.setTaskParams(
                    "{\"customConfig\":0,\"targetTable\":\"test\",\"postStatements\":[],\"jobSpeedByte\":1024,\"jobSpeedRecord\":1000,\"dtType\":\"MYSQL\",\"datasource\":1,\"dsType\":\"MYSQL\",\"datatarget\":2,\"jobSpeedByte\":0,\"sql\":\"select 1 as test from dual\",\"preStatements\":[\"delete from test\"],\"postStatements\":[\"delete from test\"]}");

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
        throws Exception {}

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
            BaseDataSource dataSource = DataSourceFactory.getDatasource(getDataSource().getType(),
                    getDataSource().getConnectionParams());

            Method method = DataxTask.class.getDeclaredMethod("parsingSqlColumnNames", DbType.class, DbType.class, BaseDataSource.class, String.class);
            method.setAccessible(true);
            String[] columns = (String[]) method.invoke(dataxTask, DbType.MYSQL, DbType.MYSQL, dataSource, "select 1 as a, 2 as `table` from dual");

            Assert.assertNotNull(columns);

            Assert.assertTrue(columns.length == 2);

            Assert.assertEquals("[`a`, `table`]", Arrays.toString(columns));
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
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
    public void testBuildDataxJsonFile()
            throws Exception {
        try {
            setTaskParems(1);
            buildDataJson();
            setTaskParems(0);
            buildDataJson();
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
    }

    public void buildDataJson() throws Exception {
        Method method = DataxTask.class.getDeclaredMethod("buildDataxJsonFile", new Class[]{Map.class});
        method.setAccessible(true);
        String filePath = (String) method.invoke(dataxTask, new Object[]{null});
        Assert.assertNotNull(filePath);
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
            List<JSONObject> contentList = (List<JSONObject>) method.invoke(dataxTask, null);
            Assert.assertNotNull(contentList);

            JSONObject content = contentList.get(0);
            JSONObject reader = (JSONObject) content.get("reader");
            Assert.assertNotNull(reader);

            String readerPluginName = (String) reader.get("name");
            Assert.assertEquals(DataxUtils.DATAX_READER_PLUGIN_MYSQL, readerPluginName);

            JSONObject writer = (JSONObject) content.get("writer");
            Assert.assertNotNull(writer);

            String writerPluginName = (String) writer.get("name");
            Assert.assertEquals(DataxUtils.DATAX_WRITER_PLUGIN_MYSQL, writerPluginName);
        }
        catch (Exception e) {
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
            JSONObject setting = (JSONObject) method.invoke(dataxTask, null);
            Assert.assertNotNull(setting);
            Assert.assertNotNull(setting.get("speed"));
            Assert.assertNotNull(setting.get("errorLimit"));
        }
        catch (Exception e) {
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
            JSONObject coreConfig = (JSONObject) method.invoke(dataxTask, null);
            Assert.assertNotNull(coreConfig);
            Assert.assertNotNull(coreConfig.get("transport"));
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    /**
     * Method: buildShellCommandFile(String jobConfigFilePath)
     */
    @Test
    public void testBuildShellCommandFile()
        throws Exception {
        try {
            Method method = DataxTask.class.getDeclaredMethod("buildShellCommandFile", String.class, Map.class);
            method.setAccessible(true);
            Assert.assertNotNull(method.invoke(dataxTask, "test.json", null));
        }
        catch (Exception e) {
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
        }
        catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
