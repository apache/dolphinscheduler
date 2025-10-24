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

import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.MapField;

import com.google.protobuf.DescriptorProtos;

public class MapFieldType {

    private MapFieldType() {
    }
    public static DescriptorProtos.FieldDescriptorProto.Builder parseMapField(DescriptorProtos.DescriptorProto.Builder parentMessage,
                                                                              String selfName, MapField mapField) {
        String mapEntryTypeName = "MapEntry_" + selfName;
        DescriptorProtos.FieldDescriptorProto.Builder mapFieldDescriptorProtoBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(selfName)
                        .setNumber(mapField.getId())
                        .setTypeName(mapEntryTypeName)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_REPEATED);
        DescriptorProtos.DescriptorProto.Builder mapEntryDescriptorProtoBuilder =
                DescriptorProtos.DescriptorProto.newBuilder()
                        .setName(mapEntryTypeName)
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
                    .setType(FieldType.parseFieldType(mapField.getKeyType()));
        } catch (IllegalArgumentException e) {
            keyDescriptorProtoBuilder.setTypeName(mapField.getKeyType());
        }
        try {
            valueDescriptorProtoBuilder
                    .setType(FieldType.parseFieldType(mapField.getType()));
        } catch (IllegalArgumentException e) {
            valueDescriptorProtoBuilder.setTypeName(mapField.getType());
        }
        mapEntryDescriptorProtoBuilder.addField(keyDescriptorProtoBuilder);
        mapEntryDescriptorProtoBuilder.addField(valueDescriptorProtoBuilder);
        parentMessage.addNestedType(mapEntryDescriptorProtoBuilder);

        return mapFieldDescriptorProtoBuilder;
    }
}
