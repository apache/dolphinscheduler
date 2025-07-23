package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.grpc.*;
import io.grpc.protobuf.ProtoUtils;
import lombok.Getter;
import lombok.val;

public class GrpcDynamicService {

    Descriptors.FileDescriptor fileDescriptor;
    ManagedChannel channel;

    public GrpcDynamicService(ManagedChannel channel, Descriptors.FileDescriptor fileDesc) {
        this.fileDescriptor = fileDesc;
        this.channel = channel;
    }


    public DynamicMessage call(String methodNameWithService, String messageJSON) throws InvalidProtocolBufferException {
        val methodNameData = new MethodName(methodNameWithService);
        val pServiceDescriptor = fileDescriptor.findServiceByName(methodNameData.serviceName);
        val pMethodDescriptor = pServiceDescriptor.findMethodByName(methodNameData.methodName);
        val methodDescriptor = methodFromProtobuf(pServiceDescriptor, pMethodDescriptor);
        val requestMessageType = pMethodDescriptor.getInputType();
        val responseMessageType = pMethodDescriptor.getOutputType();
        val requestBuilder = DynamicMessage.newBuilder(requestMessageType);
        val responseBuilder = DynamicMessage.newBuilder(responseMessageType);
        JsonFormat.parser().ignoringUnknownFields().merge(messageJSON, requestBuilder);
        val request = requestBuilder.build();
        val callOptions = CallOptions.DEFAULT;
        responseBuilder.mergeFrom((Message) io.grpc.stub.ClientCalls.blockingUnaryCall(channel, methodDescriptor, callOptions, request));
        return responseBuilder.build();
    }

    static MethodDescriptor methodFromProtobuf(
            Descriptors.ServiceDescriptor serviceDesc,
            Descriptors.MethodDescriptor methodDesc
    ) {
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
            Descriptors.MethodDescriptor methodDesc
    ) {
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
        String methodName = null;

        public MethodName(String methodNameWithService) {
            if (!checkMethodName(methodNameWithService))
                throw new RuntimeException("grpc task method name is not acceptable");
        }

        private boolean checkMethodName(String methodNameWithService) {
            String[] path = methodNameWithService.split("/");
            if (path.length == 0) return false;
            if (path.length == 1) methodName = path[0];
            if (path.length == 2) {
                serviceName = path[0];
                methodName = path[1];
            } else {
                return false;
            }
            if (serviceName == null || serviceName.isEmpty()) {
                return false;
            }
            if (methodName == null || methodName.isEmpty()) {
                return false;
            }
            return true;
        }
    }


    public static class ChannelFactory {
        public static ManagedChannel createChannel(String targetAddr) {
            return createChannel(targetAddr, InsecureChannelCredentials.create());
        }

        public static ManagedChannel createChannel(String targetAddr, ChannelCredentials channelCredentials) {
            return Grpc.newChannelBuilder(targetAddr, channelCredentials)
                    .build();
        }
    }


}
