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

import com.alibaba.fastjson.JSONObject;
import org.apache.dolphinscheduler.common.enums.DbType;
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.dao.entity.DataSource;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HdfsSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HiveSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.MysqlSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HdfsTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HiveTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.MysqlTargetGenerator;
import org.apache.dolphinscheduler.service.bean.SpringApplicationContext;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationContext;


/**
 * @author simfo
 * @date 2020/2/17 15:05
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class SqoopTaskTest {

    private ProcessService processService;
    private ApplicationContext applicationContext;

    @Before
    public void before(){
        processService = Mockito.mock(ProcessService.class);
        Mockito.when(processService.findDataSourceById(2)).thenReturn(getDataSource());
        applicationContext = Mockito.mock(ApplicationContext.class);
        SpringApplicationContext springApplicationContext = new SpringApplicationContext();
        springApplicationContext.setApplicationContext(applicationContext);
        Mockito.when(applicationContext.getBean(ProcessService.class)).thenReturn(processService);
    }

    @Test
    public void testGenerator(){
        String data1 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HDFS\",\"sourceParams\":\"{\\\"srcTable\\\":\\\"person\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"select * from person\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[],\\\"srcDatasource\\\":2}\",\"targetParams\":\"{\\\"targetPath\\\":\\\"/ods/tmp/test/pseron6\\\",\\\"deleteTargetDir\\\":true,\\\"fileType\\\":\\\"--as-avrodatafile\\\",\\\"compressionCodec\\\":\\\"snappy\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters1 = JSONObject.parseObject(data1,SqoopParameters.class);

        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters1);
        String expected = "sqoop import -m 1 --connect jdbc:mysql://172.16.90.146:3306/test --username kylo --password Lls@123! --table person --target-dir /ods/tmp/test/pseron6 --compression-codec snappy --as-avrodatafile --delete-target-dir --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(expected, script);

        MysqlSourceGenerator mysqlSourceGenerator = new MysqlSourceGenerator();
        String sourceMysqlStr =mysqlSourceGenerator.generate(sqoopParameters1);
        String sourceMysqlResult = " --connect jdbc:mysql://172.16.90.146:3306/test --username kylo --password Lls@123! --table person";
        Assert.assertEquals(sourceMysqlResult, sourceMysqlStr);

        HdfsTargetGenerator hdfsTargetGenerator = new HdfsTargetGenerator();
        String targetHdfsStr = hdfsTargetGenerator.generate(sqoopParameters1);
        String hdfsTargetResult = " --target-dir /ods/tmp/test/pseron6 --compression-codec snappy --as-avrodatafile --delete-target-dir --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(hdfsTargetResult, targetHdfsStr);

        String data2 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/ods/tmp/test/person6\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"\\\",\\\"linesTerminated\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters2 = JSONObject.parseObject(data2,SqoopParameters.class);

        HdfsSourceGenerator hdfsSourceGenerator = new HdfsSourceGenerator();
        String sourceHdfsStr =hdfsSourceGenerator.generate(sqoopParameters2);
        String sourceHdfsResult = " --export-dir /ods/tmp/test/person6";
        Assert.assertEquals(sourceHdfsResult, sourceHdfsStr);

        String data3 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HIVE\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"pseron\\\",\\\"hivePartitionKey\\\":\\\"\\\",\\\"hivePartitionValue\\\":\\\"\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"\\\",\\\"linesTerminated\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters3 = JSONObject.parseObject(data3,SqoopParameters.class);

        HiveSourceGenerator hiveSourceGenerator = new HiveSourceGenerator();
        String sourceHiveStr = hiveSourceGenerator.generate(sqoopParameters3);
        String sourceHiveResult = " --hcatalog-database stg --hcatalog-table pseron";
        Assert.assertEquals(sourceHiveResult, sourceHiveStr);

        String data4 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal\\\",\\\"createHiveTable\\\":false,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"\\\",\\\"hivePartitionValue\\\":\\\"\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters4 = JSONObject.parseObject(data4,SqoopParameters.class);

        HiveTargetGenerator hiveTargetGenerator = new HiveTargetGenerator();
        String targetHiveStr = hiveTargetGenerator.generate(sqoopParameters4);
        String targetHiveResult = " --hive-import  --hive-table stg.person_internal --hive-overwrite -delete-target-dir";
        Assert.assertEquals(targetHiveResult, targetHiveStr);

        String data5 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/opt\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person\\\",\\\"targetColumns\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters5 = JSONObject.parseObject(data5,SqoopParameters.class);

        MysqlTargetGenerator mysqlTargetGenerator = new MysqlTargetGenerator();
        String mysqlTargetStr = mysqlTargetGenerator.generate(sqoopParameters5);
        String mysqlTargetResult = " --connect jdbc:mysql://172.16.90.146:3306/test --username kylo --password Lls@123! --table person";

        Assert.assertEquals(mysqlTargetResult, mysqlTargetStr);
    }

    private DataSource getDataSource() {
        DataSource dataSource = new DataSource();
        dataSource.setType(DbType.MYSQL);
        dataSource.setConnectionParams(
                "{\"address\":\"jdbc:mysql://172.16.90.146:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://172.16.90.146:3306/test\",\"user\":\"kylo\",\"password\":\"Lls@123!\"}");
        dataSource.setUserId(1);
        return dataSource;
    }
}
