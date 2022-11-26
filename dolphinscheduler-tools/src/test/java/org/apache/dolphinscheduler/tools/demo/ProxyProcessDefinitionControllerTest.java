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

package org.apache.dolphinscheduler.tools.demo;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProxyProcessDefinitionControllerTest {

    private ProxyProcessDefinitionController controller = new ProxyProcessDefinitionController();

    @Test
    void createProcessDefinition() {
        try (MockedStatic<OkHttpUtils> mockOkHttpUtils = Mockito.mockStatic(OkHttpUtils.class)) {
            mockOkHttpUtils.when(() -> OkHttpUtils.demoPost(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap()))
                    .thenReturn(JSONUtils.toJsonString(ProxyResult.success("ok")));
            ProxyResult proxyResult = controller.createProcessDefinition(
                    "token",
                    1L,
                    "test",
                    "test",
                    "test",
                    "test",
                    1000,
                    "test",
                    "test",
                    "test",
                    ProcessExecutionTypeEnum.SERIAL_PRIORITY);
            Assertions.assertNotNull(proxyResult);
            Assertions.assertEquals("ok", proxyResult.getData());
        }
    }
}
