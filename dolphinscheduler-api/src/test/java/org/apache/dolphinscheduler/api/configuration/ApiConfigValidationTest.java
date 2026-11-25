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

package org.apache.dolphinscheduler.api.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.util.TestPropertyValues;

class ApiConfigValidationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ConfigurationPropertiesAutoConfiguration.class))
            .withUserConfiguration(ApiConfig.class);

    @Test
    void shouldNotRequireTokenWhenPythonGatewayIsDisabled() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context.getBean(ApiConfig.class).getPythonGateway().getAuthToken()).isNull();
        });
    }

    @Test
    void shouldRejectMissingTokenWhenPythonGatewayIsEnabled() {
        contextRunner.withPropertyValues("api.python-gateway.enabled=true").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasStackTraceContaining("pythonGateway.auth-token");
        });
    }

    @Test
    void shouldRejectEmptyTokenWhenPythonGatewayIsEnabled() {
        contextRunner.withPropertyValues("api.python-gateway.enabled=true", "api.python-gateway.auth-token=")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("pythonGateway.auth-token");
                });
    }

    @Test
    void shouldRejectBlankTokenWhenPythonGatewayIsEnabled() {
        contextRunner.withPropertyValues("api.python-gateway.enabled=true")
                .withInitializer(context -> TestPropertyValues.of(Collections.singletonMap(
                        "api.python-gateway.auth-token", " \t ")).applyTo(context))
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure()).hasStackTraceContaining("pythonGateway.auth-token");
                });
    }

    @Test
    void shouldAcceptExplicitTokenWhenPythonGatewayIsEnabled() {
        contextRunner
                .withPropertyValues("api.python-gateway.enabled=true", "api.python-gateway.auth-token=gateway-token")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(ApiConfig.class).getPythonGateway().getAuthToken())
                            .isEqualTo("gateway-token");
                });
    }
}
