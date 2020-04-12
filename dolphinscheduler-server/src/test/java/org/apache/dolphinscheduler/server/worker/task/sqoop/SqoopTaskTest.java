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
package org.apache.dolphinscheduler.server.worker.task.sqoop;

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.worker.task.TaskProps;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * sqoop task test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SqoopTaskTest {

    private static final Logger logger = LoggerFactory.getLogger(SqoopTaskTest.class);

    private ProcessService processService;
    private ApplicationContext applicationContext;
    private SqoopTask sqoopTask;

    @Before
    public void before() throws Exception{
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);

        TaskProps props = new TaskProps();
        props.setTaskAppId(String.valueOf(System.currentTimeMillis()));
        props.setTenantCode("1");
        props.setEnvFile(".dolphinscheduler_env.sh");
        props.setTaskStartTime(new Date());
        props.setTaskTimeout(0);
        props.setTaskParams("{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"1\\\",\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}");

        sqoopTask = new SqoopTask(new TaskExecutionContext(),logger);
        sqoopTask.init();
    }

    @Test
    public void testGenerator(){
        String data1 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HDFS\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[]}\",\"targetParams\":\"{\\\"targetPath\\\":\\\"/ods/tmp/test/person7\\\",\\\"deleteTargetDir\\\":true,\\\"fileType\\\":\\\"--as-textfile\\\",\\\"compressionCodec\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters1 = JSON.parseObject(data1,SqoopParameters.class);

        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters1,new TaskExecutionContext());
        String expected = "sqoop import -m 1 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_2 --target-dir /ods/tmp/test/person7 --as-textfile --delete-target-dir --fields-terminated-by '@' --lines-terminated-by '\\n' --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(expected, script);

        String data2 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/ods/tmp/test/person7\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"id,name,age,sex,create_time\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":true,\\\"targetUpdateKey\\\":\\\"id\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters2 = JSON.parseObject(data2,SqoopParameters.class);

        String script2 = generator.generateSqoopJob(sqoopParameters2,new TaskExecutionContext());
        String expected2 = "sqoop export -m 1 --export-dir /ods/tmp/test/person7 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_3 --columns id,name,age,sex,create_time --fields-terminated-by '@' --lines-terminated-by '\\n' --update-key id --update-mode allowinsert";
        Assert.assertEquals(expected2, script2);

        String data3 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HIVE\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-17\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person_3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\",\\\"fieldsTerminated\\\":\\\"@\\\",\\\"linesTerminated\\\":\\\"\\\\\\\\n\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters3 = JSON.parseObject(data3,SqoopParameters.class);

        String script3 = generator.generateSqoopJob(sqoopParameters3,new TaskExecutionContext());
        String expected3 = "sqoop export -m 1 --hcatalog-database stg --hcatalog-table person_internal --hcatalog-partition-keys date --hcatalog-partition-values 2020-02-17 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --table person_3 --fields-terminated-by '@' --lines-terminated-by '\\n'";
        Assert.assertEquals(expected3, script3);

        String data4 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person_2\\\",\\\"srcQueryType\\\":\\\"1\\\",\\\"srcQuerySql\\\":\\\"SELECT * FROM person_2\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[{\\\"prop\\\":\\\"id\\\",\\\"direct\\\":\\\"IN\\\",\\\"type\\\":\\\"VARCHAR\\\",\\\"value\\\":\\\"Integer\\\"}]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal_2\\\",\\\"createHiveTable\\\":true,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"date\\\",\\\"hivePartitionValue\\\":\\\"2020-02-16\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters4 = JSON.parseObject(data4,SqoopParameters.class);

        String script4 = generator.generateSqoopJob(sqoopParameters4,new TaskExecutionContext());
        String expected4 = "sqoop import -m 1 --connect jdbc:mysql://192.168.0.111:3306/test --username kylo --password 123456 --query 'SELECT * FROM person_2 WHERE $CONDITIONS' --map-column-java id=Integer --hive-import  --hive-table stg.person_internal_2 --create-hive-table --hive-overwrite -delete-target-dir --hive-partition-key date --hive-partition-value 2020-02-16";
        Assert.assertEquals(expected4, script4);

    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(
                "{\"address\":\"jdbc:mysql://192.168.0.111:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://192.168.0.111:3306/test\",\"user\":\"kylo\",\"password\":\"123456\"}");
        dataSource.setUserId(1);
        return dataSource;
    }

    @Test
    public void testGetParameters() {
        Assert.assertNotNull(sqoopTask.getParameters());
    }

    /**
     * Method: init
     */
    @Test
    public void testInit(){
        try {
            sqoopTask.init();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

}
