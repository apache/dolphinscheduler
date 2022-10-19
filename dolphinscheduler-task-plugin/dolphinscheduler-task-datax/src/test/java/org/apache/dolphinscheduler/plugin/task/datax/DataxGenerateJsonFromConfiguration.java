package org.apache.dolphinscheduler.plugin.task.datax;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.datax.entity.ColumnInfo;
import org.apache.dolphinscheduler.plugin.task.datax.entity.DataxParameters;
import org.apache.dolphinscheduler.plugin.task.datax.enums.WriteMode;
import org.apache.dolphinscheduler.spi.enums.DbType;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.sql.Types;
import java.util.*;

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
        List<ColumnInfo> dsColumns = new ArrayList<>();
        dsColumns.add(new ColumnInfo(0,"col1","string",true, ""));
        dsColumns.add(new ColumnInfo(1,"col2","string",true, ""));
        dsColumns.add(new ColumnInfo(-1,"CURDATE()","custom",true, ""));
        dataxParameters.setDsColumns(dsColumns);
        //writer
        dataxParameters.setDtType("MYSQL");
        dataxParameters.setDataTarget(1);
        List<ColumnInfo> dtColumns = new ArrayList<>();
        dtColumns.add(new ColumnInfo(0,"newCol1","string",true, ""));
        dtColumns.add(new ColumnInfo(1,"newCol2","string",true, ""));
        dtColumns.add(new ColumnInfo(2,"loadTime()","datetime",true, ""));
        dataxParameters.setDtColumns(dtColumns);
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

    @Test
    public void HivePartitionTableToMySQLDataxConfigurationTest() throws Exception {
        String dsConnectionJson = "{\"user\":\"test\",\"password\":\"\",\"address\":\"jdbc:hive2://192.150.1.181:10001\",\"database\":\"default\",\"jdbcUrl\":\"jdbc:hive2://192.150.1.181:10001/default\",\"driverClassName\":\"org.apache.hive.jdbc.HiveDriver\",\"validationQuery\":\"select 1\"}";
        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        dataxTaskExecutionContext.setSourcetype(DbType.HIVE);
        dataxTaskExecutionContext.setSourceConnectionParams(dsConnectionJson);

        String dtConnectionJson = "{\"user\":\"ict_stud\",\"password\":\"wvzpDo5v^1!S5ws5\",\"address\":\"jdbc:mysql://192.150.1.181:3309\",\"database\":\"ict_stud\",\"jdbcUrl\":\"jdbc:mysql://192.150.1.181:3309/ict_stud\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\",\"other\":\"useUnicode=true&characterEncoding=UTF-8&\",\"props\":{\"useUnicode\":\"true\",\"characterEncoding\":\"UTF-8\"}}";
        dataxTaskExecutionContext.setTargetType(DbType.MYSQL);
        dataxTaskExecutionContext.setTargetConnectionParams(dtConnectionJson);

        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setCustomConfig(0);
        dataxParameters.setCustomSQL(0);
        //reader
        dataxParameters.setDsType("HIVE");
        dataxParameters.setDataSource(3);
        dataxParameters.setSourceTable("orc_table_partition_year_month_text");
        List<ColumnInfo> dsColumns = new ArrayList<>();
        dsColumns.add(new ColumnInfo(0,"city_id","int",true, ""));
        dsColumns.add(new ColumnInfo(1,"city_name","string",true, ""));
        dsColumns.add(new ColumnInfo(-1,"fixed_string","custom",true, ""));
        dataxParameters.setDsColumns(dsColumns);
        dataxParameters.setDsPartitions(Arrays.asList("year=2022","month=12"));
        //writer
        dataxParameters.setDtType("MYSQL");
        dataxParameters.setDataTarget(7);
        List<ColumnInfo> dtColumns = new ArrayList<>();
        dtColumns.add(new ColumnInfo(0,"city_id", JDBCType.valueOf(Types.INTEGER).getName(),true, ""));
        dtColumns.add(new ColumnInfo(1,"city_name",JDBCType.valueOf(Types.VARCHAR).getName(),true, ""));
        dtColumns.add(new ColumnInfo(2,"city_desc",JDBCType.valueOf(Types.VARCHAR).getName(),true, ""));
        dataxParameters.setDtColumns(dtColumns);
        dataxParameters.setTargetTable("sync_city");
        dataxParameters.setDtPartitions(new ArrayList<>());
        dataxParameters.setWriteMode(WriteMode.UPDATE.getCode());
        //setting
        dataxParameters.setBatchSize(2048);
        dataxParameters.setChannel(0);
        dataxParameters.setJobSpeedByte(3000);
        dataxParameters.setJobSpeedRecord(0);
        dataxParameters.setXms(8);
        dataxParameters.setXmx(16);

        //the datax json file will generate in dolphinscheduler-task-plugin/dolphinscheduler-task-datax/json/test_mysql_mysql_json.json
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("test_partition_hive_2_mysql");
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

    @Test
    public void MySQLToHivePartitionTableDataxConfigurationTest() throws Exception {
        String dsConnectionJson = "{\"user\":\"ict_stud\",\"password\":\"wvzpDo5v^1!S5ws5\",\"address\":\"jdbc:mysql://192.150.1.181:3309\",\"database\":\"ict_stud\",\"jdbcUrl\":\"jdbc:mysql://192.150.1.181:3309/ict_stud\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\",\"other\":\"useUnicode=true&characterEncoding=UTF-8&\",\"props\":{\"useUnicode\":\"true\",\"characterEncoding\":\"UTF-8\"}}";
        DataxTaskExecutionContext dataxTaskExecutionContext = new DataxTaskExecutionContext();
        dataxTaskExecutionContext.setTargetType(DbType.HIVE);
        dataxTaskExecutionContext.setSourceConnectionParams(dsConnectionJson);

        String dtConnectionJson = "{\"user\":\"test\",\"password\":\"\",\"address\":\"jdbc:hive2://192.150.1.181:10001\",\"database\":\"default\",\"jdbcUrl\":\"jdbc:hive2://192.150.1.181:10001/default\",\"driverClassName\":\"org.apache.hive.jdbc.HiveDriver\",\"validationQuery\":\"select 1\"}";
        dataxTaskExecutionContext.setSourcetype(DbType.MYSQL);
        dataxTaskExecutionContext.setTargetConnectionParams(dtConnectionJson);

        DataxParameters dataxParameters = new DataxParameters();
        dataxParameters.setCustomConfig(0);
        dataxParameters.setCustomSQL(0);
        //reader
        dataxParameters.setDsType("MYSQL");
        dataxParameters.setDataSource(7);
        dataxParameters.setSourceTable("sync_city");
        List<ColumnInfo> dsColumns = new ArrayList<>();
        dsColumns.add(new ColumnInfo(0,"city_id","int",true, ""));
        dsColumns.add(new ColumnInfo(1,"city_name","string",true, ""));
        dsColumns.add(new ColumnInfo(-1,"city_desc","string",false, ""));
        dataxParameters.setDsColumns(dsColumns);
        //writer
        dataxParameters.setDtType("HIVE");
        dataxParameters.setDataTarget(3);
        List<ColumnInfo> dtColumns = new ArrayList<>();
        dtColumns.add(new ColumnInfo(0,"city_id", "int",true, ""));
        dtColumns.add(new ColumnInfo(1,"city_name","string",true, ""));
        dataxParameters.setDtColumns(dtColumns);
        dataxParameters.setTargetTable("orc_table_partition_year_month_text");
        dataxParameters.setDtPartitions(Arrays.asList("year=2022","month=12"));
        dataxParameters.setWriteMode(WriteMode.APPEND.getCode());
        //setting
        dataxParameters.setBatchSize(2048);
        dataxParameters.setChannel(0);
        dataxParameters.setJobSpeedByte(3000);
        dataxParameters.setJobSpeedRecord(0);
        dataxParameters.setXms(8);
        dataxParameters.setXmx(16);

        //the datax json file will generate in dolphinscheduler-task-plugin/dolphinscheduler-task-datax/json/test_mysql_mysql_json.json
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        taskExecutionContext.setTaskAppId("test_mysql_2_partition_hive");
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
