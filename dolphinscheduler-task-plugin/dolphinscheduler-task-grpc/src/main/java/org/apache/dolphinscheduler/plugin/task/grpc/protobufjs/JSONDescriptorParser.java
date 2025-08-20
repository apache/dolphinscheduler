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

import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Enum;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Field;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.MapField;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Method;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Namespace;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.OneOf;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Root;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Service;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.types.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

public class JSONDescriptorParser {

    public Descriptors.FileDescriptor buildDescriptor(Root root) throws Descriptors.DescriptorValidationException {
        return parseRoot(root);
    }

    private Namespace findInnerNamespace(Namespace ns) {
        while (true) {
            if (ns.nested != null && ns.nested.values().size() == 1
                    && ns.nested.values().toArray()[0] instanceof Namespace && !(ns.nested instanceof Type)
                    && !(ns.nested instanceof Service)) {
                ns = (Namespace) ns.nested.values().toArray()[0];
            } else {
                break;
            }
        }
        return ns;
    }

    public String readPackageName(Root root) {
        List<String> packageNameNS = new ArrayList<>();
        Namespace ns = root;
        while (true) {
            if (ns.nested != null && ns.nested.values().size() == 1
                    && ns.nested.values().toArray()[0] instanceof Namespace && !(ns.nested instanceof Type)
                    && !(ns.nested instanceof Service)) {
                packageNameNS.add((String) ns.nested.keySet().toArray()[0]);
                ns = (Namespace) ns.nested.values().toArray()[0];
            } else {
                break;
            }
        }
        return String.join(".", packageNameNS);
    }

    private Descriptors.FileDescriptor parseRoot(Root root) throws Descriptors.DescriptorValidationException {
        DescriptorProtos.FileDescriptorProto.Builder fileDescriptorProtoBuilder =
                DescriptorProtos.FileDescriptorProto.newBuilder()
                        .setPackage(readPackageName(root));
        Namespace innerNS = findInnerNamespace(root);
        if (innerNS.nested != null)
            innerNS.nested.forEach((name, pbObject) -> {
                if (pbObject instanceof Namespace) {
                    Namespace ns = (Namespace) pbObject;
                    if (ns instanceof Type) {
                        fileDescriptorProtoBuilder.addMessageType(parseType(name, (Type) ns));
                    } else if (ns instanceof Service) {
                        fileDescriptorProtoBuilder.addService(parseService(name, (Service) ns));
                    } else {

                    }
                } else if (pbObject instanceof Enum) {
                    fileDescriptorProtoBuilder.addEnumType(parseEnum(name, (Enum) pbObject));
                } else if (pbObject instanceof Field) {

                } else if (pbObject instanceof OneOf) {

                } else if (pbObject instanceof Method) {

                }
            });
        Descriptors.FileDescriptor fileDescriptor =
                Descriptors.FileDescriptor.buildFrom(fileDescriptorProtoBuilder.build(),
                        new Descriptors.FileDescriptor[0]);
        return fileDescriptor;
    }

    private DescriptorProtos.DescriptorProto.Builder parseType(String selfName, Type type) {
        DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder = DescriptorProtos.DescriptorProto.newBuilder()
                .setName(selfName);

        Map<String, DescriptorProtos.FieldDescriptorProto.Builder> fieldMap = new HashMap<>();
        if (type.fields != null)
            type.fields.forEach((name, pbObject) -> {
                if (pbObject instanceof Field) {
                    if (pbObject instanceof MapField) {
                        DescriptorProtos.FieldDescriptorProto.Builder mapField =
                                parseMapField(descriptorProtoBuilder, name, (MapField) pbObject);
                        fieldMap.put(name, mapField);
                    } else {
                        DescriptorProtos.FieldDescriptorProto.Builder field = parseField(name, (Field) pbObject);
                        fieldMap.put(name, field);
                    }

                }
            });
        if (type.nested != null)
            type.nested.forEach((name, pbObject) -> {
                if (pbObject instanceof Enum) {
                    descriptorProtoBuilder.addEnumType(parseEnum(name, (Enum) pbObject));
                } else if (pbObject instanceof Type) {
                    descriptorProtoBuilder.addNestedType(parseType(name, (Type) pbObject));
                }
            });
        if (type.oneofs != null) {
            type.oneofs.forEach((name, oneof) -> {
                parseOneof(descriptorProtoBuilder, name, oneof);
            });
            int oneofCount = descriptorProtoBuilder.getOneofDeclCount();
            List<DescriptorProtos.OneofDescriptorProto> oneofDescriptorProtos =
                    descriptorProtoBuilder.getOneofDeclList();
            IntStream.range(0, oneofCount)
                    .forEach(oneofIndex -> {
                        DescriptorProtos.OneofDescriptorProto oneofDescriptorProto =
                                oneofDescriptorProtos.get(oneofIndex);
                        String oneofName = oneofDescriptorProto.getName();
                        type.oneofs.get(oneofName).oneof.forEach((fieldName) -> {
                            fieldMap.get(fieldName).setOneofIndex(oneofIndex);
                        });
                    });
        }
        fieldMap.forEach((name, field) -> {
            descriptorProtoBuilder.addField(field);
        });
        return descriptorProtoBuilder;
    }

    private DescriptorProtos.ServiceDescriptorProto.Builder parseService(String selfName, Service service) {
        DescriptorProtos.ServiceDescriptorProto.Builder serviceDescriptorProtoBuilder =
                DescriptorProtos.ServiceDescriptorProto.newBuilder()
                        .setName(selfName);
        if (service.methods != null)
            service.methods.forEach((name, pbObject) -> {
                if (pbObject instanceof Method) {
                    serviceDescriptorProtoBuilder.addMethod(parseMethod(name, (Method) pbObject));
                }
            });
        return serviceDescriptorProtoBuilder;
    }

    private DescriptorProtos.MethodDescriptorProto.Builder parseMethod(String selfName, Method method) {
        DescriptorProtos.MethodDescriptorProto.Builder methodDescriptorProtoBuilder =
                DescriptorProtos.MethodDescriptorProto.newBuilder()
                        .setName(selfName)
                        .setInputType(method.requestType)
                        .setOutputType(method.responseType);
        return methodDescriptorProtoBuilder;
    }

    private DescriptorProtos.EnumDescriptorProto.Builder parseEnum(String selfName, Enum enumObj) {
        DescriptorProtos.EnumDescriptorProto.Builder enumDescriptorProtoBuilder =
                DescriptorProtos.EnumDescriptorProto.newBuilder()
                        .setName(selfName);
        enumObj.values.forEach((name, id) -> {
            DescriptorProtos.EnumValueDescriptorProto.Builder enumValueDescriptorProtoBuilder =
                    DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                            .setName(name)
                            .setNumber(id);
            enumDescriptorProtoBuilder.addValue(enumValueDescriptorProtoBuilder);
        });
        return enumDescriptorProtoBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder parseField(String selfName, Field field) {
        DescriptorProtos.FieldDescriptorProto.Builder fieldDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(selfName)
                        .setNumber(field.id);
        if (!isNull(field.options)) {
            if (!isNull(field.options.get("proto3_optional")) && ((boolean) field.options.get("proto3_optional"))) {
                fieldDescriptorProtoBuilder.setProto3Optional(true);
            }
        }
        JsonNode rule = field.rule;
        if (!isNull(rule))
            if (rule.isTextual()) {
                String label = rule.asText();
                try {
                    fieldDescriptorProtoBuilder
                            .setLabel(DescriptorProtos.FieldDescriptorProto.Label
                                    .valueOf("LABEL_" + label.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    throw new RuntimeException("grpc exception: Unrecogenized field label: " + label, e);
                }
            }
        try {
            fieldDescriptorProtoBuilder
                    .setType(DescriptorProtos.FieldDescriptorProto.Type.valueOf("TYPE_" + field.type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            fieldDescriptorProtoBuilder.setTypeName(field.type);
        }
        return fieldDescriptorProtoBuilder;
    }

    private DescriptorProtos.FieldDescriptorProto.Builder parseMapField(DescriptorProtos.DescriptorProto.Builder parentMessage,
                                                                        String selfName, MapField mapField) {
        DescriptorProtos.FieldDescriptorProto.Builder mapFieldDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(selfName)
                        .setNumber(mapField.id)
                        .setTypeName("MapEntry_" + selfName)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
        DescriptorProtos.DescriptorProto.Builder mapEntryDescriptorProtoBuilder =
                DescriptorProtos.DescriptorProto.newBuilder()
                        .setName("MapEntry_" + selfName)
                        .setOptions(DescriptorProtos.MessageOptions.newBuilder().setMapEntry(true).build());
        DescriptorProtos.FieldDescriptorProto.Builder keyDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName("key")
                        .setNumber(1);
        DescriptorProtos.FieldDescriptorProto.Builder valueDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName("value")
                        .setNumber(2);
        try {
            keyDescriptorProtoBuilder
                    .setType(DescriptorProtos.FieldDescriptorProto.Type
                            .valueOf("TYPE_" + mapField.keyType.toUpperCase()));
        } catch (IllegalArgumentException e) {
            keyDescriptorProtoBuilder.setTypeName(mapField.keyType);
        }
        try {
            valueDescriptorProtoBuilder
                    .setType(DescriptorProtos.FieldDescriptorProto.Type.valueOf("TYPE_" + mapField.type.toUpperCase()));
        } catch (IllegalArgumentException e) {
            valueDescriptorProtoBuilder.setTypeName(mapField.type);
        }
        mapEntryDescriptorProtoBuilder.addField(keyDescriptorProtoBuilder);
        mapEntryDescriptorProtoBuilder.addField(valueDescriptorProtoBuilder);
        parentMessage.addNestedType(mapEntryDescriptorProtoBuilder);

        return mapFieldDescriptorProtoBuilder;
    }

    private DescriptorProtos.OneofDescriptorProto.Builder parseOneof(DescriptorProtos.DescriptorProto.Builder descriptorProtoBuilder,
                                                                     String selfName, OneOf oneof) {
        DescriptorProtos.OneofDescriptorProto.Builder oneofDescriptorProtoBuilder =
                DescriptorProtos.OneofDescriptorProto.newBuilder()
                        .setName(selfName);
        descriptorProtoBuilder.addOneofDecl(oneofDescriptorProtoBuilder);
        return oneofDescriptorProtoBuilder;
    }
}
