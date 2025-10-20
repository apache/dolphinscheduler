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

package org.apache.dolphinscheduler.plugin.task.grpc;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.GrpcDynamicService;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.JSONDescriptorHelper;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.protobuf.Descriptors;

@EqualsAndHashCode(callSuper = true)
@Data
public class GrpcParameters extends AbstractParameters {

    private String url;

    private GrpcCredentialType channelCredentialType = GrpcCredentialType.INSECURE;

    private String grpcServiceDefinition;

    private String grpcServiceDefinitionJSON;

    private String methodName;

    private String message;

    private GrpcCheckCondition grpcCheckCondition = GrpcCheckCondition.STATUS_CODE_DEFAULT;

    private String condition;

    @JsonProperty("grpcConnectTimeoutMs")
    private long connectTimeoutMs = 0L;

    @Override
    public boolean checkParameters() {
        // validate JSON formatted proto definition
        try {
            Descriptors.FileDescriptor fileDesc =
                    JSONDescriptorHelper.fileDescFromJSON(grpcServiceDefinitionJSON);
            GrpcDynamicService.mergeJSON(fileDesc, methodName, message);
        } catch (Descriptors.DescriptorValidationException | RuntimeException e) {
            return false;
        }
        return !(StringUtils.isEmpty(url) || connectTimeoutMs <= 0);
    }
}
