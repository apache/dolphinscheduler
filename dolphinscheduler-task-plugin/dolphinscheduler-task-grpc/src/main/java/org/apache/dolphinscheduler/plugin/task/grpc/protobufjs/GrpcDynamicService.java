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

package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs;

import static java.util.Objects.isNull;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import lombok.Getter;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;

import io.grpc.CallOptions;
import io.grpc.ChannelCredentials;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class GrpcDynamicService {

    Descriptors.FileDescriptor fileDescriptor;
    ManagedChannel channel;

    public GrpcDynamicService(ManagedChannel channel, Descriptors.FileDescriptor fileDesc) {
        this.fileDescriptor = fileDesc;
        this.channel = channel;
    }

    public DynamicMessage call(String methodNameWithService, String messageJSON, long timeout) {
        MethodName methodNameData = new MethodName(methodNameWithService);
        Descriptors.ServiceDescriptor pServiceDescriptor =
                fileDescriptor.findServiceByName(methodNameData.getServiceName());
        if (isNull(pServiceDescriptor))
            throw new GrpcParserException(
                    "cannot find service <" + methodNameData.getServiceName() + "> from service definition");
        Descriptors.MethodDescriptor pMethodDescriptor =
                pServiceDescriptor.findMethodByName(methodNameData.getRpcName());
        if (isNull(pMethodDescriptor))
            throw new GrpcParserException("cannot find method <" + methodNameData.getRpcName() + "> from service <"
                    + methodNameData.getServiceName() + "> with method list: " + Arrays.toString(pServiceDescriptor
                            .getMethods().stream().map(Descriptors.MethodDescriptor::getName).toArray()));
        MethodDescriptor<DynamicMessage, DynamicMessage> methodDescriptor =
                methodFromProtobuf(pServiceDescriptor, pMethodDescriptor);
        Descriptors.Descriptor requestMessageType = pMethodDescriptor.getInputType();
        Descriptors.Descriptor responseMessageType = pMethodDescriptor.getOutputType();
        DynamicMessage.Builder requestBuilder = DynamicMessage.newBuilder(requestMessageType);
        DynamicMessage.Builder responseBuilder = DynamicMessage.newBuilder(responseMessageType);
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(messageJSON, requestBuilder);
        } catch (InvalidProtocolBufferException ipbe) {
            throw new GrpcParserException(
                    "cannot merge json message to protobuf definition type <" + requestMessageType.getName() + ">",
                    ipbe);
        }
        DynamicMessage request = requestBuilder.build();
        CallOptions callOptions = timeout > 0 ? CallOptions.DEFAULT.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS)
                : CallOptions.DEFAULT;
        responseBuilder.mergeFrom(
                io.grpc.stub.ClientCalls.blockingUnaryCall(channel, methodDescriptor, callOptions, request));
        return responseBuilder.build();
    }

    public static DynamicMessage mergeJSON(Descriptors.FileDescriptor fileDesc, String methodNameWithService,
                                           String messageJSON) {
        MethodName methodNameData = new MethodName(methodNameWithService);
        Descriptors.ServiceDescriptor pServiceDescriptor =
                fileDesc.findServiceByName(methodNameData.getServiceName());
        if (isNull(pServiceDescriptor))
            throw new GrpcParserException(
                    "cannot find service <" + methodNameData.getServiceName() + "> from service definition");
        Descriptors.MethodDescriptor pMethodDescriptor =
                pServiceDescriptor.findMethodByName(methodNameData.getRpcName());
        if (isNull(pMethodDescriptor))
            throw new GrpcParserException("cannot find method <" + methodNameData.getRpcName() + "> from service <"
                    + methodNameData.getServiceName() + "> with method list: " + Arrays.toString(pServiceDescriptor
                            .getMethods().stream().map(Descriptors.MethodDescriptor::getName).toArray()));
        Descriptors.Descriptor requestMessageType = pMethodDescriptor.getInputType();
        DynamicMessage.Builder requestBuilder = DynamicMessage.newBuilder(requestMessageType);
        try {
            JsonFormat.parser().ignoringUnknownFields().merge(messageJSON, requestBuilder);
        } catch (InvalidProtocolBufferException ipbe) {
            throw new GrpcParserException(
                    "cannot merge json message to protobuf definition type <" + requestMessageType.getName() + ">",
                    ipbe);
        }
        return requestBuilder.build();
    }

    static MethodDescriptor<DynamicMessage, DynamicMessage> methodFromProtobuf(
                                                                               Descriptors.ServiceDescriptor serviceDesc,
                                                                               Descriptors.MethodDescriptor methodDesc) {
        return MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                .setType(getMethodTypeFromDesc(methodDesc))
                .setFullMethodName(MethodDescriptor.generateFullMethodName(
                        serviceDesc.getFullName(), methodDesc.getName()))
                .setRequestMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(methodDesc.getInputType())))
                .setResponseMarshaller(ProtoUtils.marshaller(
                        DynamicMessage.getDefaultInstance(methodDesc.getOutputType())))
                .build();
    }

    static MethodDescriptor.MethodType getMethodTypeFromDesc(
                                                             Descriptors.MethodDescriptor methodDesc) {
        if (!methodDesc.isServerStreaming()
                && !methodDesc.isClientStreaming()) {
            return MethodDescriptor.MethodType.UNARY;
        } else if (methodDesc.isServerStreaming()
                && !methodDesc.isClientStreaming()) {
            return MethodDescriptor.MethodType.SERVER_STREAMING;
        } else if (!methodDesc.isServerStreaming()) {
            return MethodDescriptor.MethodType.CLIENT_STREAMING;
        } else {
            return MethodDescriptor.MethodType.BIDI_STREAMING;
        }
    }

    public static class MethodName {

        @Getter
        String serviceName = null;
        @Getter
        String rpcName = null;

        public MethodName(String methodNameWithService) {
            if (!checkMethodName(methodNameWithService))
                throw new GrpcParserException("grpc task method name is invalid");
        }

        private boolean checkMethodName(String methodNameWithService) {
            String[] path = methodNameWithService.split(GrpcConstants.SERVICE_METHOD_SEPERATOR);
            if (path.length == 0)
                return false;
            if (path.length == 1)
                rpcName = path[0];
            if (path.length == 2) {
                serviceName = path[0];
                rpcName = path[1];
            } else {
                return false;
            }
            if (serviceName == null || serviceName.isEmpty()) {
                return false;
            }
            return rpcName != null && !rpcName.isEmpty();
        }
    }

    public static class ChannelFactory {

        private ChannelFactory() {
        }

        public static ManagedChannel createChannel(String targetAddr) {
            return createChannel(targetAddr, InsecureChannelCredentials.create());
        }

        public static ManagedChannel createChannel(String targetAddr, ChannelCredentials channelCredentials) {
            NioEventLoopGroup eventLoopGroup = new NioEventLoopGroup();
            return NettyChannelBuilder.forTarget(targetAddr, channelCredentials)
                    .eventLoopGroup(eventLoopGroup)
                    .channelType(NioSocketChannel.class)
                    .build();
        }
    }

}
