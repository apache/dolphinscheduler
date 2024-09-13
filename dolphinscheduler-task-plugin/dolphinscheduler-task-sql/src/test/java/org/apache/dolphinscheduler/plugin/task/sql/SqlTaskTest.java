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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.enums.ResourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.DataSourceParameters;
import org.apache.dolphinscheduler.plugin.task.api.parameters.resource.ResourceParametersHelper;
import org.apache.dolphinscheduler.plugin.task.api.utils.ParameterUtils;
import org.apache.dolphinscheduler.spi.enums.DbType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

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
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));
    }

    @Test
    void testReplacingSqlWithDollarSymbol() {
        String querySql = "select concat(amount, '$') as price from product";
        String expected = "select concat(amount, '$') as price from product";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));
    }

    @Test
    void testReplacingHiveLoadSql() {
        String hiveLoadSql = "load inpath '/tmp/test_table/dt=${dt}' into table test_table partition(dt=${dt})";
        String expected = "load inpath '/tmp/test_table/dt=?' into table test_table partition(dt=?)";
        Assertions.assertEquals(expected, hiveLoadSql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("dt", new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        sqlTask.setSqlParamsMap(hiveLoadSql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void testReplacingSelectSql() {
        String querySql = "select id from student where dt='${dt}'";
        String expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("dt", new Property("dt", Direct.IN, DataType.VARCHAR, "1970"));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=\"${dt}\"";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=${dt}";
        expected = "select id from student where dt=?";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        querySql = "select id from student where dt=${dt} and gender=1";
        expected = "select id from student where dt=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        sqlParamsMap.clear();
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void testReplacingSqlNonGreedy() {
        String querySql = "select id from student where year=${year} and month=${month} and gender=1";
        String expected = "select id from student where year=? and month=? and gender=1";
        Assertions.assertEquals(expected, querySql.replaceAll(TaskConstants.SQL_PARAMS_REGEX, "?"));

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("year", new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        paramsMap.put("month", new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);
    }

    @Test
    void splitSql() {
    }

    @Test
    void testReplacingSqlHasQuestionMarkAndParams() {
        String querySql =
                "select id, concat('?', year) from student where year=${year} and month=${month} and gender in ('${gender}')";
        String expected =
                "select id, concat('?', year) from student where year=? and month=? and gender in (?,?)";

        Map<Integer, Property> sqlParamsMap = new HashMap<>();
        Map<Integer, Property> expectedSQLParamsMap = new HashMap<>();
        expectedSQLParamsMap.put(1, new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        expectedSQLParamsMap.put(2, new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        expectedSQLParamsMap.put(3,
                new Property("gender", Direct.IN, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList(1, 2))));
        Map<String, Property> paramsMap = new HashMap<>();
        paramsMap.put("year", new Property("year", Direct.IN, DataType.VARCHAR, "1970"));
        paramsMap.put("month", new Property("month", Direct.IN, DataType.VARCHAR, "12"));
        paramsMap.put("gender",
                new Property("gender", Direct.IN, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList(1, 2))));
        sqlTask.setSqlParamsMap(querySql, sqlParamsMap, paramsMap, 1);
        Assertions.assertEquals(sqlParamsMap, expectedSQLParamsMap);

        String formatSql = ParameterUtils.expandListParameter(sqlParamsMap, querySql);
        Assertions.assertEquals(4, sqlParamsMap.size());
        Assertions.assertEquals(expected, formatSql);
    }
}
