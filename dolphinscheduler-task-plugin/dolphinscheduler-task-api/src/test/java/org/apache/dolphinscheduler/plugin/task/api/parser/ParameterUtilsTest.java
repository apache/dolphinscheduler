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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

public class ParameterUtilsTest {

    @Test
    public void expandListParameter() {
        Map<Integer, Property> params = new HashMap<>();
        params.put(1,
                new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1", "c2", "c3"))));
        params.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        params.put(3, new Property(null, null, DataType.LIST,
                JSONUtils.toJsonString(Lists.newArrayList(3.1415, 2.44, 3.44))));
        String sql = ParameterUtils.expandListParameter(params,
                "select * from test where col1 in (?) and date=? and col2 in (?)");
        Assertions.assertEquals("select * from test where col1 in (?,?,?) and date=? and col2 in (?,?,?)", sql);
        Assertions.assertEquals(7, params.size());

        Map<Integer, Property> params2 = new HashMap<>();
        params2.put(1, new Property(null, null, DataType.LIST, JSONUtils.toJsonString(Lists.newArrayList("c1"))));
        params2.put(2, new Property(null, null, DataType.DATE, "2020-06-30"));
        String sql2 = ParameterUtils.expandListParameter(params2, "select * from test where col1 in (?) and date=?");
        Assertions.assertEquals("select * from test where col1 in (?) and date=?", sql2);
        Assertions.assertEquals(2, params2.size());

    }
}
