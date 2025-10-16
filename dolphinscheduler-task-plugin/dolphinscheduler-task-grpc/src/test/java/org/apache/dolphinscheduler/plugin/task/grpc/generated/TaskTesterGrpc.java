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

package org.apache.dolphinscheduler.plugin.task.grpc.generated;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.71.0)", comments = "Source: taskTester.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class TaskTesterGrpc {

    private TaskTesterGrpc() {
    }

    public static final java.lang.String SERVICE_NAME = "org.apache.dolphinscheduler.task.grpc.proto.TaskTester";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestOKMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestOK", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestOKMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestOKMethod;
        if ((getTestOKMethod = TaskTesterGrpc.getTestOKMethod) == null) {
            synchronized (TaskTesterGrpc.class) {
                if ((getTestOKMethod = TaskTesterGrpc.getTestOKMethod) == null) {
                    TaskTesterGrpc.getTestOKMethod = getTestOKMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestOK"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new TaskTesterMethodDescriptorSupplier("TestOK"))
                                    .build();
                }
            }
        }
        return getTestOKMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestUNIMPLEMENTEDMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestUNIMPLEMENTED", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestUNIMPLEMENTEDMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> getTestUNIMPLEMENTEDMethod;
        if ((getTestUNIMPLEMENTEDMethod = TaskTesterGrpc.getTestUNIMPLEMENTEDMethod) == null) {
            synchronized (TaskTesterGrpc.class) {
                if ((getTestUNIMPLEMENTEDMethod = TaskTesterGrpc.getTestUNIMPLEMENTEDMethod) == null) {
                    TaskTesterGrpc.getTestUNIMPLEMENTEDMethod = getTestUNIMPLEMENTEDMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestUNIMPLEMENTED"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new TaskTesterMethodDescriptorSupplier("TestUNIMPLEMENTED"))
                                    .build();
                }
            }
        }
        return getTestUNIMPLEMENTEDMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static TaskTesterStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TaskTesterStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TaskTesterStub>() {

                    @java.lang.Override
                    public TaskTesterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TaskTesterStub(channel, callOptions);
                    }
                };
        return TaskTesterStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports all types of calls on the service
     */
    public static TaskTesterBlockingV2Stub newBlockingV2Stub(
                                                             io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TaskTesterBlockingV2Stub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TaskTesterBlockingV2Stub>() {

                    @java.lang.Override
                    public TaskTesterBlockingV2Stub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TaskTesterBlockingV2Stub(channel, callOptions);
                    }
                };
        return TaskTesterBlockingV2Stub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static TaskTesterBlockingStub newBlockingStub(
                                                         io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TaskTesterBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TaskTesterBlockingStub>() {

                    @java.lang.Override
                    public TaskTesterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TaskTesterBlockingStub(channel, callOptions);
                    }
                };
        return TaskTesterBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static TaskTesterFutureStub newFutureStub(
                                                     io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<TaskTesterFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<TaskTesterFutureStub>() {

                    @java.lang.Override
                    public TaskTesterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new TaskTesterFutureStub(channel, callOptions);
                    }
                };
        return TaskTesterFutureStub.newStub(factory, channel);
    }

    /**
     */
    public interface AsyncService {

        /**
         */
        default void testOK(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request,
                            io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestOKMethod(), responseObserver);
        }

        /**
         */
        default void testUNIMPLEMENTED(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request,
                                       io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestUNIMPLEMENTEDMethod(), responseObserver);
        }
    }

    /**
     * Base class for the server implementation of the service TaskTester.
     */
    public static abstract class TaskTesterImplBase
            implements
                io.grpc.BindableService,
                AsyncService {

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return TaskTesterGrpc.bindService(this);
        }
    }

    /**
     * A stub to allow clients to do asynchronous rpc calls to service TaskTester.
     */
    public static final class TaskTesterStub
            extends
                io.grpc.stub.AbstractAsyncStub<TaskTesterStub> {

        private TaskTesterStub(
                               io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected TaskTesterStub build(
                                       io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TaskTesterStub(channel, callOptions);
        }

        /**
         */
        public void testOK(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request,
                           io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestOKMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testUNIMPLEMENTED(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request,
                                      io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestUNIMPLEMENTEDMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * A stub to allow clients to do synchronous rpc calls to service TaskTester.
     */
    public static final class TaskTesterBlockingV2Stub
            extends
                io.grpc.stub.AbstractBlockingStub<TaskTesterBlockingV2Stub> {

        private TaskTesterBlockingV2Stub(
                                         io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected TaskTesterBlockingV2Stub build(
                                                 io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TaskTesterBlockingV2Stub(channel, callOptions);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply testOK(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestOKMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply testUNIMPLEMENTED(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestUNIMPLEMENTEDMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do limited synchronous rpc calls to service TaskTester.
     */
    public static final class TaskTesterBlockingStub
            extends
                io.grpc.stub.AbstractBlockingStub<TaskTesterBlockingStub> {

        private TaskTesterBlockingStub(
                                       io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected TaskTesterBlockingStub build(
                                               io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TaskTesterBlockingStub(channel, callOptions);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply testOK(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestOKMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply testUNIMPLEMENTED(org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestUNIMPLEMENTEDMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do ListenableFuture-style rpc calls to service TaskTester.
     */
    public static final class TaskTesterFutureStub
            extends
                io.grpc.stub.AbstractFutureStub<TaskTesterFutureStub> {

        private TaskTesterFutureStub(
                                     io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected TaskTesterFutureStub build(
                                             io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TaskTesterFutureStub(channel, callOptions);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> testOK(
                                                                                                                                             org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestOKMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply> testUNIMPLEMENTED(
                                                                                                                                                        org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestUNIMPLEMENTEDMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_TEST_OK = 0;
    private static final int METHODID_TEST_UNIMPLEMENTED = 1;

    private static final class MethodHandlers<Req, Resp>
            implements
                io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
                io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
                io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
                io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {

        private final AsyncService serviceImpl;
        private final int methodId;

        MethodHandlers(AsyncService serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_TEST_OK:
                    serviceImpl.testOK((org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>) responseObserver);
                    break;
                case METHODID_TEST_UNIMPLEMENTED:
                    serviceImpl.testUNIMPLEMENTED(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>) responseObserver);
                    break;
                default:
                    throw new AssertionError();
            }
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public io.grpc.stub.StreamObserver<Req> invoke(
                                                       io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                default:
                    throw new AssertionError();
            }
        }
    }

    public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
        return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                .addMethod(
                        getTestOKMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>(
                                        service, METHODID_TEST_OK)))
                .addMethod(
                        getTestUNIMPLEMENTEDMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.StringRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.StringReply>(
                                        service, METHODID_TEST_UNIMPLEMENTED)))
                .build();
    }

    private static abstract class TaskTesterBaseDescriptorSupplier
            implements
                io.grpc.protobuf.ProtoFileDescriptorSupplier,
                io.grpc.protobuf.ProtoServiceDescriptorSupplier {

        TaskTesterBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.TaskTesterProto.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("TaskTester");
        }
    }

    private static final class TaskTesterFileDescriptorSupplier
            extends
                TaskTesterBaseDescriptorSupplier {

        TaskTesterFileDescriptorSupplier() {
        }
    }

    private static final class TaskTesterMethodDescriptorSupplier
            extends
                TaskTesterBaseDescriptorSupplier
            implements
                io.grpc.protobuf.ProtoMethodDescriptorSupplier {

        private final java.lang.String methodName;

        TaskTesterMethodDescriptorSupplier(java.lang.String methodName) {
            this.methodName = methodName;
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
            return getServiceDescriptor().findMethodByName(methodName);
        }
    }

    private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

    public static io.grpc.ServiceDescriptor getServiceDescriptor() {
        io.grpc.ServiceDescriptor result = serviceDescriptor;
        if (result == null) {
            synchronized (TaskTesterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new TaskTesterFileDescriptorSupplier())
                            .addMethod(getTestOKMethod())
                            .addMethod(getTestUNIMPLEMENTEDMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
