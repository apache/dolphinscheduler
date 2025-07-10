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

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GrpcParameters extends AbstractParameters {

    private String url; // http://domain:port

    private String methodName; // e.g. com.example.service.ExampleService/ExampleMethod

    private String requestMessage; // e.g. {"key1": "value1", "key2": "value2"}

    private String serviceDefinition; // protobuf service definition

    private GrpcCheckCondition grpcCheckCondition = GrpcCheckCondition.STATUS_CODE_DEFAULT;

    private String condition;

    /**
     * Connect Timeout
     * Unit: ms
     */
    private int connectTimeout;

    @Override
    public boolean checkParameters() {
        return true;
    }
}
