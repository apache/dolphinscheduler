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

package org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types;

import static java.util.Objects.isNull;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.DescriptorProtos;

public class FieldType {

    private static final HashMap<String, DescriptorProtos.FieldDescriptorProto.Label> labelMap =
            new HashMap<String, DescriptorProtos.FieldDescriptorProto.Label>() {

                {
                    put("optional", DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
                    put("required", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED);
                    put("repeated", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
                }
            };

    private static final HashMap<String, DescriptorProtos.FieldDescriptorProto.Type> primitiveTypeMap =
            new HashMap<String, DescriptorProtos.FieldDescriptorProto.Type>() {

                {
                    put("double", DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);
                    put("float", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT);
                    put("int64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
                    put("uint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64);
                    put("int32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
                    put("fixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64);
                    put("fixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32);
                    put("bool", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
                    put("string", DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
                    put("group", DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP);
                    put("bytes", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);
                    put("uint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
                    put("enum", DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM);
                    put("sfixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32);
                    put("sfixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64);
                    put("sint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32);
                    put("sint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64);
                };
            };

    public static DescriptorProtos.FieldDescriptorProto.Label parseFieldLabel(String labelName) {
        if (!labelMap.containsKey(labelName)) {
            throw new IllegalArgumentException("Not a field label: " + labelName);
        }
        return labelMap.get(labelName);
    }

    public static DescriptorProtos.FieldDescriptorProto.Type parseFieldType(String typeName) {
        if (!primitiveTypeMap.containsKey(typeName)) {
            throw new IllegalArgumentException("Not a primitive type: " + typeName);
        }
        return primitiveTypeMap.get(typeName);
    }

    public static DescriptorProtos.FieldDescriptorProto.Builder parseField(String selfName,
                                                                           org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Field field) {
        DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(selfName)
                        .setNumber(field.id);

        enumOptions(fieldDescriptorProtoBuilder, field.options);

        JsonNode rule = field.rule;
        if (!isNull(rule))
            if (rule.isTextual()) {
                String label = rule.asText();
                try {
                    fieldDescriptorProtoBuilder
                            .setLabel(FieldType.parseFieldLabel(label));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("grpc exception: Unrecognized field label: " + label, e);
                }
            }
        try {
            fieldDescriptorProtoBuilder
                    .setType(FieldType.parseFieldType(field.type));
        } catch (IllegalArgumentException e) {
            fieldDescriptorProtoBuilder.setTypeName(field.type);
        }
        return fieldDescriptorProtoBuilder;
    }

    private static void enumOptions(DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorProtoBuilder,
                                    Map<String, Object> options) {
        if (!isNull(options)) {
            if (!isNull(options.get("proto3_optional")) && ((boolean) options.get("proto3_optional"))) {
                fieldDescriptorProtoBuilder.setProto3Optional(true);
            }
        }
    }
}
