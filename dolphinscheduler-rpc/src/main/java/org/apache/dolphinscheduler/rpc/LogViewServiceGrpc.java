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
package org.apache.dolphinscheduler.rpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;
import static io.grpc.stub.ClientCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ClientCalls.asyncClientStreamingCall;
import static io.grpc.stub.ClientCalls.asyncServerStreamingCall;
import static io.grpc.stub.ClientCalls.asyncUnaryCall;
import static io.grpc.stub.ClientCalls.blockingServerStreamingCall;
import static io.grpc.stub.ClientCalls.blockingUnaryCall;
import static io.grpc.stub.ClientCalls.futureUnaryCall;
import static io.grpc.stub.ServerCalls.asyncBidiStreamingCall;
import static io.grpc.stub.ServerCalls.asyncClientStreamingCall;
import static io.grpc.stub.ServerCalls.asyncServerStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnaryCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall;
import static io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall;

/**
 * <pre>
 **
 *  log view service
 * </pre>
 */
@javax.annotation.Generated(
        value = "by gRPC proto compiler (version 1.9.0)",
        comments = "Source: scheduler.proto")
public final class LogViewServiceGrpc {

    private LogViewServiceGrpc() {}

    public static final String SERVICE_NAME = "schduler.LogViewService";

    // Static method descriptors that strictly reflect the proto.
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    @java.lang.Deprecated // Use {@link #getRollViewLogMethod()} instead.
    public static final io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.LogParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> METHOD_ROLL_VIEW_LOG = getRollViewLogMethod();

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.LogParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> getRollViewLogMethod;

    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.LogParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> getRollViewLogMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.LogParameter, org.apache.dolphinscheduler.rpc.RetStrInfo> getRollViewLogMethod;
        if ((getRollViewLogMethod = LogViewServiceGrpc.getRollViewLogMethod) == null) {
            synchronized (LogViewServiceGrpc.class) {
                if ((getRollViewLogMethod = LogViewServiceGrpc.getRollViewLogMethod) == null) {
                    LogViewServiceGrpc.getRollViewLogMethod = getRollViewLogMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.rpc.LogParameter, org.apache.dolphinscheduler.rpc.RetStrInfo>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "schduler.LogViewService", "rollViewLog"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.LogParameter.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.RetStrInfo.getDefaultInstance()))
                                    .setSchemaDescriptor(new LogViewServiceMethodDescriptorSupplier("rollViewLog"))
                                    .build();
                }
            }
        }
        return getRollViewLogMethod;
    }
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    @java.lang.Deprecated // Use {@link #getViewLogMethod()} instead.
    public static final io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> METHOD_VIEW_LOG = getViewLogMethod();

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> getViewLogMethod;

    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetStrInfo> getViewLogMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter, org.apache.dolphinscheduler.rpc.RetStrInfo> getViewLogMethod;
        if ((getViewLogMethod = LogViewServiceGrpc.getViewLogMethod) == null) {
            synchronized (LogViewServiceGrpc.class) {
                if ((getViewLogMethod = LogViewServiceGrpc.getViewLogMethod) == null) {
                    LogViewServiceGrpc.getViewLogMethod = getViewLogMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.rpc.PathParameter, org.apache.dolphinscheduler.rpc.RetStrInfo>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "schduler.LogViewService", "viewLog"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.PathParameter.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.RetStrInfo.getDefaultInstance()))
                                    .setSchemaDescriptor(new LogViewServiceMethodDescriptorSupplier("viewLog"))
                                    .build();
                }
            }
        }
        return getViewLogMethod;
    }
    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    @java.lang.Deprecated // Use {@link #getGetLogBytesMethod()} instead.
    public static final io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetByteInfo> METHOD_GET_LOG_BYTES = getGetLogBytesMethod();

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetByteInfo> getGetLogBytesMethod;

    @io.grpc.ExperimentalApi("https://github.com/grpc/grpc-java/issues/1901")
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter,
            org.apache.dolphinscheduler.rpc.RetByteInfo> getGetLogBytesMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.rpc.PathParameter, org.apache.dolphinscheduler.rpc.RetByteInfo> getGetLogBytesMethod;
        if ((getGetLogBytesMethod = LogViewServiceGrpc.getGetLogBytesMethod) == null) {
            synchronized (LogViewServiceGrpc.class) {
                if ((getGetLogBytesMethod = LogViewServiceGrpc.getGetLogBytesMethod) == null) {
                    LogViewServiceGrpc.getGetLogBytesMethod = getGetLogBytesMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.rpc.PathParameter, org.apache.dolphinscheduler.rpc.RetByteInfo>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(
                                            "schduler.LogViewService", "getLogBytes"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.PathParameter.getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.rpc.RetByteInfo.getDefaultInstance()))
                                    .setSchemaDescriptor(new LogViewServiceMethodDescriptorSupplier("getLogBytes"))
                                    .build();
                }
            }
        }
        return getGetLogBytesMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static LogViewServiceStub newStub(io.grpc.Channel channel) {
        return new LogViewServiceStub(channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static LogViewServiceBlockingStub newBlockingStub(
            io.grpc.Channel channel) {
        return new LogViewServiceBlockingStub(channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static LogViewServiceFutureStub newFutureStub(
            io.grpc.Channel channel) {
        return new LogViewServiceFutureStub(channel);
    }

    /**
     * <pre>
     **
     *  log view service
     * </pre>
     */
    public static abstract class LogViewServiceImplBase implements io.grpc.BindableService {

        /**
         * <pre>
         **
         *  roll view log
         * </pre>
         */
        public void rollViewLog(org.apache.dolphinscheduler.rpc.LogParameter request,
                                io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo> responseObserver) {
            asyncUnimplementedUnaryCall(getRollViewLogMethod(), responseObserver);
        }

        /**
         * <pre>
         **
         * view all log
         * </pre>
         */
        public void viewLog(org.apache.dolphinscheduler.rpc.PathParameter request,
                            io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo> responseObserver) {
            asyncUnimplementedUnaryCall(getViewLogMethod(), responseObserver);
        }

        /**
         * <pre>
         **
         * get log bytes
         * </pre>
         */
        public void getLogBytes(org.apache.dolphinscheduler.rpc.PathParameter request,
                                io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetByteInfo> responseObserver) {
            asyncUnimplementedUnaryCall(getGetLogBytesMethod(), responseObserver);
        }

        @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
            return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
                    .addMethod(
                            getRollViewLogMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            org.apache.dolphinscheduler.rpc.LogParameter,
                                            org.apache.dolphinscheduler.rpc.RetStrInfo>(
                                            this, METHODID_ROLL_VIEW_LOG)))
                    .addMethod(
                            getViewLogMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            org.apache.dolphinscheduler.rpc.PathParameter,
                                            org.apache.dolphinscheduler.rpc.RetStrInfo>(
                                            this, METHODID_VIEW_LOG)))
                    .addMethod(
                            getGetLogBytesMethod(),
                            asyncUnaryCall(
                                    new MethodHandlers<
                                            org.apache.dolphinscheduler.rpc.PathParameter,
                                            org.apache.dolphinscheduler.rpc.RetByteInfo>(
                                            this, METHODID_GET_LOG_BYTES)))
                    .build();
        }
    }

    /**
     * <pre>
     **
     *  log view service
     * </pre>
     */
    public static final class LogViewServiceStub extends io.grpc.stub.AbstractStub<LogViewServiceStub> {
        private LogViewServiceStub(io.grpc.Channel channel) {
            super(channel);
        }

        private LogViewServiceStub(io.grpc.Channel channel,
                                   io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected LogViewServiceStub build(io.grpc.Channel channel,
                                           io.grpc.CallOptions callOptions) {
            return new LogViewServiceStub(channel, callOptions);
        }

        /**
         * <pre>
         **
         *  roll view log
         * </pre>
         */
        public void rollViewLog(org.apache.dolphinscheduler.rpc.LogParameter request,
                                io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getRollViewLogMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         **
         * view all log
         * </pre>
         */
        public void viewLog(org.apache.dolphinscheduler.rpc.PathParameter request,
                            io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getViewLogMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         * <pre>
         **
         * get log bytes
         * </pre>
         */
        public void getLogBytes(org.apache.dolphinscheduler.rpc.PathParameter request,
                                io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetByteInfo> responseObserver) {
            asyncUnaryCall(
                    getChannel().newCall(getGetLogBytesMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * <pre>
     **
     *  log view service
     * </pre>
     */
    public static final class LogViewServiceBlockingStub extends io.grpc.stub.AbstractStub<LogViewServiceBlockingStub> {
        private LogViewServiceBlockingStub(io.grpc.Channel channel) {
            super(channel);
        }

        private LogViewServiceBlockingStub(io.grpc.Channel channel,
                                           io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected LogViewServiceBlockingStub build(io.grpc.Channel channel,
                                                   io.grpc.CallOptions callOptions) {
            return new LogViewServiceBlockingStub(channel, callOptions);
        }

        /**
         * <pre>
         **
         *  roll view log
         * </pre>
         */
        public org.apache.dolphinscheduler.rpc.RetStrInfo rollViewLog(org.apache.dolphinscheduler.rpc.LogParameter request) {
            return blockingUnaryCall(
                    getChannel(), getRollViewLogMethod(), getCallOptions(), request);
        }

        /**
         * <pre>
         **
         * view all log
         * </pre>
         */
        public org.apache.dolphinscheduler.rpc.RetStrInfo viewLog(org.apache.dolphinscheduler.rpc.PathParameter request) {
            return blockingUnaryCall(
                    getChannel(), getViewLogMethod(), getCallOptions(), request);
        }

        /**
         * <pre>
         **
         * get log bytes
         * </pre>
         */
        public org.apache.dolphinscheduler.rpc.RetByteInfo getLogBytes(org.apache.dolphinscheduler.rpc.PathParameter request) {
            return blockingUnaryCall(
                    getChannel(), getGetLogBytesMethod(), getCallOptions(), request);
        }
    }

    /**
     * <pre>
     **
     *  log view service
     * </pre>
     */
    public static final class LogViewServiceFutureStub extends io.grpc.stub.AbstractStub<LogViewServiceFutureStub> {
        private LogViewServiceFutureStub(io.grpc.Channel channel) {
            super(channel);
        }

        private LogViewServiceFutureStub(io.grpc.Channel channel,
                                         io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected LogViewServiceFutureStub build(io.grpc.Channel channel,
                                                 io.grpc.CallOptions callOptions) {
            return new LogViewServiceFutureStub(channel, callOptions);
        }

        /**
         * <pre>
         **
         *  roll view log
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.rpc.RetStrInfo> rollViewLog(
                org.apache.dolphinscheduler.rpc.LogParameter request) {
            return futureUnaryCall(
                    getChannel().newCall(getRollViewLogMethod(), getCallOptions()), request);
        }

        /**
         * <pre>
         **
         * view all log
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.rpc.RetStrInfo> viewLog(
                org.apache.dolphinscheduler.rpc.PathParameter request) {
            return futureUnaryCall(
                    getChannel().newCall(getViewLogMethod(), getCallOptions()), request);
        }

        /**
         * <pre>
         **
         * get log bytes
         * </pre>
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.rpc.RetByteInfo> getLogBytes(
                org.apache.dolphinscheduler.rpc.PathParameter request) {
            return futureUnaryCall(
                    getChannel().newCall(getGetLogBytesMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_ROLL_VIEW_LOG = 0;
    private static final int METHODID_VIEW_LOG = 1;
    private static final int METHODID_GET_LOG_BYTES = 2;

    private static final class MethodHandlers<Req, Resp> implements
            io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
            io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
        private final LogViewServiceImplBase serviceImpl;
        private final int methodId;

        MethodHandlers(LogViewServiceImplBase serviceImpl, int methodId) {
            this.serviceImpl = serviceImpl;
            this.methodId = methodId;
        }

        @java.lang.Override
        @java.lang.SuppressWarnings("unchecked")
        public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
            switch (methodId) {
                case METHODID_ROLL_VIEW_LOG:
                    serviceImpl.rollViewLog((org.apache.dolphinscheduler.rpc.LogParameter) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo>) responseObserver);
                    break;
                case METHODID_VIEW_LOG:
                    serviceImpl.viewLog((org.apache.dolphinscheduler.rpc.PathParameter) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetStrInfo>) responseObserver);
                    break;
                case METHODID_GET_LOG_BYTES:
                    serviceImpl.getLogBytes((org.apache.dolphinscheduler.rpc.PathParameter) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.rpc.RetByteInfo>) responseObserver);
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

    private static abstract class LogViewServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
        LogViewServiceBaseDescriptorSupplier() {}

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return org.apache.dolphinscheduler.rpc.SchdulerProto.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("LogViewService");
        }
    }

    private static final class LogViewServiceFileDescriptorSupplier
            extends LogViewServiceBaseDescriptorSupplier {
        LogViewServiceFileDescriptorSupplier() {}
    }

    private static final class LogViewServiceMethodDescriptorSupplier
            extends LogViewServiceBaseDescriptorSupplier
            implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
        private final String methodName;

        LogViewServiceMethodDescriptorSupplier(String methodName) {
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
            synchronized (LogViewServiceGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new LogViewServiceFileDescriptorSupplier())
                            .addMethod(getRollViewLogMethod())
                            .addMethod(getViewLogMethod())
                            .addMethod(getGetLogBytesMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
