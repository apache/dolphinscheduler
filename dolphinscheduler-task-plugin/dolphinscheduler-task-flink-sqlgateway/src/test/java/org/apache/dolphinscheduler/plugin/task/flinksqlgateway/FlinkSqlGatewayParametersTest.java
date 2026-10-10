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

package org.apache.dolphinscheduler.plugin.task.flinksqlgateway;

import org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType;
import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.Collections;
import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkSqlGatewayParametersTest {

    private FlinkSqlGatewayParameters buildValidFileBasedParameters() {
        FlinkSqlGatewayParameters parameters = new FlinkSqlGatewayParameters();
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(SqlSourceType.FILE);
        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setResourceName("main.sql");
        parameters.setResourceList(Collections.singletonList(mainResource));
        parameters.setInitScriptType(SqlSourceType.FILE);
        ResourceInfo initResource = new ResourceInfo();
        initResource.setResourceName("init.sql");
        parameters.setInitScriptResourceList(Collections.singletonList(initResource));
        return parameters;
    }

    @Test
    public void checkParametersShouldPassWhenFileBasedScriptsAreValid() {
        FlinkSqlGatewayParameters parameters = buildValidFileBasedParameters();
        Assertions.assertTrue(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldFailWhenJdbcUrlIsBlank() {
        FlinkSqlGatewayParameters parameters = buildValidFileBasedParameters();
        parameters.setFlinkJdbcUrl(" ");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldFailWhenMainScriptFileIsMissing() {
        FlinkSqlGatewayParameters parameters = buildValidFileBasedParameters();
        parameters.setResourceList(null);
        Assertions.assertFalse(parameters.checkParameters());

        parameters.setResourceList(Collections.emptyList());
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldFailWhenInitScriptFileIsMissing() {
        FlinkSqlGatewayParameters parameters = buildValidFileBasedParameters();
        parameters.setInitScriptResourceList(null);
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldPassWhenInlineScriptsAreValid() {
        FlinkSqlGatewayParameters parameters = new FlinkSqlGatewayParameters();
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(SqlSourceType.SCRIPT);
        parameters.setRawScript("SELECT 1");
        parameters.setInitScriptType(SqlSourceType.SCRIPT);
        Assertions.assertTrue(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldFailWhenInlineMainScriptIsBlank() {
        FlinkSqlGatewayParameters parameters = new FlinkSqlGatewayParameters();
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(SqlSourceType.SCRIPT);
        parameters.setRawScript("");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    public void getResourceFilesListShouldAggregateInitAndMainResourceFiles() {
        FlinkSqlGatewayParameters parameters = buildValidFileBasedParameters();
        Assertions.assertEquals(2, parameters.getResourceFilesList().size());
    }

    @Test
    public void getResourceFilesListShouldReturnEmptyWhenScriptsAreInline() {
        FlinkSqlGatewayParameters parameters = new FlinkSqlGatewayParameters();
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(SqlSourceType.SCRIPT);
        parameters.setInitScriptType(SqlSourceType.SCRIPT);
        Assertions.assertEquals(0, parameters.getResourceFilesList().size());
    }

    @Test
    public void toJdbcPropertiesShouldFilterNullKeysAndValues() {
        FlinkSqlGatewayParameters parameters = new FlinkSqlGatewayParameters();
        parameters.setJdbcProperties(null);
        Assertions.assertEquals(0, parameters.toJdbcProperties().size());

        parameters.setJdbcProperties(new java.util.HashMap<String, String>() {

            {
                put("user", "flink");
                put("badKey", null);
                put(null, "badValue");
            }
        });
        Properties properties = parameters.toJdbcProperties();
        Assertions.assertEquals(1, properties.size());
        Assertions.assertEquals("flink", properties.getProperty("user"));
    }
}
