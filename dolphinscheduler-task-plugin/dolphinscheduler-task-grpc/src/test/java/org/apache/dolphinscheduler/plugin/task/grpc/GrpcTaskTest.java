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

import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;

import org.apache.dolphinscheduler.plugin.task.grpc.generated.TaskTesterGrpc;
import org.apache.dolphinscheduler.plugin.task.grpc.generated.TaskTesterProto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.grpc.ManagedChannel;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Test GrpcTask
 */
@ExtendWith(MockitoExtension.class)
public class GrpcTaskTest {

    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private final TaskTesterGrpc.TaskTesterImplBase serviceImpl =
            mock(TaskTesterGrpc.TaskTesterImplBase.class, delegatesTo(
                    new TaskTesterGrpc.TaskTesterImplBase() {
                        // By default the client will receive Status.UNIMPLEMENTED for all RPCs.
                        // You might need to implement necessary behaviors for your test here, like this:
                        //
                        // @Override
                        // public void sayHello(HelloRequest request, StreamObserver<HelloReply> respObserver) {
                        //   respObserver.onNext(HelloReply.getDefaultInstance());
                        //   respObserver.onCompleted();
                        // }
                    }));

//    private HelloWorldClient client;

    @Before
    public void setUp() throws Exception {
        // Generate a unique in-process server name.
        String serverName = InProcessServerBuilder.generateName();

        // Create a server, add service, start, and register for automatic graceful shutdown.
        grpcCleanup.register(InProcessServerBuilder
                .forName(serverName).directExecutor().addService(serviceImpl).build().start());

        // Create a client channel and register for automatic graceful shutdown.
        ManagedChannel channel = grpcCleanup.register(
                InProcessChannelBuilder.forName(serverName).directExecutor().build());

        // Create a HelloWorldClient using the in-process channel;
//        client = new HelloWorldClient(channel);
    }

    @Test
    public void testHandleStatusCodeDefaultOK() throws Exception {

    }

    @Test
    public void testHandleStatusCodeCustom() throws Exception {

    }

    @Test
    public void testAddDefaultOutput() throws Exception {

    }

    private GrpcTask generateGrpcTask() {
        return generateGrpcTaskWithJSONDefinition();
    }

    private GrpcTask generateGrpcTaskWithJSONDefinition() {
        TaskExecutionContext taskExecutionContext = Mockito.mock(TaskExecutionContext.class);
        return new GrpcTask(taskExecutionContext);
    }
}
