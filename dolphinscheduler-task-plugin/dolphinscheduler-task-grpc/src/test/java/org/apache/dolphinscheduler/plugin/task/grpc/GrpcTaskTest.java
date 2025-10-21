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

import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_FAILURE;
import static org.apache.dolphinscheduler.plugin.task.api.TaskConstants.EXIT_CODE_SUCCESS;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.TaskTesterGrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;

/**
 * Test GrpcTask
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class GrpcTaskTest {

    private TaskTesterGrpc.TaskTesterImplBase serviceImpl =
            mock(TaskTesterGrpc.TaskTesterImplBase.class, delegatesTo(
                    new TaskTesterGrpc.TaskTesterImplBase() {

                        @Override
                        public void testOK(StringRequest request, StreamObserver<StringReply> respObserver) {
                            StringReply reply =
                                    StringReply.newBuilder().setMessage("test reply: " + request.getUsername()).build();
                            respObserver.onNext(reply);
                            respObserver.onCompleted();
                        }

                        @Override
                        public void testUNIMPLEMENTED(StringRequest request, StreamObserver<StringReply> respObserver) {
                            respObserver.onError(new StatusRuntimeException(Status.UNIMPLEMENTED));
                            respObserver.onCompleted();
                        }
                    }));

    private ExecutorService executor = Executors.newFixedThreadPool(2);
    private Server server = Grpc.newServerBuilderForPort(0, InsecureServerCredentials.create())
            .executor(executor)
            .addService(serviceImpl)
            .build();
    private int serverPort = 0;

    @BeforeEach
    void setUp() throws Exception {
        executor = Executors.newFixedThreadPool(2);
        server = Grpc.newServerBuilderForPort(0, InsecureServerCredentials.create())
                .executor(executor)
                .addService(serviceImpl)
                .build();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (server != null) {
                    server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                server.shutdownNow();
                e.printStackTrace(System.err);
            } finally {
                executor.shutdown();
            }
        }));
        server.start();
        serverPort = server.getPort();
    }

    @AfterEach
    void after() {
        if (server != null && !server.isShutdown()) {
            server.shutdownNow();
        }
        if (!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Test
    void testHandleStatusCodeDefaultOK() throws Exception {
        GrpcTask grpcTaskOK = generateGrpcTask("TaskTester/TestOK", "{\"username\":\"test username\"}",
                GrpcCheckCondition.STATUS_CODE_DEFAULT, "OK");
        GrpcTask grpcTaskMismatchedStatus = generateGrpcTask("TaskTester/TestUNIMPLEMENTED",
                "{\"username\":\"test username\"}", GrpcCheckCondition.STATUS_CODE_DEFAULT, "OK");
        grpcTaskOK.handle(null);
        grpcTaskMismatchedStatus.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, grpcTaskOK.getExitStatusCode());
        Assertions.assertEquals(EXIT_CODE_FAILURE, grpcTaskMismatchedStatus.getExitStatusCode());
    }

    @Test
    void testHandleStatusCodeCustom() throws Exception {
        GrpcTask grpcTaskUnimplemented = generateGrpcTask("TaskTester/TestUNIMPLEMENTED",
                "{\"username\":\"test username\"}", GrpcCheckCondition.STATUS_CODE_CUSTOM, "UNIMPLEMENTED");
        grpcTaskUnimplemented.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, grpcTaskUnimplemented.getExitStatusCode());
    }

    @Test
    void testAddDefaultOutput() throws Exception {
        GrpcTask grpcTask = generateGrpcTask("TaskTester/TestOK", "{\"username\":\"test username\"}",
                GrpcCheckCondition.STATUS_CODE_DEFAULT, "OK");
        String response = "{\"message\":\"test reply: test username\"}";
        AbstractParameters grpcParameters = grpcTask.getParameters();
        grpcTask.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, grpcTask.getExitStatusCode());
        List<Property> varPool = grpcParameters.getVarPool();
        Assertions.assertEquals(1, varPool.size());
        Property property = varPool.get(0);
        Assertions.assertEquals("null.response", property.getProp());
        Assertions.assertEquals(Direct.OUT, property.getDirect());
        Assertions.assertEquals(DataType.VARCHAR, property.getType());
        Assertions.assertEquals(response, property.getValue());
    }

    private GrpcTask generateGrpcTask(String methodName, String requestMessage,
                                      GrpcCheckCondition grpcCheckCondition, String condition) throws IOException {
        String paramData =
                generateGrpcParameters("127.0.0.1:" + serverPort, methodName, requestMessage, grpcCheckCondition,
                        condition);
        return generateGrpcTaskFromParamData(paramData, null);
    }

    private GrpcTask generateGrpcTaskFromParamData(String paramData, Map<String, String> prepareParamsMap) {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        Mockito.when(taskExecutionContext.getTaskParams()).thenReturn(paramData);
        if (prepareParamsMap != null) {
            Map<String, Property> propertyParamsMap = new HashMap<>();
            prepareParamsMap.forEach((k, v) -> {
                Property property = new Property();
                property.setProp(k);
                property.setValue(v);
                propertyParamsMap.put(k, property);
            });
            Mockito.when(taskExecutionContext.getPrepareParamsMap()).thenReturn(propertyParamsMap);
        }
        GrpcTask grpcTask = new GrpcTask(taskExecutionContext);
        grpcTask.init();
        return grpcTask;
    }

    private String generateGrpcParameters(String url, String methodName, String requestMessage,
                                          GrpcCheckCondition grpcCheckCondition, String condition) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        GrpcParameters grpcParameters = new GrpcParameters();
        grpcParameters.setUrl(url);
        // read definition from resources/taskTester.json
        grpcParameters.setGrpcServiceDefinitionJSON(readResourceTextFile("taskTester.json"));
        grpcParameters.setMethodName(methodName);
        grpcParameters.setMessage(requestMessage);
        grpcParameters.setGrpcCheckCondition(grpcCheckCondition);
        grpcParameters.setCondition(condition);
        grpcParameters.setConnectTimeoutMs(10000);
        return mapper.writeValueAsString(grpcParameters);
    }

    public static String readResourceTextFile(String pathInResource) throws IOException {
        ClassPathResource resource = new ClassPathResource(pathInResource);
        InputStream inputStream = resource.getInputStream();
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n"); // Append newline for line-by-line reading
            }
        }
        if (resultStringBuilder.length() > 0 && resultStringBuilder.charAt(resultStringBuilder.length() - 1) == '\n') {
            resultStringBuilder.setLength(resultStringBuilder.length() - 1);
        }
        return resultStringBuilder.toString();
    }

}
