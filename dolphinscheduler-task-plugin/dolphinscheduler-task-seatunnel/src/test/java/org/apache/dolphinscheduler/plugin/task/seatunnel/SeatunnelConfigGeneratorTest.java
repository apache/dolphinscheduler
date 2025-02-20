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

package org.apache.dolphinscheduler.plugin.task.seatunnel;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.seatunnel.generator.SeatunnelConfigGenerator;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.DorisParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.HdfsFileParameters;
import org.apache.dolphinscheduler.plugin.task.seatunnel.parameter.MysqlParameters;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SeatunnelConfigGeneratorTest {

    @Test
    void testSeatunnelHDFSConfigGeneration() {
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);

        seatunnelParameters.setJobMode(JobModeEnum.BATCH);
        seatunnelParameters.setSourceType("HDFS");
        seatunnelParameters.setTargetType("HDFS");

        HdfsFileParameters sourceConfig = new HdfsFileParameters();
        sourceConfig.setFileFormat("parquet");
        sourceConfig.setDefaultFs("hdfs://hadoopcluster");
        sourceConfig.setFilePath("/tmp/dolphinscheduler/seautnnel/st_hdfs_source.parquet");

        List<Property> sourceCustomConfig = new ArrayList<>();
        sourceCustomConfig
                .add(new Property("hdfs_site_path", Direct.IN, DataType.VARCHAR, "/tmp/hadoop/hdfs-site.xml"));
        sourceCustomConfig.add(new Property("krb5_path", Direct.IN, DataType.VARCHAR, "/tmp/hadoop/krb5.conf"));
        sourceConfig.setCustomParams(sourceCustomConfig);

        HdfsFileParameters sinkConfig = new HdfsFileParameters();
        sinkConfig.setFileFormat("orc");
        sinkConfig.setDefaultFs("hdfs://hadoopcluster");
        sinkConfig.setFilePath("/tmp/dolphinscheduler/seautnnel/st_hdfs_sink.orc");

        List<Property> sinkCustomConfig = new ArrayList<>();
        sinkCustomConfig.add(new Property("hdfs_site_path", Direct.IN, DataType.VARCHAR, "/tmp/hadoop/hdfs-site.xml"));
        sinkConfig.setCustomParams(sinkCustomConfig);

        seatunnelParameters.setSourceConfig(JSONUtils.toJsonString(sourceConfig));
        seatunnelParameters.setTargetConfig(JSONUtils.toJsonString(sinkConfig));
        seatunnelParameters.setParallelism(5);

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();

        String generateConfig =
                SeatunnelConfigGenerator.generateSeatunnelJob(seatunnelParameters, seatunnelTaskExecutionContext);

        Assertions.assertEquals(RAW_SCRIPT_1, generateConfig);
    }

    @Test
    void testSeatunnelMysqlConfigGeneration() {
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);

        seatunnelParameters.setJobMode(JobModeEnum.BATCH);
        seatunnelParameters.setSourceType("MYSQL");
        seatunnelParameters.setTargetType("MYSQL");

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();
        String connectionParams =
                "{\"user\":\"root\",\"password\":\"root123\",\"address\":\"jdbc:mysql://xxx:3306\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql://xxx:3306/test\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";
        seatunnelTaskExecutionContext.setSourceConnectionParams(connectionParams);
        seatunnelTaskExecutionContext.setTargetConnectionParams(connectionParams);

        MysqlParameters sourceConfig = new MysqlParameters();
        sourceConfig.setDbType(DbType.MYSQL);
        sourceConfig.setDatabaseId(1);
        sourceConfig.setTable("source_mysql_table");

        MysqlParameters targetConfig = new MysqlParameters();
        targetConfig.setDbType(DbType.MYSQL);
        targetConfig.setDatabaseId(2);
        targetConfig.setTable("target_mysql_table");

        // add custom params
        targetConfig.setCustomParams(
                Collections.singletonList(new Property("max_retries", Direct.IN, DataType.VARCHAR, "3")));

        seatunnelParameters.setSourceConfig(JSONUtils.toJsonString(sourceConfig));
        seatunnelParameters.setTargetConfig(JSONUtils.toJsonString(targetConfig));
        seatunnelParameters.setParallelism(5);

        String generateConfig =
                SeatunnelConfigGenerator.generateSeatunnelJob(seatunnelParameters, seatunnelTaskExecutionContext);

        Assertions.assertEquals(RAW_SCRIPT_2, generateConfig);
    }

    @Test
    void testSeatunnelDorisConfigGeneration() {
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);

        seatunnelParameters.setJobMode(JobModeEnum.BATCH);
        seatunnelParameters.setSourceType("DORIS");
        seatunnelParameters.setTargetType("DORIS");

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();
        String connectionParams =
                "{\"user\":\"root\",\"password\":\"root123\",\"address\":\"jdbc:mysql:loadbalance://xxx:9030\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql:loadbalance://xxx:9030/test\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";
        seatunnelTaskExecutionContext.setSourceConnectionParams(connectionParams);
        seatunnelTaskExecutionContext.setTargetConnectionParams(connectionParams);

        DorisParameters sourceConfig = new DorisParameters();
        sourceConfig.setDbType(DbType.DORIS);
        sourceConfig.setDatabaseId(1);
        sourceConfig.setTable("source_doris_table");

        DorisParameters targetConfig = new DorisParameters();
        targetConfig.setDbType(DbType.DORIS);
        targetConfig.setDatabaseId(2);
        targetConfig.setTable("target_doris_table");

        // add custom params
        targetConfig.setCustomParams(
                Collections.singletonList(new Property("doris.batch.size", Direct.IN, DataType.VARCHAR, "10240")));

        seatunnelParameters.setSourceConfig(JSONUtils.toJsonString(sourceConfig));
        seatunnelParameters.setTargetConfig(JSONUtils.toJsonString(targetConfig));
        seatunnelParameters.setParallelism(5);

        String generateConfig =
                SeatunnelConfigGenerator.generateSeatunnelJob(seatunnelParameters, seatunnelTaskExecutionContext);

        Assertions.assertEquals(RAW_SCRIPT_3, generateConfig);
    }

    @Test
    void testSeatunnelConfigGenerationWithTransform() {
        SeatunnelParameters seatunnelParameters = new SeatunnelParameters();
        seatunnelParameters.setUseCustom(false);

        seatunnelParameters.setJobMode(JobModeEnum.BATCH);
        seatunnelParameters.setSourceType("DORIS");
        seatunnelParameters.setTargetType("DORIS");

        SeatunnelTaskExecutionContext seatunnelTaskExecutionContext = new SeatunnelTaskExecutionContext();
        String connectionParams =
                "{\"user\":\"root\",\"password\":\"root123\",\"address\":\"jdbc:mysql:loadbalance://xxx:9030\",\"database\":\"test\",\"jdbcUrl\":\"jdbc:mysql:loadbalance://xxx:9030/test\",\"driverClassName\":\"com.mysql.cj.jdbc.Driver\",\"validationQuery\":\"select 1\"}";
        seatunnelTaskExecutionContext.setSourceConnectionParams(connectionParams);
        seatunnelTaskExecutionContext.setTargetConnectionParams(connectionParams);

        DorisParameters sourceConfig = new DorisParameters();
        sourceConfig.setDbType(DbType.DORIS);
        sourceConfig.setDatabaseId(1);
        sourceConfig.setTable("source_doris_table");

        DorisParameters targetConfig = new DorisParameters();
        targetConfig.setDbType(DbType.DORIS);
        targetConfig.setDatabaseId(2);
        targetConfig.setTable("target_doris_table");

        // add custom params
        targetConfig.setCustomParams(
                Collections.singletonList(new Property("doris.batch.size", Direct.IN, DataType.VARCHAR, "10240")));

        seatunnelParameters.setSourceConfig(JSONUtils.toJsonString(sourceConfig));
        seatunnelParameters.setTargetConfig(JSONUtils.toJsonString(targetConfig));
        seatunnelParameters.setParallelism(5);

        String transform = "transform {\n" +
                "  Sql {\n" +
                "    source_table_name = \"fake\"\n" +
                "    result_table_name = \"fake1\"\n" +
                "    query = \"select id, name, age+1 from fake\"\n" +
                "  }\n" +
                "}";
        seatunnelParameters.setCustomDataFilter(true);
        seatunnelParameters.setCustomTransform(transform);

        String generateConfig =
                SeatunnelConfigGenerator.generateSeatunnelJob(seatunnelParameters, seatunnelTaskExecutionContext);

        Assertions.assertEquals(RAW_SCRIPT_4, generateConfig);
    }

    @Test
    public void testGetSourceAndResultTableFromTransform() {
        String transform = "transform {\n" +
                "    sql {\n" +
                "        source_table_name = \"fake\"\n" +
                "        result_table_name = \"fake1\"\n" +
                "        sql = \"select name,age from fake\"\n" +
                "    }\n" +
                "}";

        Map<String, String> sourceAndResultTableFromTransform =
                SeatunnelConfigGenerator.getSourceAndResultTableFromTransform(transform);

        Assertions.assertNotNull(sourceAndResultTableFromTransform);
        Assertions.assertEquals(sourceAndResultTableFromTransform.size(), 2);
        Assertions.assertTrue(sourceAndResultTableFromTransform.containsKey("source_table_name"));
        Assertions.assertTrue(sourceAndResultTableFromTransform.containsKey("result_table_name"));

    }

    private static final String RAW_SCRIPT_1 = "env {\n" +
            "  job.mode = \"BATCH\"" + "\n" +
            "  parallelism = 5" + "\n" +
            "}\n" +
            "source {\n" +
            "  HdfsFile {\n" +
            "    path = \"/tmp/dolphinscheduler/seautnnel/st_hdfs_source.parquet\"" + "\n" +
            "    file_format_type = \"parquet\"" + "\n" +
            "    fs.defaultFS = \"hdfs://hadoopcluster\"" + "\n" +
            "    hdfs_site_path = \"/tmp/hadoop/hdfs-site.xml\"" + "\n" +
            "    krb5_path = \"/tmp/hadoop/krb5.conf\"" + "\n" +
            "  }\n" +
            "}\n\n" +
            "sink {\n" +
            "  HdfsFile {\n" +
            "    path = \"/tmp/dolphinscheduler/seautnnel/st_hdfs_sink.orc\"" + "\n" +
            "    file_format_type = \"orc\"" + "\n" +
            "    fs.defaultFS = \"hdfs://hadoopcluster\"" + "\n" +
            "    hdfs_site_path = \"/tmp/hadoop/hdfs-site.xml\"" + "\n" +
            "  }\n" +
            "}";

    private static final String RAW_SCRIPT_2 = "env {\n" +
            "  job.mode = \"BATCH\"\n" +
            "  parallelism = 5\n" +
            "}\n" +
            "source {\n" +
            "  Jdbc {\n" +
            "    url = \"jdbc:mysql://xxx:3306/test?useUnicode=true&characterEncoding=UTF-8\"\n" +
            "    driver = \"com.mysql.cj.jdbc.Driver\"\n" +
            "    user = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    query = \"select * from source_mysql_table\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "sink {\n" +
            "  jdbc {\n" +
            "    url = \"jdbc:mysql://xxx:3306/test?useUnicode=true&characterEncoding=UTF-8\"\n" +
            "    driver = \"com.mysql.cj.jdbc.Driver\"\n" +
            "    user = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    database = \"test\"\n" +
            "    table = \"target_mysql_table\"\n" +
            "    generate_sink_sql = \"true\"\n" +
            "    max_retries = \"3\"\n" +
            "  }\n" +
            "}";

    private static final String RAW_SCRIPT_3 = "env {\n" +
            "  job.mode = \"BATCH\"\n" +
            "  parallelism = 5\n" +
            "}\n" +
            "source {\n" +
            "  Doris {\n" +
            "    fenodes = \"xxx:8030\"\n" +
            "    username = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    database = \"test\"\n" +
            "    table = \"source_doris_table\"\n" +
            "  }\n" +
            "}\n" +
            "\n" +
            "sink {\n" +
            "  Doris {\n" +
            "    fenodes = \"xxx:8030\"\n" +
            "    username = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    database = \"test\"\n" +
            "    table = \"target_doris_table\"\n" +
            "    doris.batch.size = \"10240\"\n" +
            "    doris.config {\n" +
            "      format = \"json\"\n" +
            "      read_json_by_line = \"true\"\n" +
            "    }\n" +
            "  }\n" +
            "}";

    private static final String RAW_SCRIPT_4 = "env {\n" +
            "  job.mode = \"BATCH\"\n" +
            "  parallelism = 5\n" +
            "}\n" +
            "source {\n" +
            "  Doris {\n" +
            "    fenodes = \"xxx:8030\"\n" +
            "    username = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    database = \"test\"\n" +
            "    table = \"source_doris_table\"\n" +
            "  result_table_name = \"fake\"\n" +
            "  }\n" +
            "}\n" +
            "transform {\n" +
            "  Sql {\n" +
            "    source_table_name = \"fake\"\n" +
            "    result_table_name = \"fake1\"\n" +
            "    query = \"select id, name, age+1 from fake\"\n" +
            "  }\n" +
            "}\n" +
            "sink {\n" +
            "  Doris {\n" +
            "    fenodes = \"xxx:8030\"\n" +
            "    username = \"root\"\n" +
            "    password = \"root123\"\n" +
            "    database = \"test\"\n" +
            "    table = \"target_doris_table\"\n" +
            "    doris.batch.size = \"10240\"\n" +
            "    doris.config {\n" +
            "      format = \"json\"\n" +
            "      read_json_by_line = \"true\"\n" +
            "    }\n" +
            "  source_table_name = \"fake1\"\n" +
            "  }\n" +
            "}";

}
