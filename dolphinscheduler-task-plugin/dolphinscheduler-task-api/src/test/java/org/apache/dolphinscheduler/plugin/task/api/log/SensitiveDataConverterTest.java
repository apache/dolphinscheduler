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

package org.apache.dolphinscheduler.plugin.task.api.log;

import static org.apache.dolphinscheduler.common.constants.Constants.K8S_CONFIG_REGEX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

class SensitiveDataConverterTest {

    @AfterEach
    void tearDown() {
        SensitiveDataConverter.clearTaskSensitiveValues(101);
        SensitiveDataConverter.clearTaskSensitiveValues(102);
        SensitiveDataConverter.clearTaskSensitiveValues(201);
        SensitiveDataConverter.clearTaskSensitiveValues(202);
        MDC.remove(TaskConstants.TASK_INSTANCE_ID_MDC_KEY);
    }

    /**
     * mask sensitive logMsg - sql task datasource password
     */
    @Test
    void testPwdLogMsgConverter() {
        HashMap<String, String> tcs = new HashMap<>();
        tcs.put("{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\","
                + "\"database\":\"carbond\","
                + "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\","
                + "\"user\":\"view\","
                + "\"password\":\"view1\"}",

                "{\"address\":\"jdbc:mysql://192.168.xx.xx:3306\","
                        + "\"database\":\"carbond\","
                        + "\"jdbcUrl\":\"jdbc:mysql://192.168.xx.xx:3306/ods\","
                        + "\"user\":\"view\","
                        + "\"password\":\"******\"}");

        tcs.put("End initialize task {\n" +
                "  \"resourceParametersHelper\" : {\n" +
                "    \"resourceMap\" : {\n" +
                "      \"DATASOURCE\" : {\n" +
                "        \"1\" : {\n" +
                "          \"resourceType\" : \"DATASOURCE\",\n" +
                "          \"type\" : \"ORACLE\",\n" +
                "          \"connectionParams\" : \"{\\\"user\\\":\\\"user\\\",\\\"password\\\":\\\"view1\\\"}\",\n" +
                "          \"DATASOURCE\" : null\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}",

                "End initialize task {\n" +
                        "  \"resourceParametersHelper\" : {\n" +
                        "    \"resourceMap\" : {\n" +
                        "      \"DATASOURCE\" : {\n" +
                        "        \"1\" : {\n" +
                        "          \"resourceType\" : \"DATASOURCE\",\n" +
                        "          \"type\" : \"ORACLE\",\n" +
                        "          \"connectionParams\" : \"{\\\"user\\\":\\\"user\\\",\\\"password\\\":\\\"******\\\"}\",\n"
                        +
                        "          \"DATASOURCE\" : null\n" +
                        "        }\n" +
                        "      }\n" +
                        "    }\n" +
                        "  }\n" +
                        "}");

        for (String logMsg : tcs.keySet()) {
            String maskedLog = SensitiveDataConverter.maskSensitiveData(logMsg);
            assertEquals(tcs.get(logMsg), maskedLog);
        }
    }

    @Test
    void testPostJdbcInfoLogMsgConverter() {
        String POST_JDBC_INFO_REGEX = "(?<=(post jdbc info:)).*(?=)";
        SensitiveDataConverter.addMaskPattern(POST_JDBC_INFO_REGEX);
        String postJdbcInfoLogMsg = "post jdbc info:clickhouse,jdbc:clickhouse://127.0.0.1:8123/td_cdp,admin,123%@@56";
        final String maskedLog = SensitiveDataConverter.maskSensitiveData(postJdbcInfoLogMsg);
        String expectedMsg = "post jdbc info:******";
        assertEquals(expectedMsg, maskedLog);
    }

    @Test
    void testMaskSensitiveDataWithEmptyPassword() {
        String postJdbcInfoLogMsg =
                "MySQLConnectionParam{user='admin', password='', address='jdbc:mysql://localhost:3306', database='aa', jdbcUrl='jdbc:mysql://localhost:3306/aa', driverLocation='null', driverClassName='com.mysql.cj.jdbc.Driver', validationQuery='select 1', other='null'}";
        final String maskedLog = SensitiveDataConverter.maskSensitiveData(postJdbcInfoLogMsg);
        final String expectedMsg =
                "MySQLConnectionParam{user='admin', password='******', address='jdbc:mysql://localhost:3306', database='aa', jdbcUrl='jdbc:mysql://localhost:3306/aa', driverLocation='null', driverClassName='com.mysql.cj.jdbc.Driver', validationQuery='select 1', other='null'}";
        assertEquals(expectedMsg, maskedLog);
    }

    @Test
    void testK8SLogMsgConverter() {
        String msg = "End initialize task {\n" +
                "  \"taskName\" : \"echo\",\n" +
                "  \"k8sTaskExecutionContext\" : {\n" +
                "    \"configYaml\" : \"apiVersion: v1 xxx client-key-data: ==\",\n" +
                "    \"namespace\" : \"abc\"\n" +
                "  },\n" +
                "  \"logBufferEnable\" : false\n" +
                "}";
        String maskMsg = "End initialize task {\n" +
                "  \"taskName\" : \"echo\",\n" +
                "  \"k8sTaskExecutionContext\" : {\n" +
                "    \"configYaml\" : \"******\",\n" +
                "    \"namespace\" : \"abc\"\n" +
                "  },\n" +
                "  \"logBufferEnable\" : false\n" +
                "}";
        SensitiveDataConverter.addMaskPattern(K8S_CONFIG_REGEX);
        final String maskedLog = SensitiveDataConverter.maskSensitiveData(msg);

        assertEquals(maskMsg, maskedLog);
    }

    @Test
    void registerTaskSensitiveValuesMasksPlaintextInLog() {
        Map<String, Property> params = new HashMap<>();
        params.put("pwd", sensitive("pwd", "SuperSecret123"));
        params.put("plain", nonSensitive("plain", "hello"));
        SensitiveDataConverter.registerTaskSensitiveValues(101, params);

        String masked = SensitiveDataConverter.maskSensitiveData("echo SuperSecret123 and hello", 101);
        assertEquals("echo ****** and hello", masked);
        assertFalse(masked.contains("SuperSecret123"));
    }

    @Test
    void registerSkipsEmptyAndPlaceholder() {
        Map<String, Property> params = new HashMap<>();
        params.put("empty", sensitive("empty", ""));
        params.put("mask", sensitive("mask", TaskConstants.SENSITIVE_DATA_MASK));
        SensitiveDataConverter.registerTaskSensitiveValues(101, params);

        assertTrue(SensitiveDataConverter.getTaskSensitiveValues(101).isEmpty());
    }

    @Test
    void clearTaskSensitiveValuesStopsMasking() {
        SensitiveDataConverter.registerTaskSensitiveValues(101,
                Collections.singletonMap("pwd", sensitive("pwd", "token-xyz")));
        assertEquals("echo ******", SensitiveDataConverter.maskSensitiveData("echo token-xyz", 101));

        SensitiveDataConverter.clearTaskSensitiveValues(101);
        assertEquals("echo token-xyz", SensitiveDataConverter.maskSensitiveData("echo token-xyz", 101));
    }

    @Test
    void maskUsesTaskInstanceIdFromMdcWhenNotPassed() {
        SensitiveDataConverter.registerTaskSensitiveValues(101,
                Collections.singletonMap("pwd", sensitive("pwd", "mdc-secret")));
        MDC.put(TaskConstants.TASK_INSTANCE_ID_MDC_KEY, "101");
        try {
            assertEquals(" -> ******", SensitiveDataConverter.maskSensitiveData(" -> mdc-secret"));
        } finally {
            MDC.remove(TaskConstants.TASK_INSTANCE_ID_MDC_KEY);
        }
    }

    @Test
    void sequentialTasksDoNotPolluteEachOther() {
        SensitiveDataConverter.registerTaskSensitiveValues(101,
                Collections.singletonMap("pwd", sensitive("pwd", "secret-A")));
        assertEquals("A=******", SensitiveDataConverter.maskSensitiveData("A=secret-A", 101));

        SensitiveDataConverter.clearTaskSensitiveValues(101);
        SensitiveDataConverter.registerTaskSensitiveValues(102,
                Collections.singletonMap("pwd", sensitive("pwd", "secret-B")));

        assertEquals("A=secret-A", SensitiveDataConverter.maskSensitiveData("A=secret-A", 102));
        assertEquals("B=******", SensitiveDataConverter.maskSensitiveData("B=secret-B", 102));
        assertEquals("A=secret-A", SensitiveDataConverter.maskSensitiveData("A=secret-A", 101));
    }

    @Test
    void parallelTasksDoNotPolluteEachOther() throws Exception {
        SensitiveDataConverter.registerTaskSensitiveValues(201,
                Collections.singletonMap("pwd", sensitive("pwd", "parallel-A")));
        SensitiveDataConverter.registerTaskSensitiveValues(202,
                Collections.singletonMap("pwd", sensitive("pwd", "parallel-B")));

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch go = new CountDownLatch(1);
        AtomicReference<String> maskedA = new AtomicReference<>();
        AtomicReference<String> maskedB = new AtomicReference<>();
        try {
            pool.submit(() -> {
                ready.countDown();
                go.await(5, TimeUnit.SECONDS);
                maskedA.set(SensitiveDataConverter.maskSensitiveData("A=parallel-A B=parallel-B", 201));
                return null;
            });
            pool.submit(() -> {
                ready.countDown();
                go.await(5, TimeUnit.SECONDS);
                maskedB.set(SensitiveDataConverter.maskSensitiveData("A=parallel-A B=parallel-B", 202));
                return null;
            });
            assertTrue(ready.await(5, TimeUnit.SECONDS));
            go.countDown();
            pool.shutdown();
            assertTrue(pool.awaitTermination(5, TimeUnit.SECONDS));
        } finally {
            pool.shutdownNow();
        }

        assertEquals("A=****** B=parallel-B", maskedA.get());
        assertEquals("A=parallel-A B=******", maskedB.get());
    }

    private static Property sensitive(String prop, String value) {
        return Property.builder()
                .prop(prop)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(true)
                .build();
    }

    private static Property nonSensitive(String prop, String value) {
        return Property.builder()
                .prop(prop)
                .direct(Direct.IN)
                .type(DataType.VARCHAR)
                .value(value)
                .sensitive(false)
                .build();
    }
}
