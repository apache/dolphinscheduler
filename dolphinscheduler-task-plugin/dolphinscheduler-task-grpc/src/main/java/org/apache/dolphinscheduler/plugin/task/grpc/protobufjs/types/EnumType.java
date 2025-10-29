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

import com.google.protobuf.DescriptorProtos;

public class EnumType {

    private EnumType() {
    }

    public static DescriptorProtos.EnumDescriptorProto.Builder parseEnum(String selfName, Enum enumObj) {
        DescriptorProtos.EnumDescriptorProto.Builder enumDescriptorProtoBuilder =
                DescriptorProtos.EnumDescriptorProto.newBuilder()
                        .setName(selfName);
        enumObj.getValues().forEach((name, id) -> {
            DescriptorProtos.EnumValueDescriptorProto.Builder enumValueDescriptorProtoBuilder =
                    DescriptorProtos.EnumValueDescriptorProto.newBuilder()
                            .setName(name)
                            .setNumber(id);
            enumDescriptorProtoBuilder.addValue(enumValueDescriptorProtoBuilder);
        });
        return enumDescriptorProtoBuilder;
    }
}
