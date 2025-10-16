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
@javax.annotation.Generated(value = "by gRPC proto compiler (version 1.71.0)", comments = "Source: parserTester.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class ParserTesterGrpc {

    private ParserTesterGrpc() {
    }

    public static final java.lang.String SERVICE_NAME = "org.apache.dolphinscheduler.task.grpc.proto.ParserTester";

    // Static method descriptors that strictly reflect the proto.
    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestNoneTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestNoneType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestNoneTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestNoneTypeMethod;
        if ((getTestNoneTypeMethod = ParserTesterGrpc.getTestNoneTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestNoneTypeMethod = ParserTesterGrpc.getTestNoneTypeMethod) == null) {
                    ParserTesterGrpc.getTestNoneTypeMethod = getTestNoneTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestNoneType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestNoneType"))
                                    .build();
                }
            }
        }
        return getTestNoneTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestBasicType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicTypeMethod;
        if ((getTestBasicTypeMethod = ParserTesterGrpc.getTestBasicTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestBasicTypeMethod = ParserTesterGrpc.getTestBasicTypeMethod) == null) {
                    ParserTesterGrpc.getTestBasicTypeMethod = getTestBasicTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestBasicType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestBasicType"))
                                    .build();
                }
            }
        }
        return getTestBasicTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestEnumTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestEnumType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestEnumTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestEnumTypeMethod;
        if ((getTestEnumTypeMethod = ParserTesterGrpc.getTestEnumTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestEnumTypeMethod = ParserTesterGrpc.getTestEnumTypeMethod) == null) {
                    ParserTesterGrpc.getTestEnumTypeMethod = getTestEnumTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestEnumType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestEnumType"))
                                    .build();
                }
            }
        }
        return getTestEnumTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicMapTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestBasicMapType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicMapTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestBasicMapTypeMethod;
        if ((getTestBasicMapTypeMethod = ParserTesterGrpc.getTestBasicMapTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestBasicMapTypeMethod = ParserTesterGrpc.getTestBasicMapTypeMethod) == null) {
                    ParserTesterGrpc.getTestBasicMapTypeMethod = getTestBasicMapTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestBasicMapType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestBasicMapType"))
                                    .build();
                }
            }
        }
        return getTestBasicMapTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestMapTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestMapType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestMapTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestMapTypeMethod;
        if ((getTestMapTypeMethod = ParserTesterGrpc.getTestMapTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestMapTypeMethod = ParserTesterGrpc.getTestMapTypeMethod) == null) {
                    ParserTesterGrpc.getTestMapTypeMethod = getTestMapTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestMapType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestMapType"))
                                    .build();
                }
            }
        }
        return getTestMapTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestPrimitiveMapTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestPrimitiveMapType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestPrimitiveMapTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestPrimitiveMapTypeMethod;
        if ((getTestPrimitiveMapTypeMethod = ParserTesterGrpc.getTestPrimitiveMapTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestPrimitiveMapTypeMethod = ParserTesterGrpc.getTestPrimitiveMapTypeMethod) == null) {
                    ParserTesterGrpc.getTestPrimitiveMapTypeMethod = getTestPrimitiveMapTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestPrimitiveMapType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(
                                            new ParserTesterMethodDescriptorSupplier("TestPrimitiveMapType"))
                                    .build();
                }
            }
        }
        return getTestPrimitiveMapTypeMethod;
    }

    private static volatile io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestOneofTypeMethod;

    @io.grpc.stub.annotations.RpcMethod(fullMethodName = SERVICE_NAME + '/'
            + "TestOneofType", requestType = org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType.class, responseType = org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply.class, methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
    public static io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestOneofTypeMethod() {
        io.grpc.MethodDescriptor<org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> getTestOneofTypeMethod;
        if ((getTestOneofTypeMethod = ParserTesterGrpc.getTestOneofTypeMethod) == null) {
            synchronized (ParserTesterGrpc.class) {
                if ((getTestOneofTypeMethod = ParserTesterGrpc.getTestOneofTypeMethod) == null) {
                    ParserTesterGrpc.getTestOneofTypeMethod = getTestOneofTypeMethod =
                            io.grpc.MethodDescriptor.<org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>newBuilder()
                                    .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                                    .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TestOneofType"))
                                    .setSampledToLocalTracing(true)
                                    .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType
                                                    .getDefaultInstance()))
                                    .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                                            org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply
                                                    .getDefaultInstance()))
                                    .setSchemaDescriptor(new ParserTesterMethodDescriptorSupplier("TestOneofType"))
                                    .build();
                }
            }
        }
        return getTestOneofTypeMethod;
    }

    /**
     * Creates a new async stub that supports all call types for the service
     */
    public static ParserTesterStub newStub(io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<ParserTesterStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<ParserTesterStub>() {

                    @java.lang.Override
                    public ParserTesterStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new ParserTesterStub(channel, callOptions);
                    }
                };
        return ParserTesterStub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports all types of calls on the service
     */
    public static ParserTesterBlockingV2Stub newBlockingV2Stub(
                                                               io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<ParserTesterBlockingV2Stub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<ParserTesterBlockingV2Stub>() {

                    @java.lang.Override
                    public ParserTesterBlockingV2Stub newStub(io.grpc.Channel channel,
                                                              io.grpc.CallOptions callOptions) {
                        return new ParserTesterBlockingV2Stub(channel, callOptions);
                    }
                };
        return ParserTesterBlockingV2Stub.newStub(factory, channel);
    }

    /**
     * Creates a new blocking-style stub that supports unary and streaming output calls on the service
     */
    public static ParserTesterBlockingStub newBlockingStub(
                                                           io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<ParserTesterBlockingStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<ParserTesterBlockingStub>() {

                    @java.lang.Override
                    public ParserTesterBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new ParserTesterBlockingStub(channel, callOptions);
                    }
                };
        return ParserTesterBlockingStub.newStub(factory, channel);
    }

    /**
     * Creates a new ListenableFuture-style stub that supports unary calls on the service
     */
    public static ParserTesterFutureStub newFutureStub(
                                                       io.grpc.Channel channel) {
        io.grpc.stub.AbstractStub.StubFactory<ParserTesterFutureStub> factory =
                new io.grpc.stub.AbstractStub.StubFactory<ParserTesterFutureStub>() {

                    @java.lang.Override
                    public ParserTesterFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
                        return new ParserTesterFutureStub(channel, callOptions);
                    }
                };
        return ParserTesterFutureStub.newStub(factory, channel);
    }

    /**
     */
    public interface AsyncService {

        /**
         */
        default void testNoneType(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest request,
                                  io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestNoneTypeMethod(), responseObserver);
        }

        /**
         */
        default void testBasicType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType request,
                                   io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestBasicTypeMethod(), responseObserver);
        }

        /**
         */
        default void testEnumType(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType request,
                                  io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestEnumTypeMethod(), responseObserver);
        }

        /**
         */
        default void testBasicMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType request,
                                      io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestBasicMapTypeMethod(), responseObserver);
        }

        /**
         */
        default void testMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType request,
                                 io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestMapTypeMethod(), responseObserver);
        }

        /**
         */
        default void testPrimitiveMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType request,
                                          io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestPrimitiveMapTypeMethod(), responseObserver);
        }

        /**
         */
        default void testOneofType(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType request,
                                   io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTestOneofTypeMethod(), responseObserver);
        }
    }

    /**
     * Base class for the server implementation of the service ParserTester.
     */
    public static abstract class ParserTesterImplBase
            implements
                io.grpc.BindableService,
                AsyncService {

        @java.lang.Override
        public final io.grpc.ServerServiceDefinition bindService() {
            return ParserTesterGrpc.bindService(this);
        }
    }

    /**
     * A stub to allow clients to do asynchronous rpc calls to service ParserTester.
     */
    public static final class ParserTesterStub
            extends
                io.grpc.stub.AbstractAsyncStub<ParserTesterStub> {

        private ParserTesterStub(
                                 io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected ParserTesterStub build(
                                         io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ParserTesterStub(channel, callOptions);
        }

        /**
         */
        public void testNoneType(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest request,
                                 io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestNoneTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testBasicType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType request,
                                  io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestBasicTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testEnumType(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType request,
                                 io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestEnumTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testBasicMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType request,
                                     io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestBasicMapTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType request,
                                io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestMapTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testPrimitiveMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType request,
                                         io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestPrimitiveMapTypeMethod(), getCallOptions()), request, responseObserver);
        }

        /**
         */
        public void testOneofType(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType request,
                                  io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> responseObserver) {
            io.grpc.stub.ClientCalls.asyncUnaryCall(
                    getChannel().newCall(getTestOneofTypeMethod(), getCallOptions()), request, responseObserver);
        }
    }

    /**
     * A stub to allow clients to do synchronous rpc calls to service ParserTester.
     */
    public static final class ParserTesterBlockingV2Stub
            extends
                io.grpc.stub.AbstractBlockingStub<ParserTesterBlockingV2Stub> {

        private ParserTesterBlockingV2Stub(
                                           io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected ParserTesterBlockingV2Stub build(
                                                   io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ParserTesterBlockingV2Stub(channel, callOptions);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testNoneType(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestNoneTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testBasicType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestBasicTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testEnumType(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestEnumTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testBasicMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestBasicMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testPrimitiveMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestPrimitiveMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testOneofType(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestOneofTypeMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do limited synchronous rpc calls to service ParserTester.
     */
    public static final class ParserTesterBlockingStub
            extends
                io.grpc.stub.AbstractBlockingStub<ParserTesterBlockingStub> {

        private ParserTesterBlockingStub(
                                         io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected ParserTesterBlockingStub build(
                                                 io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ParserTesterBlockingStub(channel, callOptions);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testNoneType(org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestNoneTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testBasicType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestBasicTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testEnumType(org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestEnumTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testBasicMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestBasicMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testPrimitiveMapType(org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestPrimitiveMapTypeMethod(), getCallOptions(), request);
        }

        /**
         */
        public org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply testOneofType(org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType request) {
            return io.grpc.stub.ClientCalls.blockingUnaryCall(
                    getChannel(), getTestOneofTypeMethod(), getCallOptions(), request);
        }
    }

    /**
     * A stub to allow clients to do ListenableFuture-style rpc calls to service ParserTester.
     */
    public static final class ParserTesterFutureStub
            extends
                io.grpc.stub.AbstractFutureStub<ParserTesterFutureStub> {

        private ParserTesterFutureStub(
                                       io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            super(channel, callOptions);
        }

        @java.lang.Override
        protected ParserTesterFutureStub build(
                                               io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new ParserTesterFutureStub(channel, callOptions);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testNoneType(
                                                                                                                                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestNoneTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testBasicType(
                                                                                                                                                  org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestBasicTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testEnumType(
                                                                                                                                                 org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestEnumTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testBasicMapType(
                                                                                                                                                     org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestBasicMapTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testMapType(
                                                                                                                                                org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestMapTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testPrimitiveMapType(
                                                                                                                                                         org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestPrimitiveMapTypeMethod(), getCallOptions()), request);
        }

        /**
         */
        public com.google.common.util.concurrent.ListenableFuture<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply> testOneofType(
                                                                                                                                                  org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType request) {
            return io.grpc.stub.ClientCalls.futureUnaryCall(
                    getChannel().newCall(getTestOneofTypeMethod(), getCallOptions()), request);
        }
    }

    private static final int METHODID_TEST_NONE_TYPE = 0;
    private static final int METHODID_TEST_BASIC_TYPE = 1;
    private static final int METHODID_TEST_ENUM_TYPE = 2;
    private static final int METHODID_TEST_BASIC_MAP_TYPE = 3;
    private static final int METHODID_TEST_MAP_TYPE = 4;
    private static final int METHODID_TEST_PRIMITIVE_MAP_TYPE = 5;
    private static final int METHODID_TEST_ONEOF_TYPE = 6;

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
                case METHODID_TEST_NONE_TYPE:
                    serviceImpl.testNoneType(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_BASIC_TYPE:
                    serviceImpl.testBasicType(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_ENUM_TYPE:
                    serviceImpl.testEnumType((org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_BASIC_MAP_TYPE:
                    serviceImpl.testBasicMapType(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_MAP_TYPE:
                    serviceImpl.testMapType((org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_PRIMITIVE_MAP_TYPE:
                    serviceImpl.testPrimitiveMapType(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
                    break;
                case METHODID_TEST_ONEOF_TYPE:
                    serviceImpl.testOneofType(
                            (org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType) request,
                            (io.grpc.stub.StreamObserver<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>) responseObserver);
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
                        getTestNoneTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneRequest, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_NONE_TYPE)))
                .addMethod(
                        getTestBasicTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_BASIC_TYPE)))
                .addMethod(
                        getTestEnumTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.EnumType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_ENUM_TYPE)))
                .addMethod(
                        getTestBasicMapTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.BasicMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_BASIC_MAP_TYPE)))
                .addMethod(
                        getTestMapTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.MapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_MAP_TYPE)))
                .addMethod(
                        getTestPrimitiveMapTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.PrimitiveMapType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_PRIMITIVE_MAP_TYPE)))
                .addMethod(
                        getTestOneofTypeMethod(),
                        io.grpc.stub.ServerCalls.asyncUnaryCall(
                                new MethodHandlers<org.apache.dolphinscheduler.plugin.task.grpc.generated.OneofType, org.apache.dolphinscheduler.plugin.task.grpc.generated.NoneReply>(
                                        service, METHODID_TEST_ONEOF_TYPE)))
                .build();
    }

    private static abstract class ParserTesterBaseDescriptorSupplier
            implements
                io.grpc.protobuf.ProtoFileDescriptorSupplier,
                io.grpc.protobuf.ProtoServiceDescriptorSupplier {

        ParserTesterBaseDescriptorSupplier() {
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
            return org.apache.dolphinscheduler.plugin.task.grpc.generated.ParserTesterProto.getDescriptor();
        }

        @java.lang.Override
        public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
            return getFileDescriptor().findServiceByName("ParserTester");
        }
    }

    private static final class ParserTesterFileDescriptorSupplier
            extends
                ParserTesterBaseDescriptorSupplier {

        ParserTesterFileDescriptorSupplier() {
        }
    }

    private static final class ParserTesterMethodDescriptorSupplier
            extends
                ParserTesterBaseDescriptorSupplier
            implements
                io.grpc.protobuf.ProtoMethodDescriptorSupplier {

        private final java.lang.String methodName;

        ParserTesterMethodDescriptorSupplier(java.lang.String methodName) {
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
            synchronized (ParserTesterGrpc.class) {
                result = serviceDescriptor;
                if (result == null) {
                    serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                            .setSchemaDescriptor(new ParserTesterFileDescriptorSupplier())
                            .addMethod(getTestNoneTypeMethod())
                            .addMethod(getTestBasicTypeMethod())
                            .addMethod(getTestEnumTypeMethod())
                            .addMethod(getTestBasicMapTypeMethod())
                            .addMethod(getTestMapTypeMethod())
                            .addMethod(getTestPrimitiveMapTypeMethod())
                            .addMethod(getTestOneofTypeMethod())
                            .build();
                }
            }
        }
        return result;
    }
}
