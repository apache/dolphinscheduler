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

package org.apache.dolphinscheduler.dao.crypto;

import static org.apache.dolphinscheduler.plugin.datasource.api.constants.DataSourceConstants.DATASOURCE_ENCRYPTION_ENABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mockStatic;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.TaskDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinition;
import org.apache.dolphinscheduler.dao.entity.WorkflowInstance;
import org.apache.dolphinscheduler.plugin.datasource.api.utils.PasswordUtils;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.utils.GlobalParameterUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SensitiveDefinitionParamInterceptorTest {

    private final SensitiveDefinitionParamInterceptor interceptor = new SensitiveDefinitionParamInterceptor();

    @Test
    void encryptThenDecryptRestoresWorkflowDefinitionPlaintext() {
        withEncryptionEnabled(() -> {
            WorkflowDefinition definition = new WorkflowDefinition();
            definition.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                    Arrays.asList(sensitive("pwd", "secret-1"))));

            interceptor.apply(definition, true);
            assertEquals(PasswordUtils.encodePassword("secret-1"),
                    GlobalParameterUtils.deserializeGlobalParameter(definition.getGlobalParams()).get(0).getValue());

            interceptor.apply(definition, false);
            assertEquals("secret-1",
                    GlobalParameterUtils.deserializeGlobalParameter(definition.getGlobalParams()).get(0).getValue());
        });
    }

    @Test
    void encryptThenDecryptRestoresTaskDefinitionPlaintext() {
        withEncryptionEnabled(() -> {
            TaskDefinition definition = new TaskDefinition();
            definition.setTaskParams("{\"localParams\":"
                    + GlobalParameterUtils.serializeGlobalParameter(Arrays.asList(sensitive("token", "t-secret")))
                    + "}");

            interceptor.apply(definition, true);
            String encrypted = localValue(definition.getTaskParams());
            assertEquals(PasswordUtils.encodePassword("t-secret"), encrypted);

            interceptor.apply(definition, false);
            assertEquals("t-secret", localValue(definition.getTaskParams()));
        });
    }

    @Test
    void doesNotTouchInstanceGlobalParams() {
        withEncryptionEnabled(() -> {
            WorkflowInstance instance = new WorkflowInstance();
            String plaintext = GlobalParameterUtils.serializeGlobalParameter(
                    Arrays.asList(sensitive("pwd", "secret-1")));
            instance.setGlobalParams(plaintext);

            interceptor.apply(instance, true);
            assertEquals(plaintext, instance.getGlobalParams());
        });
    }

    @Test
    void paramMapAliasesDoNotDoubleEncrypt() {
        withEncryptionEnabled(() -> {
            WorkflowDefinition definition = new WorkflowDefinition();
            definition.setGlobalParams(GlobalParameterUtils.serializeGlobalParameter(
                    Arrays.asList(sensitive("pwd", "secret-1"))));
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("et", definition);
            parameter.put("param1", definition);

            interceptor.apply(parameter, true);
            String encoded = GlobalParameterUtils.deserializeGlobalParameter(definition.getGlobalParams())
                    .get(0).getValue();
            assertEquals(PasswordUtils.encodePassword("secret-1"), encoded);
            assertNotEquals(PasswordUtils.encodePassword(encoded), encoded);

            interceptor.apply(parameter, false);
            assertEquals("secret-1",
                    GlobalParameterUtils.deserializeGlobalParameter(definition.getGlobalParams()).get(0).getValue());
        });
    }

    private static String localValue(String taskParams) {
        List<Property> properties = JSONUtils.toList(
                JSONUtils.parseObject(taskParams).get("localParams").toString(),
                Property.class);
        return properties.get(0).getValue();
    }

    private static void withEncryptionEnabled(Runnable action) {
        try (
                MockedStatic<PropertyUtils> propertyUtils =
                        mockStatic(PropertyUtils.class, Mockito.CALLS_REAL_METHODS)) {
            propertyUtils.when(() -> PropertyUtils.getBoolean(DATASOURCE_ENCRYPTION_ENABLE, false))
                    .thenReturn(true);
            action.run();
        }
    }

    private static Property sensitive(String name, String value) {
        return Property.builder()
                .prop(name)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(true)
                .build();
    }
}
