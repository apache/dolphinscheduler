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

import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Enum;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Field;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.MapField;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.OneOf;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.ReflectionObject;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Type;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.google.protobuf.DescriptorProtos;

public class TypeType {

    private TypeType() {
    }

    public static DescriptorProtos.DescriptorProto.Builder parseType(String selfName, Type type) {
        DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName(selfName);

        Map<String, DescriptorProtos.FieldDescriptorProto.Builder> fieldMap = new HashMap<>();

        enumFields(descriptorProtoBuilder, fieldMap, type.getFields());
        enumNested(descriptorProtoBuilder, type.getNested());
        enumOneofs(descriptorProtoBuilder, fieldMap, type.getOneofs());
        return descriptorProtoBuilder;
    }

    private static void enumFields(DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder,
                                   Map<String, DescriptorProtos.FieldDescriptorProto.Builder> fieldMap,
                                   Map<String, Field> fields) {
        if (fields != null)
            fields.forEach((name, pbObject) -> {
                if (pbObject != null) {
                    if (pbObject instanceof MapField) {
                        DescriptorProtos.FieldDescriptorProto.Builder mapField =
                                MapFieldType.parseMapField(descriptorProtoBuilder, name, (MapField) pbObject);
                        fieldMap.put(name, mapField);
                    } else {
                        DescriptorProtos.FieldDescriptorProto.Builder field = FieldType.parseField(name,
                                pbObject);
                        fieldMap.put(name, field);
                    }
                }
            });
    }

    private static void enumNested(DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder,
                                   Map<String, ? extends ReflectionObject> nested) {
        if (nested != null)
            nested.forEach((name, pbObject) -> {
                if (pbObject instanceof org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Enum) {
                    descriptorProtoBuilder.addEnumType(EnumType.parseEnum(name, (Enum) pbObject));
                } else if (pbObject instanceof Type) {
                    descriptorProtoBuilder.addNestedType(parseType(name, (Type) pbObject));
                }
            });
    }

    private static void enumOneofs(DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder,
                                   Map<String, DescriptorProtos.FieldDescriptorProto.Builder> fieldMap,
                                   Map<String, OneOf> oneofs) {
        if (oneofs != null) {
            oneofs.entrySet()
                    .stream()
                    .sorted(TypeType::mapEntrySorter)
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new))
                    .forEach((name, oneof) -> OneofType.parseOneof(descriptorProtoBuilder, name));
            int oneofCount = descriptorProtoBuilder.getOneofDeclCount();
            List<DescriptorProtos.OneofDescriptorProto> oneofDescriptorProtos =
                    descriptorProtoBuilder.getOneofDeclList();
            IntStream.range(0, oneofCount)
                    .forEach(oneofIndex -> {
                        DescriptorProtos.OneofDescriptorProto oneofDescriptorProto =
                                oneofDescriptorProtos.get(oneofIndex);
                        String oneofName = oneofDescriptorProto.getName();
                        oneofs.get(oneofName).getOneofList()
                                .forEach(fieldName -> fieldMap.get(fieldName).setOneofIndex(oneofIndex));
                    });
        }
        fieldMap.forEach((name, field) -> descriptorProtoBuilder.addField(field));
    }

    private static boolean isHiddenMapEntryKey(String fieldName) {
        return fieldName.startsWith("_");
    }

    private static int mapEntrySorter(Map.Entry<String, OneOf> entry1, Map.Entry<String, OneOf> entry2) {
        boolean isKey1Hidden = isHiddenMapEntryKey(entry1.getKey());
        boolean isKey2Hidden = isHiddenMapEntryKey(entry2.getKey());
        if (isKey1Hidden && !isKey2Hidden) {
            return 1; // Move key1 after key2
        } else if (!isKey1Hidden && isKey2Hidden) {
            return -1; // Move key1 before key2
        } else {
            return 0; // Keep the original order
        }
    }
}
