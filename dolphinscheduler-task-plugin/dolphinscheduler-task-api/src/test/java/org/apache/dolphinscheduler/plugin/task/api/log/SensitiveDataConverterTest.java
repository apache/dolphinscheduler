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

import java.util.HashMap;

import org.junit.jupiter.api.Test;

class SensitiveDataConverterTest {

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
}
