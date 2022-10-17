package org.apache.dolphinscheduler.plugin.task.datax;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DataxGenerateJsonFromConfiguration {

    @Test
    public void MySQLToMySQLDataxConfigurationTest() throws Exception {
        String connectionJson = "{\"user\":\"root\",\"password\":\"pawword\",\"address\":\"jdbc:mysql://127.0.0.1:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://101.42.167.97:3306/test\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";

        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        dataxTaskExecutionContext.setSourcetype(DbType.MYSQL);
        dataxTaskExecutionContext.setSourceConnectionParams(connectionJson);
        dataxTaskExecutionContext.setTargetType(DbType.MYSQL);
        dataxTaskExecutionContext.setTargetConnectionParams(connectionJson);

        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setCustomConfig(0);
        dataxParameters.setCustomSQL(0);
        //reader
        dataxParameters.setDsType("MYSQL");
        dataxParameters.setDataSource(0);
        dataxParameters.setSourceTable("sourceTable");
        dataxParameters.setWhere("updateTime > '2022-01-01 00:00:00'");
        dataxParameters.setSplitPk("id");
        dataxParameters.setDsColumns(Arrays.asList("col1", "col2", "CURDATE()"));
        //writer
        dataxParameters.setDtType("MYSQL");
        dataxParameters.setDataTarget(1);
        dataxParameters.setDtColumns(Arrays.asList("newCol1", "newCol2", "loadTime"));
        dataxParameters.setTargetTable("targetTable");
        dataxParameters.setPreStatements(Collections.singletonList("truncate targetTable;"));
        dataxParameters.setPostStatements(Collections.singletonList("update targetTable set loadTime = now() where col1 = 1;"));
        dataxParameters.setWriteMode(WriteMode.INSERT.getCode());
        //setting
        dataxParameters.setBatchSize(2048);
        dataxParameters.setChannel(4);
        dataxParameters.setJobSpeedByte(3000);
        dataxParameters.setJobSpeedRecord(5);
        dataxParameters.setXms(8);
        dataxParameters.setXmx(16);

        //the datax json file will generate in dolphinscheduler-task-plugin/dolphinscheduler-task-datax/json/test_mysql_mysql_json.json
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("test_mysql_mysql");
        taskExecutionContext.setExecutePath("json");

        Class clz = Class.forName("org.apache.dolphinscheduler.plugin.task.datax.DataxTask");
        Constructor constructor = clz.getConstructor(TaskExecutionContext.class);
        DataxTask dataxTask = (DataxTask) constructor.newInstance(taskExecutionContext);
        Field field1 = clz.getDeclaredField("dataxTaskExecutionContext");
        field1.setAccessible(true);
        field1.set(dataxTask, dataxTaskExecutionContext);

        Field field2 = clz.getDeclaredField("dataXParameters");
        field2.setAccessible(true);
        field2.set(dataxTask, dataxParameters);
        Method method = clz.getDeclaredMethod("buildDataxJsonFile", Map.class);
        method.setAccessible(true);
        method.invoke(dataxTask, new HashMap<>());
    }
}
