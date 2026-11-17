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

    /**
     * mask sensitive logMsg - S3/OSS access-key and secret-key, see issue #17398.
     * Covers the key names used in the codebase: "access-key", "secret-key",
     * "accessKey", "secretKey", "accessKeyId", "accessKeySecret",
     * "access.key.id", "access.key.secret" and "secret.access.key".
     */
    @Test
    void testS3OssAccessKeyLogMsgConverter() {
        HashMap<String, String> tcs = new HashMap<>();
        // JSON-like, compact: {"access-key":"...","secret-key":"..."}
        tcs.put("{\"access-key\":\"AKIAIOSFODNN7EXAMPLE\","
                + "\"secret-key\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"}",
                "{\"access-key\":\"******\","
                        + "\"secret-key\":\"******\"}");
        // JSON-like, compact: accessKey / secretKey, other fields are kept
        tcs.put("{\"accessKey\":\"AKIAIOSFODNN7EXAMPLE\","
                + "\"secretKey\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\","
                + "\"bucket\":\"my-bucket\"}",
                "{\"accessKey\":\"******\","
                        + "\"secretKey\":\"******\","
                        + "\"bucket\":\"my-bucket\"}");
        // JSON-like, compact: accessKeyId / accessKeySecret
        tcs.put("{\"accessKeyId\":\"AKIAIOSFODNN7EXAMPLE\","
                + "\"accessKeySecret\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"}",
                "{\"accessKeyId\":\"******\","
                        + "\"accessKeySecret\":\"******\"}");
        // JSON-like, compact: dotted keys used by AWS/S3 configuration
        tcs.put("{\"access.key.id\":\"AKIAIOSFODNN7EXAMPLE\","
                + "\"access.key.secret\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"}",
                "{\"access.key.id\":\"******\","
                        + "\"access.key.secret\":\"******\"}");
        // JSON-like, compact: secret.access.key used by AwsConfigurationKeys
        tcs.put("{\"secret.access.key\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"}",
                "{\"secret.access.key\":\"******\"}");
        // JSON-like, compact: key names are matched case-insensitively
        tcs.put("{\"ACCESS-KEY\":\"AKIAIOSFODNN7EXAMPLE\",\"Secret-Key\":\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"}",
                "{\"ACCESS-KEY\":\"******\",\"Secret-Key\":\"******\"}");
        // JSON-like, pretty printed, as logged by DinkyTask
        tcs.put("Initialize dinky task params: {\n"
                + "  \"address\" : \"http://localhost:8888\",\n"
                + "  \"access-key\" : \"AKIAIOSFODNN7EXAMPLE\",\n"
                + "  \"secret-key\" : \"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\"\n"
                + "}",
                "Initialize dinky task params: {\n"
                        + "  \"address\" : \"http://localhost:8888\",\n"
                        + "  \"access-key\" : \"******\",\n"
                        + "  \"secret-key\" : \"******\"\n"
                        + "}");
        // JSON-like, escaped nested JSON, as in connectionParams
        tcs.put("End initialize task {\n"
                + "  \"connectionParams\" : \"{\\\"accessKeyId\\\":\\\"AKIAIOSFODNN7EXAMPLE\\\",\\\"accessKeySecret\\\":\\\"wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\\\"}\",\n"
                + "  \"type\" : \"DINKY\"\n"
                + "}",
                "End initialize task {\n"
                        + "  \"connectionParams\" : \"{\\\"accessKeyId\\\":\\\"******\\\",\\\"accessKeySecret\\\":\\\"******\\\"}\",\n"
                        + "  \"type\" : \"DINKY\"\n"
                        + "}");
        // Java toString() style with quoted values
        tcs.put("S3ConnectionParam{accessKey='AKIAIOSFODNN7EXAMPLE', secretKey='wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY'}",
                "S3ConnectionParam{accessKey='******', secretKey='******'}");
        // Java toString() style without quotes, e.g. Lombok @Data OssConnection;
        // the endpoint must NOT be masked
        tcs.put("OssConnection(accessKeyId=AKIAIOSFODNN7EXAMPLE, accessKeySecret=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY, endPoint=http://oss-cn-hangzhou.aliyuncs.com)",
                "OssConnection(accessKeyId=******, accessKeySecret=******, endPoint=http://oss-cn-hangzhou.aliyuncs.com)");
        // Java toString() style without quotes, sensitive key is the last field
        // before the closing parenthesis
        tcs.put("OssConnection(accessKeyId=AKIAIOSFODNN7EXAMPLE, accessKeySecret=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY)",
                "OssConnection(accessKeyId=******, accessKeySecret=******)");
        // key=value / properties-like output, e.g. MinIO configuration
        tcs.put("access.key.id=AKIAIOSFODNN7EXAMPLE\n"
                + "secret.access.key=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY\n"
                + "endpoint=http://localhost:9000",
                "access.key.id=******\n"
                        + "secret.access.key=******\n"
                        + "endpoint=http://localhost:9000");
        // key=value with dotted prefixes, e.g. remote logging OSS configuration
        tcs.put("remote.logging.oss.access.key.id=AKIAIOSFODNN7EXAMPLE\n"
                + "remote.logging.oss.access.key.secret=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "remote.logging.oss.access.key.id=******\n"
                        + "remote.logging.oss.access.key.secret=******");
        // key=value, access.key.secret used by the S3 storage plugin
        tcs.put("access.key.secret=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY",
                "access.key.secret=******");
        // empty values are masked as well, like an empty password
        tcs.put("accessKeyId=, accessKeySecret=",
                "accessKeyId=******, accessKeySecret=******");
        // no key/value separator after the key name, nothing is masked
        tcs.put("failed to load access-key from config, check endpoint=http://localhost:9000",
                "failed to load access-key from config, check endpoint=http://localhost:9000");
        tcs.put("endpoint=http://oss-cn-hangzhou.aliyuncs.com, bucket=my-bucket",
                "endpoint=http://oss-cn-hangzhou.aliyuncs.com, bucket=my-bucket");

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
