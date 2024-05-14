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

package org.apache.dolphinscheduler.plugin.task.api.loop.template.http.parser;

import org.apache.dolphinscheduler.plugin.task.api.loop.template.LoopTaskYamlDefinition;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpTaskDefinitionParserTest {

    private static final String yamlFile = HttpTaskDefinitionParserTest.class.getResource("/mock_loop_task.yaml")
            .getFile();

    @Test
    public void parseYamlConfigFile() throws IOException {
        LoopTaskYamlDefinition loopTaskYamlDefinition = new HttpTaskDefinitionParser().parseYamlConfigFile(yamlFile);
        // check not null
        Assertions.assertNotNull(loopTaskYamlDefinition);
        Assertions.assertNotNull(loopTaskYamlDefinition.getService());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getName());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getType());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getApi());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getApi().getSubmit());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getApi().getQueryState());
        Assertions.assertNotNull(loopTaskYamlDefinition.getService().getApi().getCancel());
        // check data consistency
        LoopTaskYamlDefinition.LoopTaskServiceYamlDefinition service = loopTaskYamlDefinition.getService();
        Assertions.assertEquals("MockService", service.getName());
        Assertions.assertEquals("Http", service.getType());
        Map<String, String> expectedHeaders = new HashMap<>();
        expectedHeaders.put("Content-Type", "text/html");
        expectedHeaders.put("Content-Length", "1234");
        Assertions.assertEquals("/api/v1/submit", service.getApi().getSubmit().getUrl());
        Assertions.assertEquals(expectedHeaders, service.getApi().getSubmit().getHttpHeaders());
    }

    @Test
    public void validateYamlDefinition() throws IOException {
        HttpTaskDefinitionParser httpTaskDefinitionParser = new HttpTaskDefinitionParser();
        LoopTaskYamlDefinition loopTaskYamlDefinition = httpTaskDefinitionParser.parseYamlConfigFile(yamlFile);
        httpTaskDefinitionParser.validateYamlDefinition(loopTaskYamlDefinition);
        // if no exception assert true
        Assertions.assertTrue(true);

    }
}
