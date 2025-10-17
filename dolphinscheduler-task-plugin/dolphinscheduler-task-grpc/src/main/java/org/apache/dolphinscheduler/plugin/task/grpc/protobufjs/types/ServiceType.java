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

import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Method;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.mapping.Service;

import com.google.protobuf.DescriptorProtos;

public class ServiceType {

    private ServiceType() {
    }

    public static DescriptorProtos.ServiceDescriptorProto.Builder parseService(String selfName, Service service) {
        DescriptorProtos.ServiceDescriptorProto.Builder serviceDescriptorProtoBuilder =
                DescriptorProtos.ServiceDescriptorProto.newBuilder()
                        .setName(selfName);
        if (service.getMethods() != null)
            service.getMethods().forEach((name, pbObject) -> {
                if (pbObject instanceof Method) {
                    serviceDescriptorProtoBuilder.addMethod(MethodType.parseMethod(name, (Method) pbObject));
                }
            });
        return serviceDescriptorProtoBuilder;
    }
}
