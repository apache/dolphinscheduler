package org.apache.dolphinscheduler.server.worker.task.etl;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.CommandType;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.job.db.BaseDataSource;
import org.apache.dolphinscheduler.common.job.db.DataSourceFactory;
import org.apache.dolphinscheduler.common.utils.SpringApplicationContext;
import org.apache.dolphinscheduler.dao.ProcessDao;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.server.utils.DataxUtils;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * DataxTask Tester.
 * 
 * @author wenhemin
 * @since
 * 
 *        <pre>
 * 一月 31, 2020
 *        </pre>
 * 
 * @version 1.0
 */
public class DataxTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(DataxTaskTest.class);

    private DataxTask dataxTask;

    private ProcessDao processDao;

    private ApplicationContext applicationContext;

    @Before
    public void before()
        throws Exception {
        processDao = Mockito.mock(ProcessDao.class);
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessDao.class)).thenReturn(processDao);

        TaskProps props = new TaskProps();
        props.setTaskDir("/tmp");
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTaskInstId(1);
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams(
            "{\"targetTable\":\"test\",\"postStatements\":[],\"jobSpeedRecord\":1000,\"dtType\":\"MYSQL\",\"datasource\":1,\"dsType\":\"MYSQL\",\"datatarget\":2,\"jobSpeedByte\":0,\"sql\":\"select 1 as test1, 2 as test2 from dual\",\"preStatements\":[\"delete from test\"]}");
        dataxTask = new DataxTask(props, logger);
        SpringApplicationContext.getBean(ProcessDao.class);

        MockitoAnnotations.initMocks(dataxTask);

        dataxTask.init();

        Mockito.when(processDao.findDataSourceById(1)).thenReturn(getDataSource());
        Mockito.when(processDao.findDataSourceById(2)).thenReturn(getDataSource());
        Mockito.when(processDao.findProcessInstanceByTaskId(1)).thenReturn(getProcessInstance());
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(
                "{\"user\":\"root\",\"password\":\"123456\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://127.0.0.1:3306/test\"}");
        dataSource.setUserId(1);
        return dataSource;
    }

    private ProcessInstance getProcessInstance() {
        ProcessInstance processInstance = new ProcessInstance();
        processInstance.setCommandType(CommandType.START_PROCESS);
        processInstance.setScheduleTime(new Date());
        return processInstance;
    }

    @After
    public void after()
        throws Exception {}

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

            Method method = dataxTask.getClass().getDeclaredMethod("parsingSqlColumnNames", DbType.class, DbType.class, BaseDataSource.class, String.class);
            method.setAccessible(true);
            String[] columns = (String[]) method.invoke(dataxTask, DbType.MYSQL, DbType.MYSQL, dataSource, "select 1 as a, 2 as `table` from dual");

            Assert.assertNotNull(columns);

            Assert.assertTrue(columns.length == 2);

            Assert.assertEquals(Arrays.toString(columns), "[`a`, `table`]");
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: tryGrammaticalParsingSqlColumnNames(DbType dbType, String sql)
     */
    @Test
    public void testTryGrammaticalAnalysisSqlColumnNames()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("tryGrammaticalAnalysisSqlColumnNames", DbType.class, String.class);
            method.setAccessible(true);
            String[] columns = (String[]) method.invoke(dataxTask, DbType.MYSQL, "select 1 as a, 2 as b from dual");

            Assert.assertNotNull(columns);

            Assert.assertTrue(columns.length == 2);

            Assert.assertEquals(Arrays.toString(columns), "[a, b]");
        }
        catch (Exception e) {
            throw e;
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
            Method method = dataxTask.getClass().getDeclaredMethod("buildDataxJsonFile");
            method.setAccessible(true);
            String filePath = (String) method.invoke(dataxTask, null);
            Assert.assertNotNull(filePath);
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: buildDataxJobContentJson()
     */
    @Test
    public void testBuildDataxJobContentJson()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("buildDataxJobContentJson");
            method.setAccessible(true);
            List<JSONObject> contentList = (List<JSONObject>) method.invoke(dataxTask, null);
            Assert.assertNotNull(contentList);

            JSONObject content = contentList.get(0);
            JSONObject reader = (JSONObject) content.get("reader");
            Assert.assertNotNull(reader);

            String readerPluginName = (String) reader.get("name");
            Assert.assertEquals(readerPluginName, DataxUtils.DATAX_READER_PLUGIN_MYSQL);

            JSONObject writer = (JSONObject) content.get("writer");
            Assert.assertNotNull(writer);

            String writerPluginName = (String) writer.get("name");
            Assert.assertEquals(writerPluginName, DataxUtils.DATAX_WRITER_PLUGIN_MYSQL);
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: buildDataxJobSettingJson()
     */
    @Test
    public void testBuildDataxJobSettingJson()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("buildDataxJobSettingJson");
            method.setAccessible(true);
            JSONObject setting = (JSONObject) method.invoke(dataxTask, null);
            Assert.assertNotNull(setting);
            Assert.assertNotNull(setting.get("speed"));
            Assert.assertNotNull(setting.get("errorLimit"));
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: buildDataxCoreJson()
     */
    @Test
    public void testBuildDataxCoreJson()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("buildDataxCoreJson");
            method.setAccessible(true);
            JSONObject coreConfig = (JSONObject) method.invoke(dataxTask, null);
            Assert.assertNotNull(coreConfig);
            Assert.assertNotNull(coreConfig.get("transport"));
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: buildShellCommandFile(String jobConfigFilePath)
     */
    @Test
    public void testBuildShellCommandFile()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("buildShellCommandFile", String.class);
            method.setAccessible(true);
            Assert.assertNotNull(method.invoke(dataxTask, "test.json"));
        }
        catch (Exception e) {
            throw e;
        }
    }

    /**
     * Method: notNull(Object obj, String message)
     */
    @Test
    public void testNotNull()
        throws Exception {
        try {
            Method method = dataxTask.getClass().getDeclaredMethod("notNull", Object.class, String.class);
            method.setAccessible(true);
            method.invoke(dataxTask, "abc", "test throw RuntimeException");
        }
        catch (Exception e) {
            throw e;
        }
    }

}
