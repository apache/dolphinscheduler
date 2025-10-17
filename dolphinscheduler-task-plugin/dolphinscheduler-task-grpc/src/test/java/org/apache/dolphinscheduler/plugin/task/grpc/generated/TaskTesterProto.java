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

public final class TaskTesterProto {

    private TaskTesterProto() {
    }
    public static void registerAllExtensions(
                                             com.google.protobuf.ExtensionRegistryLite registry) {
        // No extensions to register
    }

    public static void registerAllExtensions(
                                             com.google.protobuf.ExtensionRegistry registry) {
        registerAllExtensions(
                (com.google.protobuf.ExtensionRegistryLite) registry);
    }
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringRequest_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringRequest_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringReply_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringReply_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }
    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
    static {
        java.lang.String[] descriptorData = {
                "\n\020taskTester.proto\022+org.apache.dolphinsc" +
                        "heduler.task.grpc.proto\"!\n\rStringRequest" +
                        "\022\020\n\010username\030\001 \001(\t\"\036\n\013StringReply\022\017\n\007mes" +
                        "sage\030\001 \001(\t2\235\002\n\nTaskTester\022\200\001\n\006TestOK\022:.o" +
                        "rg.apache.dolphinscheduler.task.grpc.pro" +
                        "to.StringRequest\0328.org.apache.dolphinsch" +
                        "eduler.task.grpc.proto.StringReply\"\000\022\213\001\n" +
                        "\021TestUNIMPLEMENTED\022:.org.apache.dolphins" +
                        "cheduler.task.grpc.proto.StringRequest\0328" +
                        ".org.apache.dolphinscheduler.task.grpc.p" +
                        "roto.StringReply\"\000BK\n6org.apache.dolphin" +
                        "scheduler.plugin.task.grpc.generatedB\017Ta" +
                        "skTesterProtoP\001b\006proto3"
        };
        descriptor = com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        });
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringRequest_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringRequest_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringRequest_descriptor,
                        new java.lang.String[]{"Username",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringReply_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringReply_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_StringReply_descriptor,
                        new java.lang.String[]{"Message",});
    }

    // @@protoc_insertion_point(outer_class_scope)
}
