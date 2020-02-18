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
import org.apache.dolphinscheduler.common.task.sqoop.SqoopParameters;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.SqoopJobGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HdfsSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.sources.HiveSourceGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HdfsTargetGenerator;
import org.apache.dolphinscheduler.server.worker.task.sqoop.generator.targets.HiveTargetGenerator;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author simfo
 * @date 2020/2/17 15:05
 */
public class SqoopTaskTest {

    @Test
    public void testGenerator(){
        String data1 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HDFS\",\"sourceParams\":\"{\\\"srcTable\\\":\\\"person\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"select * from person\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[],\\\"srcDatasource\\\":2}\",\"targetParams\":\"{\\\"targetPath\\\":\\\"/ods/tmp/test/pseron6\\\",\\\"deleteTargetDir\\\":true,\\\"fileType\\\":\\\"--as-avrodatafile\\\",\\\"compressionCodec\\\":\\\"snappy\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters1 = JSONObject.parseObject(data1,SqoopParameters.class);

        SqoopJobGenerator generator = new SqoopJobGenerator();
        String script = generator.generateSqoopJob(sqoopParameters1);
        String result = "sqoop import -m 1 --connect jdbc:mysql://172.16.90.146:3306/test --username kylo --password Lls@123! --table person --target-dir /ods/tmp/test/pseron6 --compression-codec snappy --as-avrodatafile --delete-target-dir --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(script, result);

        HdfsTargetGenerator hdfsTargetGenerator = new HdfsTargetGenerator();
        String targetHdfsStr = hdfsTargetGenerator.generate(sqoopParameters1);
        String hdfsTargetResult = " --target-dir /ods/tmp/test/pseron6 --compression-codec snappy --as-avrodatafile --delete-target-dir --null-non-string 'NULL' --null-string 'NULL'";
        Assert.assertEquals(targetHdfsStr, hdfsTargetResult);

        String data2 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HDFS\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"exportDir\\\":\\\"/ods/tmp/test/person6\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"\\\",\\\"linesTerminated\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters2 = JSONObject.parseObject(data2,SqoopParameters.class);

        HdfsSourceGenerator hdfsSourceGenerator = new HdfsSourceGenerator();
        String sourceHdfsStr =hdfsSourceGenerator.generate(sqoopParameters2);
        String sourceHdfsResult = " --export-dir /ods/tmp/test/person6";
        Assert.assertEquals(sourceHdfsStr, sourceHdfsResult);

        String data3 = "{\"concurrency\":1,\"modelType\":\"export\",\"sourceType\":\"HIVE\",\"targetType\":\"MYSQL\",\"sourceParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"pseron\\\",\\\"hivePartitionKey\\\":\\\"\\\",\\\"hivePartitionValue\\\":\\\"\\\"}\",\"targetParams\":\"{\\\"targetDatasource\\\":2,\\\"targetTable\\\":\\\"person3\\\",\\\"targetColumns\\\":\\\"\\\",\\\"fieldsTerminated\\\":\\\"\\\",\\\"linesTerminated\\\":\\\"\\\",\\\"preQuery\\\":\\\"\\\",\\\"isUpdate\\\":false,\\\"targetUpdateKey\\\":\\\"\\\",\\\"targetUpdateMode\\\":\\\"allowinsert\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters3 = JSONObject.parseObject(data3,SqoopParameters.class);

        HiveSourceGenerator hiveSourceGenerator = new HiveSourceGenerator();
        String sourceHiveStr = hiveSourceGenerator.generate(sqoopParameters3);
        String sourceHiveResult = " --hcatalog-database stg --hcatalog-table pseron";
        Assert.assertEquals(sourceHiveStr, sourceHiveResult);

        String data4 = "{\"concurrency\":1,\"modelType\":\"import\",\"sourceType\":\"MYSQL\",\"targetType\":\"HIVE\",\"sourceParams\":\"{\\\"srcDatasource\\\":2,\\\"srcTable\\\":\\\"person\\\",\\\"srcQueryType\\\":\\\"0\\\",\\\"srcQuerySql\\\":\\\"\\\",\\\"srcColumnType\\\":\\\"0\\\",\\\"srcColumns\\\":\\\"\\\",\\\"srcConditionList\\\":[],\\\"mapColumnHive\\\":[],\\\"mapColumnJava\\\":[]}\",\"targetParams\":\"{\\\"hiveDatabase\\\":\\\"stg\\\",\\\"hiveTable\\\":\\\"person_internal\\\",\\\"createHiveTable\\\":false,\\\"dropDelimiter\\\":false,\\\"hiveOverWrite\\\":true,\\\"replaceDelimiter\\\":\\\"\\\",\\\"hivePartitionKey\\\":\\\"\\\",\\\"hivePartitionValue\\\":\\\"\\\"}\",\"localParams\":[]}";
        SqoopParameters sqoopParameters4 = JSONObject.parseObject(data4,SqoopParameters.class);

        HiveTargetGenerator hiveTargetGenerator = new HiveTargetGenerator();
        String targetHiveStr = hiveTargetGenerator.generate(sqoopParameters4);
        String targetHiveResult = " --hive-import  --hive-table stg.person_internal --hive-overwrite -delete-target-dir";
        Assert.assertEquals(targetHiveStr, targetHiveResult);
    }
}
