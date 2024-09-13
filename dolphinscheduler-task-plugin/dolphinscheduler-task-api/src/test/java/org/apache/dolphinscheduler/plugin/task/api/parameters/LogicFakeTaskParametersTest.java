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

package org.apache.dolphinscheduler.plugin.task.api.parameters;

import static com.google.common.truth.Truth.assertThat;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.junit.jupiter.api.Test;

class LogicFakeTaskParametersTest {

    @Test
    public void testFakeParamsToJson() {
        final LogicFakeTaskParameters logicFakeTaskParameters = new LogicFakeTaskParameters();
        logicFakeTaskParameters.setShellScript("console.log('hello world');");
        String jsonString = JSONUtils.toJsonString(logicFakeTaskParameters);
        assertThat(jsonString)
                .isEqualTo("{\"localParams\":null,\"varPool\":[],\"shellScript\":\"console.log('hello world');\"}");
    }

    @Test
    public void testJsonToFakeParams() {
        final String json = "{\"localParams\":null,\"varPool\":[],\"shellScript\":\"console.log('hello world');\"}";
        final LogicFakeTaskParameters logicFakeTaskParameters =
                JSONUtils.parseObject(json, LogicFakeTaskParameters.class);
        assertThat(logicFakeTaskParameters).isNotNull();

        assertThat(logicFakeTaskParameters.getShellScript()).isEqualTo("console.log('hello world');");
    }

}
