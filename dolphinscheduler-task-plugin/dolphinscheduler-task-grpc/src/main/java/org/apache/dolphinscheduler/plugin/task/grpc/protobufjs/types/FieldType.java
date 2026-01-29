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

import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.GrpcParserException;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.DescriptorProtos;

public class FieldType {

    private FieldType() {
    }

    private static final HashMap<String, DescriptorProtos.FieldDescriptorProto.Label> labelMap = initLabelMap();
    private static HashMap<String, DescriptorProtos.FieldDescriptorProto.Label> initLabelMap() {
        HashMap<String, DescriptorProtos.FieldDescriptorProto.Label> map = new HashMap<>();
        map.put("optional", DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);
        map.put("required", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REQUIRED);
        map.put("repeated", DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
        return map;
    }

    private static final HashMap<String, DescriptorProtos.FieldDescriptorProto.Type> primitiveTypeMap =
            initPrimitiveTypeMap();
    private static HashMap<String, DescriptorProtos.FieldDescriptorProto.Type> initPrimitiveTypeMap() {
        HashMap<String, DescriptorProtos.FieldDescriptorProto.Type> map = new HashMap<>();
        map.put("double", DescriptorProtos.FieldDescriptorProto.Type.TYPE_DOUBLE);
        map.put("float", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FLOAT);
        map.put("int64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT64);
        map.put("uint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64);
        map.put("int32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_INT32);
        map.put("fixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED64);
        map.put("fixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_FIXED32);
        map.put("bool", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL);
        map.put("string", DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING);
        map.put("group", DescriptorProtos.FieldDescriptorProto.Type.TYPE_GROUP);
        map.put("bytes", DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES);
        map.put("uint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32);
        map.put("enum", DescriptorProtos.FieldDescriptorProto.Type.TYPE_ENUM);
        map.put("sfixed32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED32);
        map.put("sfixed64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SFIXED64);
        map.put("sint32", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT32);
        map.put("sint64", DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64);
        return map;
    }

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
                        .setNumber(field.getId());

        enumOptions(fieldDescriptorProtoBuilder, field.getOptions());

        JsonNode rule = field.getRule();
        if (!isNull(rule) && rule.isTextual()) {
            String label = rule.asText();
            try {
                fieldDescriptorProtoBuilder
                        .setLabel(FieldType.parseFieldLabel(label));
            } catch (IllegalArgumentException e) {
                throw new GrpcParserException("grpc exception: Unrecognized field label: " + label, e);
            }
        }
        try {
            fieldDescriptorProtoBuilder
                    .setType(FieldType.parseFieldType(field.getType()));
        } catch (IllegalArgumentException e) {
            fieldDescriptorProtoBuilder.setTypeName(field.getType());
        }
        return fieldDescriptorProtoBuilder;
    }

    private static void enumOptions(DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorProtoBuilder,
                                    Map<String, Object> options) {
        if (!isNull(options) && !isNull(options.get("proto3_optional")) && ((boolean) options.get("proto3_optional"))) {
            fieldDescriptorProtoBuilder.setProto3Optional(true);
        }
    }
}
