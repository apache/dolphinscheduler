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

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GrpcParameters extends AbstractParameters {

    private String url; // grpc endpoint, http://domain:port

    private String grpcServiceDefinition; // protobuf service definition, written in .proto file format

    private String grpcServiceDefinitionJSON; // parsed protobuf service definition, written in protobuf.js json descriptors, parsed from grpcServiceDefinition at browser side

    private String methodName; // e.g. com.example.service.ExampleService/ExampleMethod

    private String message; // e.g. {"key1": "value1", "key2": "value2"}, will apply to request definition

    private GrpcCheckCondition grpcCheckCondition = GrpcCheckCondition.STATUS_CODE_DEFAULT;

    private String condition;

    /**
     * Connect Timeout
     * Unit: ms
     */
    private int connectTimeout = 0; // use default timeout

    @Override
    public boolean checkParameters() {

        if (StringUtils.isEmpty(url) || connectTimeout <= 0)
            return false;
        // TODO Check apply message to definition to test
        return true;
    }
}
