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

public final class ParserTesterProto {

    private ParserTesterProto() {
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
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_Int32Only_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_Int32Only_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_SeatsEntry_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_SeatsEntry_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneRequest_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneRequest_fieldAccessorTable;
    static final com.google.protobuf.Descriptors.Descriptor internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor;
    static final com.google.protobuf.GeneratedMessageV3.FieldAccessorTable internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_fieldAccessorTable;

    public static com.google.protobuf.Descriptors.FileDescriptor getDescriptor() {
        return descriptor;
    }
    private static com.google.protobuf.Descriptors.FileDescriptor descriptor;
    static {
        java.lang.String[] descriptorData = {
                "\n\022parserTester.proto\022+org.apache.dolphin" +
                        "scheduler.task.grpc.proto\"\372\001\n\tBasicType\022" +
                        "\021\n\tstringVal\030\001 \001(\t\022\017\n\007boolVal\030\002 \001(\010\022\020\n\010f" +
                        "loatVal\030\003 \001(\002\022\021\n\tdoubleVal\030\004 \001(\001\022\020\n\010int3" +
                        "2Val\030\005 \001(\005\022\020\n\010int64Val\030\006 \001(\003\022\021\n\tuint32Va" +
                        "l\030\007 \001(\r\022\021\n\tuint64Val\030\010 \001(\004\022\021\n\tsint32Val\030" +
                        "\t \001(\021\022\021\n\tsint64Val\030\n \001(\022\022\020\n\010sfixed32\030\013 \001" +
                        "(\017\022\020\n\010sfixed64\030\014 \001(\020\022\020\n\010bytesVal\030\r \001(\014\"z" +
                        "\n\tOneofType\022\014\n\004name\030\001 \001(\t\022\020\n\003bio\030\002 \001(\tH\001" +
                        "\210\001\001\022\020\n\003age\030\003 \001(\005H\002\210\001\001\022\017\n\005phone\030\004 \001(\tH\000\022\017"
                        +
                        "\n\005email\030\005 \001(\tH\000B\t\n\007contactB\006\n\004_bioB\006\n\004_a" +
                        "ge\"Q\n\010EnumType\022E\n\010what2eat\030\001 \001(\01623.org.a" +
                        "pache.dolphinscheduler.task.grpc.proto.F" +
                        "ruits\"\027\n\tInt32Only\022\n\n\002id\030\001 \001(\005\"\221\001\n\014Basic" +
                        "MapType\022S\n\005seats\030\001 \003(\0132D.org.apache.dolp" +
                        "hinscheduler.task.grpc.proto.BasicMapTyp" +
                        "e.SeatsEntry\032,\n\nSeatsEntry\022\013\n\003key\030\001 \001(\t\022" +
                        "\r\n\005value\030\002 \001(\005:\0028\001\"\362\002\n\007MapType\022P\n\006bought" +
                        "\030\001 \003(\0132@.org.apache.dolphinscheduler.tas" +
                        "k.grpc.proto.MapType.BoughtEntry\022L\n\004cash" +
                        "\030\002 \003(\0132>.org.apache.dolphinscheduler.tas" +
                        "k.grpc.proto.MapType.CashEntry\032b\n\013Bought" +
                        "Entry\022\013\n\003key\030\001 \001(\t\022B\n\005value\030\002 \001(\01623.org." +
                        "apache.dolphinscheduler.task.grpc.proto." +
                        "Fruits:\0028\001\032c\n\tCashEntry\022\013\n\003key\030\001 \001(\t\022E\n\005" +
                        "value\030\002 \001(\01326.org.apache.dolphinschedule" +
                        "r.task.grpc.proto.Int32Only:\0028\001\"\325\001\n\020Prim" +
                        "itiveMapType\022]\n\006bought\030\001 \003(\0132M.org.apach" +
                        "e.dolphinscheduler.task.grpc.proto.Primi" +
                        "tiveMapType.MapEntry_bought\032b\n\017MapEntry_" +
                        "bought\022\013\n\003key\030\001 \001(\t\022B\n\005value\030\002 \001(\01623.org" +
                        ".apache.dolphinscheduler.task.grpc.proto" +
                        ".Fruits\"\r\n\013NoneRequest\"\013\n\tNoneReply*)\n\006F" +
                        "ruits\022\t\n\005LINGO\020\000\022\n\n\006ICHIGO\020\001\022\010\n\004MOMO\020\0022\267" +
                        "\007\n\014ParserTester\022\202\001\n\014TestNoneType\0228.org.a" +
                        "pache.dolphinscheduler.task.grpc.proto.N" +
                        "oneRequest\0326.org.apache.dolphinscheduler" +
                        ".task.grpc.proto.NoneReply\"\000\022\201\001\n\rTestBas" +
                        "icType\0226.org.apache.dolphinscheduler.tas" +
                        "k.grpc.proto.BasicType\0326.org.apache.dolp" +
                        "hinscheduler.task.grpc.proto.NoneReply\"\000" +
                        "\022\177\n\014TestEnumType\0225.org.apache.dolphinsch" +
                        "eduler.task.grpc.proto.EnumType\0326.org.ap" +
                        "ache.dolphinscheduler.task.grpc.proto.No" +
                        "neReply\"\000\022\207\001\n\020TestBasicMapType\0229.org.apa" +
                        "che.dolphinscheduler.task.grpc.proto.Bas" +
                        "icMapType\0326.org.apache.dolphinscheduler." +
                        "task.grpc.proto.NoneReply\"\000\022}\n\013TestMapTy" +
                        "pe\0224.org.apache.dolphinscheduler.task.gr" +
                        "pc.proto.MapType\0326.org.apache.dolphinsch" +
                        "eduler.task.grpc.proto.NoneReply\"\000\022\217\001\n\024T" +
                        "estPrimitiveMapType\022=.org.apache.dolphin" +
                        "scheduler.task.grpc.proto.PrimitiveMapTy" +
                        "pe\0326.org.apache.dolphinscheduler.task.gr" +
                        "pc.proto.NoneReply\"\000\022\201\001\n\rTestOneofType\0226" +
                        ".org.apache.dolphinscheduler.task.grpc.p" +
                        "roto.OneofType\0326.org.apache.dolphinsched" +
                        "uler.task.grpc.proto.NoneReply\"\000BM\n6org." +
                        "apache.dolphinscheduler.plugin.task.grpc" +
                        ".generatedB\021ParserTesterProtoP\001b\006proto3"
        };
        descriptor = com.google.protobuf.Descriptors.FileDescriptor
                .internalBuildGeneratedFileFrom(descriptorData,
                        new com.google.protobuf.Descriptors.FileDescriptor[]{
                        });
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicType_descriptor =
                getDescriptor().getMessageTypes().get(0);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicType_descriptor,
                        new java.lang.String[]{"StringVal", "BoolVal", "FloatVal", "DoubleVal", "Int32Val", "Int64Val",
                                "Uint32Val", "Uint64Val", "Sint32Val", "Sint64Val", "Sfixed32", "Sfixed64",
                                "BytesVal",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor =
                getDescriptor().getMessageTypes().get(1);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_OneofType_descriptor,
                        new java.lang.String[]{"Name", "Bio", "Age", "Phone", "Email", "Contact", "Bio", "Age",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor =
                getDescriptor().getMessageTypes().get(2);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_EnumType_descriptor,
                        new java.lang.String[]{"What2Eat",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_Int32Only_descriptor =
                getDescriptor().getMessageTypes().get(3);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_Int32Only_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_Int32Only_descriptor,
                        new java.lang.String[]{"Id",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_descriptor =
                getDescriptor().getMessageTypes().get(4);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_descriptor,
                        new java.lang.String[]{"Seats",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_SeatsEntry_descriptor =
                internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_descriptor.getNestedTypes()
                        .get(0);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_SeatsEntry_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_BasicMapType_SeatsEntry_descriptor,
                        new java.lang.String[]{"Key", "Value",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor =
                getDescriptor().getMessageTypes().get(5);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor,
                        new java.lang.String[]{"Bought", "Cash",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_descriptor =
                internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor.getNestedTypes().get(0);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_BoughtEntry_descriptor,
                        new java.lang.String[]{"Key", "Value",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_descriptor =
                internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_descriptor.getNestedTypes().get(1);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_MapType_CashEntry_descriptor,
                        new java.lang.String[]{"Key", "Value",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor =
                getDescriptor().getMessageTypes().get(6);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor,
                        new java.lang.String[]{"Bought",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor =
                internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_descriptor.getNestedTypes()
                        .get(0);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_PrimitiveMapType_MapEntry_bought_descriptor,
                        new java.lang.String[]{"Key", "Value",});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneRequest_descriptor =
                getDescriptor().getMessageTypes().get(7);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneRequest_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneRequest_descriptor,
                        new java.lang.String[]{});
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor =
                getDescriptor().getMessageTypes().get(8);
        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_fieldAccessorTable =
                new com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
                        internal_static_org_apache_dolphinscheduler_task_grpc_proto_NoneReply_descriptor,
                        new java.lang.String[]{});
    }

    // @@protoc_insertion_point(outer_class_scope)
}
