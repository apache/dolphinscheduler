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
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.TaskTesterGrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;

/**
 * Test GrpcTask
 */
@ExtendWith(MockitoExtension.class)
public class GrpcTaskTest {

    @Rule
    public final GrpcCleanupRule GRPCCLEANUP = new GrpcCleanupRule();

    private final TaskTesterGrpc.TaskTesterImplBase SERVICE_IMPL =
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
                            // respObserver.onNext(StringReply.getDefaultInstance());
                            respObserver.onError(new StatusRuntimeException(Status.UNIMPLEMENTED));
                            respObserver.onCompleted();
                        }
                    }));

    private final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private final Server SERVER = Grpc.newServerBuilderForPort(0, InsecureServerCredentials.create())
            .executor(EXECUTOR)
            .addService(SERVICE_IMPL)
            .build()
            .start();
    private final int SERVER_PORT = SERVER.getPort();

    public GrpcTaskTest() throws IOException {
    }

    @Before
    public void setUp() throws Exception {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                if (SERVER != null) {
                    SERVER.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                }
            } catch (InterruptedException e) {
                SERVER.shutdownNow();
                e.printStackTrace(System.err);
            } finally {
                EXECUTOR.shutdown();
            }
        }));

        SERVER.start();
        SERVER.awaitTermination();
    }

    @After
    public void after() {
        if (SERVER != null && !SERVER.isShutdown()) {
            SERVER.shutdownNow();
        }
        if (!EXECUTOR.isShutdown()) {
            EXECUTOR.shutdownNow();
        }
    }

    @Test
    public void testHandleStatusCodeDefaultOK() throws Exception {
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
    public void testHandleStatusCodeCustom() throws Exception {
        GrpcTask grpcTaskUnimplemented = generateGrpcTask("TaskTester/TestUNIMPLEMENTED",
                "{\"username\":\"test username\"}", GrpcCheckCondition.STATUS_CODE_CUSTOM, "UNIMPLEMENTED");
        grpcTaskUnimplemented.handle(null);
        Assertions.assertEquals(EXIT_CODE_SUCCESS, grpcTaskUnimplemented.getExitStatusCode());
    }

    @Test
    public void testAddDefaultOutput() throws Exception {

    }

    private GrpcTask generateGrpcTask(String methodName, String requestMessage,
                                      Map<String, String> prepareParamsMap,
                                      GrpcCheckCondition grpcCheckCondition, String condition) throws IOException {
        String paramData =
                generateGrpcParameters("127.0.0.1:" + SERVER_PORT, methodName, requestMessage, grpcCheckCondition,
                        condition);
        return generateGrpcTaskFromParamData(paramData, prepareParamsMap);
    }

    private GrpcTask generateGrpcTask(String methodName, String requestMessage,
                                      GrpcCheckCondition grpcCheckCondition, String condition) throws IOException {
        String paramData =
                generateGrpcParameters("127.0.0.1:" + SERVER_PORT, methodName, requestMessage, grpcCheckCondition,
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
        grpcParameters.setConnectTimeout(10000);
        return mapper.writeValueAsString(grpcParameters);
    }

    public String readResourceTextFile(String pathInResource) throws IOException {
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
