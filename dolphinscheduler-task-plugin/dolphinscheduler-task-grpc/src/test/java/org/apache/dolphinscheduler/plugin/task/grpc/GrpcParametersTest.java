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

package org.apache.dolphinscheduler.plugin.task.grpc;

import org.apache.dolphinscheduler.common.utils.JSONUtils;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test GrpcTask
 */
public class GrpcParametersTest {

    @Test
    void testGenerator() throws IOException {
        String paramData = "{" +
                "\"localParams\":[]," +
                "\"url\":\"127.0.0.1:50010\"," +
                "\"grpcServiceDefinition\":\"\"," +
                "\"grpcServiceDefinitionJSON\":\"" + toJSONEscaped(GrpcTaskTest.readResourceTextFile("taskTester.json"))
                + "\"," +
                "\"methodName\":\"TaskTester/TestOK\"," +
                "\"message\":\"{ \\\"username\\\":\\\"test username\\\" }\"," +
                "\"grpcCheckCondition\":\"STATUS_CODE_DEFAULT\"," +
                "\"condition\":\"\"," +
                "\"grpcConnectTimeoutMs\":\"10000\"" +
                "}";
        GrpcParameters grpcParameters = JSONUtils.parseObject(paramData, GrpcParameters.class);
        Assertions.assertEquals(10000, grpcParameters.getConnectTimeoutMs());
        Assertions.assertEquals("127.0.0.1:50010", grpcParameters.getUrl());
        Assertions.assertEquals("TaskTester/TestOK", grpcParameters.getMethodName());
        Assertions.assertEquals("{ \"username\":\"test username\" }", grpcParameters.getMessage());
        Assertions.assertEquals(GrpcCheckCondition.STATUS_CODE_DEFAULT, grpcParameters.getGrpcCheckCondition());
        Assertions.assertEquals("", grpcParameters.getCondition());
    }

    @Test
    void testCheckParameters() throws IOException {
        String paramData = "{" +
                "\"localParams\":[]," +
                "\"url\":\"127.0.0.1:50010\"," +
                "\"grpcServiceDefinition\":\"\"," +
                "\"grpcServiceDefinitionJSON\":\"" + toJSONEscaped(GrpcTaskTest.readResourceTextFile("taskTester.json"))
                + "\"," +
                "\"methodName\":\"TaskTester/TestOK\"," +
                "\"message\":\"{ \\\"username\\\":\\\"test username\\\" }\"," +
                "\"grpcCheckCondition\":\"STATUS_CODE_DEFAULT\"," +
                "\"condition\":\"\"," +
                "\"grpcConnectTimeoutMs\":\"10000\"" +
                "}";
        GrpcParameters grpcParameters = JSONUtils.parseObject(paramData, GrpcParameters.class);
        Assertions.assertTrue(grpcParameters.checkParameters());
        Assertions.assertEquals(10000, grpcParameters.getConnectTimeoutMs());
        Assertions.assertEquals("127.0.0.1:50010", grpcParameters.getUrl());
        Assertions.assertEquals("TaskTester/TestOK", grpcParameters.getMethodName());
        Assertions.assertEquals("{ \"username\":\"test username\" }", grpcParameters.getMessage());
        Assertions.assertEquals(GrpcCheckCondition.STATUS_CODE_DEFAULT, grpcParameters.getGrpcCheckCondition());
        Assertions.assertEquals("", grpcParameters.getCondition());
    }

    @Test
    void testCheckValues() throws IOException {
        String paramData = "{" +
                "\"localParams\":[]," +
                "\"url\":\"127.0.0.1:50010\"," +
                "\"grpcServiceDefinition\":\"\"," +
                "\"grpcServiceDefinitionJSON\":\"" + toJSONEscaped(GrpcTaskTest.readResourceTextFile("taskTester.json"))
                + "\"," +
                "\"methodName\":\"TaskTester/TestOK\"," +
                "\"message\":\"{ \\\"username\\\":\\\"test username\\\" }\"," +
                "\"grpcCheckCondition\":\"STATUS_CODE_DEFAULT\"," +
                "\"condition\":\"\"," +
                "\"grpcConnectTimeoutMs\":\"10000\"" +
                "}";
        GrpcParameters grpcParameters = JSONUtils.parseObject(paramData, GrpcParameters.class);
        Assertions.assertEquals(10000, grpcParameters.getConnectTimeoutMs());
        Assertions.assertEquals("127.0.0.1:50010", grpcParameters.getUrl());
        Assertions.assertEquals("TaskTester/TestOK", grpcParameters.getMethodName());
        Assertions.assertEquals("{ \"username\":\"test username\" }", grpcParameters.getMessage());
        Assertions.assertEquals(GrpcCheckCondition.STATUS_CODE_DEFAULT, grpcParameters.getGrpcCheckCondition());
        Assertions.assertEquals("", grpcParameters.getCondition());
        Assertions.assertEquals(0, grpcParameters.getLocalParametersMap().size());
        Assertions.assertEquals(0, grpcParameters.getResourceFilesList().size());
    }

    public static String toJSONEscaped(String input) {
        return StringEscapeUtils.escapeEcmaScript(input);
    }
}
