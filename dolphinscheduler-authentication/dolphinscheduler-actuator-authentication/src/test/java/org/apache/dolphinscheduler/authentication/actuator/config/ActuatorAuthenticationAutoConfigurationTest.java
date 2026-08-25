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

package org.apache.dolphinscheduler.authentication.actuator.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ActuatorAuthenticationAutoConfigurationTest {

    private static final String CONTEXT_PATH = "/dolphinscheduler";
    private static final String USERNAME = "actuator-user";
    private static final String PASSWORD = "actuator-password";

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void shouldAuthenticateEquivalentActuatorPaths() {
        try (ConfigurableApplicationContext context = startApplication(true)) {
            assertThat(get(context, "/actuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(get(context, "/%61ctuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(get(context, "/%61ctuator/prometheus", USERNAME, "wrong-password").getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(get(context, "/%61ctuator/prometheus", USERNAME, PASSWORD).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(get(context, "/actuator/health", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(get(context, "/actuator/%68ealth", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void shouldAuthenticateActuatorPathsWithoutContextPath() {
        try (ConfigurableApplicationContext context = startApplication(true, "")) {
            assertThat(get(context, "/actuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(get(context, "/%61ctuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(get(context, "/%61ctuator/prometheus", USERNAME, PASSWORD).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    @Test
    void shouldPermitActuatorEndpointsWhenAuthenticationIsDisabled() {
        try (ConfigurableApplicationContext context = startApplication(false)) {
            assertThat(get(context, "/actuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
            assertThat(get(context, "/%61ctuator/prometheus", null, null).getStatusCode())
                    .isEqualTo(HttpStatus.OK);
        }
    }

    private ConfigurableApplicationContext startApplication(boolean securityEnabled) {
        return startApplication(securityEnabled, CONTEXT_PATH);
    }

    private ConfigurableApplicationContext startApplication(boolean securityEnabled, String contextPath) {
        SpringApplication application = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.SERVLET)
                .properties(
                        "server.port=0",
                        "server.servlet.context-path=" + contextPath,
                        "management.security.enabled=" + securityEnabled,
                        "management.security.username=" + USERNAME,
                        "management.security.password=" + PASSWORD,
                        "management.security.exclude=health,metrics",
                        "management.endpoints.web.exposure.include=health,metrics,prometheus",
                        "spring.main.banner-mode=off")
                .build();
        return application.run();
    }

    private ResponseEntity<String> get(
                                       ConfigurableApplicationContext context,
                                       String path,
                                       String username,
                                       String password) {
        ServletWebServerApplicationContext webServerApplicationContext =
                (ServletWebServerApplicationContext) context;
        int port = webServerApplicationContext.getWebServer().getPort();
        String contextPath = webServerApplicationContext.getServletContext().getContextPath();
        URI uri = URI.create("http://127.0.0.1:" + port + contextPath + path);
        HttpHeaders headers = new HttpHeaders();
        if (username != null) {
            headers.setBasicAuth(username, password);
        }
        return restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    static class TestApplication {
    }
}
