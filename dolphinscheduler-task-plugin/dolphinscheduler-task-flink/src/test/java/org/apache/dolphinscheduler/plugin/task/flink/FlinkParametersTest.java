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

package org.apache.dolphinscheduler.plugin.task.flink;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class FlinkParametersTest {

    @Test
    public void getResourceFilesList() {
        FlinkParameters flinkParameters = new FlinkParameters();
        Assertions.assertTrue(flinkParameters.getResourceFilesList().isEmpty());

        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setResourceName("/testFlinkMain-1.0.0-SNAPSHOT.jar");
        flinkParameters.setMainJar(mainResource);

        List<ResourceInfo> resourceInfos = new LinkedList<>();
        ResourceInfo resourceInfo1 = new ResourceInfo();
        resourceInfo1.setResourceName("/testFlinkParameters1.jar");
        resourceInfos.add(resourceInfo1);

        flinkParameters.setResourceList(resourceInfos);
        List<ResourceInfo> resourceFilesList = flinkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(2, resourceFilesList.size());

        ResourceInfo resourceInfo2 = new ResourceInfo();
        resourceInfo2.setResourceName("/testFlinkParameters2.jar");
        resourceInfos.add(resourceInfo2);

        flinkParameters.setResourceList(resourceInfos);
        resourceFilesList = flinkParameters.getResourceFilesList();
        Assertions.assertNotNull(resourceFilesList);
        Assertions.assertEquals(3, resourceFilesList.size());
    }

    @Test
    public void checkParametersShouldKeepClientPathWhenSqlSubmitTypeIsAbsent() {
        FlinkParameters parameters = new FlinkParameters();
        parameters.setProgramType(ProgramType.SQL);
        parameters.setRawScript("SELECT 1");
        Assertions.assertTrue(parameters.checkParameters());
        Assertions.assertFalse(parameters.isSqlGateway());
    }

    @Test
    public void checkParametersShouldFailWhenSqlGatewayJdbcUrlIsBlank() {
        FlinkParameters parameters = gatewayFileParameters();
        parameters.setFlinkJdbcUrl(" ");
        Assertions.assertFalse(parameters.checkParameters());
    }

    @Test
    public void checkParametersShouldPassWhenSqlGatewayInlineScriptIsPresent() {
        FlinkParameters parameters = new FlinkParameters();
        parameters.setSqlSubmitType(FlinkSqlSubmitType.SQL_GATEWAY);
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType.SCRIPT);
        parameters.setRawScript("SELECT 1");
        parameters.setInitScriptType(org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType.SCRIPT);
        Assertions.assertTrue(parameters.checkParameters());
        Assertions.assertEquals(0, parameters.getResourceFilesList().size());
    }

    @Test
    public void getResourceFilesListShouldReturnGatewayScriptFilesOnly() {
        FlinkParameters parameters = gatewayFileParameters();
        Assertions.assertEquals(2, parameters.getResourceFilesList().size());
    }

    @Test
    public void toJdbcPropertiesShouldFilterNullKeysAndValues() {
        FlinkParameters parameters = new FlinkParameters();
        Assertions.assertEquals(0, parameters.toJdbcProperties().size());

        java.util.Map<String, String> jdbcProperties = new java.util.HashMap<>();
        jdbcProperties.put("user", "flink");
        jdbcProperties.put("badKey", null);
        jdbcProperties.put(null, "badValue");
        parameters.setJdbcProperties(jdbcProperties);
        java.util.Properties properties = parameters.toJdbcProperties();
        Assertions.assertEquals(1, properties.size());
        Assertions.assertEquals("flink", properties.getProperty("user"));
    }

    private FlinkParameters gatewayFileParameters() {
        FlinkParameters parameters = new FlinkParameters();
        parameters.setSqlSubmitType(FlinkSqlSubmitType.SQL_GATEWAY);
        parameters.setFlinkJdbcUrl("jdbc:flink://localhost:8083");
        parameters.setRawScriptType(org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType.FILE);
        ResourceInfo mainResource = new ResourceInfo();
        mainResource.setResourceName("main.sql");
        parameters.setResourceList(java.util.Collections.singletonList(mainResource));
        parameters.setInitScriptType(org.apache.dolphinscheduler.plugin.task.api.enums.SqlSourceType.FILE);
        ResourceInfo initResource = new ResourceInfo();
        initResource.setResourceName("init.sql");
        parameters.setInitScriptResourceList(java.util.Collections.singletonList(initResource));
        return parameters;
    }
}
