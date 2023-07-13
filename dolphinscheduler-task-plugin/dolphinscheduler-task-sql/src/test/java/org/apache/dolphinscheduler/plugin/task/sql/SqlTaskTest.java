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

package org.apache.dolphinscheduler.plugin.task.sql;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SqlTaskTest {

    private SqlTask sqlTask;

    @BeforeEach
    void setup() {
        DataSourceParameters parameters = new DataSourceParameters();
        parameters.setType(DbType.HIVE);
        parameters.setResourceType(ResourceType.DATASOURCE.name());

        ResourceParametersHelper resourceParametersHelper = new ResourceParametersHelper();
        resourceParametersHelper.put(ResourceType.DATASOURCE, 1, parameters);

        TaskExecutionContext ctx = new TaskExecutionContext();
        ctx.setResourceParametersHelper(resourceParametersHelper);
        ctx.setTaskParams("{\"type\":\"HIVE\",\"datasource\":1,\"sql\":\"select 1\"}");

        sqlTask = new SqlTask(ctx);
    }

    @Test
    void testReplacingSqlWithoutParams() {
        String querySql = "select 1";
        String expected = "select 1";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));
    }

    @Test
    void testReplacingSqlWithDollarSymbol() {
        String querySql = "select concat(amount, '$') as price from product";
        String expected = "select concat(amount, '$') as price from product";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));
    }

    @Test
    void testReplacingHiveLoadSql() {
        String hiveLoadSql = "load inpath '/tmp/test_table/dt=${dt}' into table test_table partition(dt=${dt})";
        String expected = "load inpath '/tmp/test_table/dt=?' into table test_table partition(dt=?)";
        Assertions.assertEquals(expected, hiveLoadSql.replaceAll(sqlTask.rgex, "?"));
    }

    @Test
    void testReplacingSelectSql() {
        String querySql = "select id from student where dt='${dt}'";
        String expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));

        querySql = "select id from student where dt=\"${dt}\"";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));

        querySql = "select id from student where dt=${dt}";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));

        querySql = "select id from student where dt=${dt} and gender=1";
        expected = "select id from student where dt=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));
    }

    @Test
    void testReplacingSqlNonGreedy() {
        String querySql = "select id from student where year=${year} and month=${month} and gender=1";
        String expected = "select id from student where year=? and month=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(sqlTask.rgex, "?"));
    }
}
