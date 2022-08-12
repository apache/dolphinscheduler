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

package org.apache.dolphinscheduler.plugin.task.api.parser;

import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.spi.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;

public class ParameterUtilsTest {

    @Test
    public void expandListParameter() {
        Map<Integer, Property> params = new HashMap<>();
        params.put(1, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1", "c2", "c3"))));
        params.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        params.put(3, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList(3.1415, 2.44, 3.44))));
        String sql = ParameterUtils.expandListParameter(params, "select * from test where col1 in (?) and date=? and col2 in (?)");
        Assert.assertEquals("select * from test where col1 in (?,?,?) and date=? and col2 in (?,?,?)", sql);
        Assert.assertEquals(7, params.size());

        Map<Integer, Property> params2 = new HashMap<>();
        params2.put(1, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1"))));
        params2.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        String sql2 = ParameterUtils.expandListParameter(params2, "select * from test where col1 in (?) and date=?");
        Assert.assertEquals("select * from test where col1 in (?) and date=?", sql2);
        Assert.assertEquals(2, params2.size());

    }

    @Test
    public void testBasicBuiltInParameterPlaceholders() {
        Map<String, String> params = new HashMap<>();
        params.put("system.datetime", "20220812000000");
        params.put("system.biz.curdate", "20220812");
        params.put("system.biz.date", "20220811");
        String content = "echo ${system.datetime}, ${system.biz.curdate}, ${system.biz.date}";
        String script = ParameterUtils.convertParameterPlaceholders(content, params);
        Assert.assertEquals("echo 20220812000000, 20220812, 20220811", script);
    }

    @Test
    public void testExtendedBuiltInParameterPlaceholders() {
        Map<String, String> params = new HashMap<>();
        params.put("ds.params.1", "ds.params.value.1");
        params.put("ds.params.2", "ds.params.value.2");
        String content = "echo ds.params.1=${ds.params.1}, ds.params.2=${ds.params.2}";
        String script = ParameterUtils.convertParameterPlaceholders(content, params);
        Assert.assertEquals("echo ds.params.1=ds.params.value.1, ds.params.2=ds.params.value.2", script);
    }

    @Test
    public void testExtendedBuiltInParameterPlaceholdersToBenchmarkVariable() {
        Map<String, String> params = new HashMap<>();
        params.put("system.datetime", "20220811070101");
        String content = "$[yyyyMMddHHmmss] $[yyyyMMdd] $[HHmmss]";
        String script = ParameterUtils.convertParameterPlaceholders(content, params);
        Assert.assertEquals("20220811070101 20220811 070101", script);

        Map<String, String> params2 = new HashMap<>();
        params2.put("system.datetime", "20220811070101");
        String content2 = "$[yyyy-MM-dd HH:mm:ss] $[yyyy-MM-dd] $[HH:mm:ss]";
        String script2 = ParameterUtils.convertParameterPlaceholders(content2, params2);
        Assert.assertEquals("2022-08-11 07:01:01 2022-08-11 07:01:01", script2);
    }

    @Test
    public void testExtendedBuiltInParameterPlaceholders4() {
        Map<String, String> params = new HashMap<>();
        params.put("system.datetime", "20220811070101");
        Assert.assertEquals("20230811",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyyMMdd,12*1)]", params));
        Assert.assertEquals("2023-08-11",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyy-MM-dd,12*1)]", params));
        Assert.assertEquals("20210811",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyyMMdd,-12*1)]", params));
        Assert.assertEquals("20220911",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyyMMdd,1)]", params));
        Assert.assertEquals("20220711",
                ParameterUtils.convertParameterPlaceholders("$[add_months(yyyyMMdd,-1)]", params));

        Assert.assertEquals("20220818",
                ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+7*1]", params));
        Assert.assertEquals("2022-08-18",
                ParameterUtils.convertParameterPlaceholders("$[yyyy-MM-dd+7*1]", params));
        Assert.assertEquals("20220804",
                ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd-7*1]", params));
        Assert.assertEquals("20220812",
                ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd+1]", params));
        Assert.assertEquals("20220810",
                ParameterUtils.convertParameterPlaceholders("$[yyyyMMdd-1]", params));

        Assert.assertEquals("090101",
                ParameterUtils.convertParameterPlaceholders("$[HHmmss+2/24]", params));
        Assert.assertEquals("09:01:01",
                ParameterUtils.convertParameterPlaceholders("$[HH:mm:ss+2/24]", params));
        Assert.assertEquals("09 01 01",
                ParameterUtils.convertParameterPlaceholders("$[HH mm ss+2/24]", params));
        Assert.assertEquals("050101",
                ParameterUtils.convertParameterPlaceholders("$[HHmmss-2/24]", params));
        Assert.assertEquals("070301",
                ParameterUtils.convertParameterPlaceholders("$[HHmmss+2/24/60]", params));
        Assert.assertEquals("065901",
                ParameterUtils.convertParameterPlaceholders("$[HHmmss-2/24/60]", params));

        String sql = "select JSON_EXTRACT(json_val, CONCAT('$[', id - 1, '].input1') from test";
        String sqlScript = ParameterUtils.convertParameterPlaceholders(sql, params);
        Assert.assertEquals("select JSON_EXTRACT(json_val, CONCAT('$[', id - 1, '].input1') from test",
                sqlScript);
    }

}
