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

package org.apache.dolphinscheduler.plugin.task.api.parameters.resource;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.SqlParameters;
import org.apache.dolphinscheduler.spi.enums.DbType;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AbstractResourceParametersTest {

    @Test
    public void testDataSource() {
        TaskExecutionContext taskExecutionContext = new TaskExecutionContext();
        String taskParam =
                "{\"localParams\":[],\"resourceList\":[],\"type\":\"MYSQL\",\"datasource\":\"1\",\"sql\":\"select now();\",\"sqlType\":\"0\",\"preStatements\":[],\"postStatements\":[],\"conditionResult\":\"null\",\"dependence\":\"null\",\"switchResult\":\"null\",\"waitStartTimeout\":null}";

        ResourceParametersHelper resourceParametersHelper =
                JSONUtils.parseObject(taskParam, SqlParameters.class).getResources();

        resourceParametersHelper.getResourceMap().forEach((type, map) -> {
            map.forEach((code, parameters) -> {
                DataSourceParameters dataSourceParameters = new DataSourceParameters();
                dataSourceParameters.setType(DbType.MYSQL);
                dataSourceParameters.setConnectionParams("127.0.0.1:3306");
                map.put(code, dataSourceParameters);
            });
        });

        taskExecutionContext.setResourceParametersHelper(resourceParametersHelper);

        String json = JSONUtils.toJsonString(taskExecutionContext);

        taskExecutionContext = JSONUtils.parseObject(json, TaskExecutionContext.class);

        Assertions.assertNotNull(taskExecutionContext);
    }
}
