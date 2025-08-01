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

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.AbstractTask;
import org.apache.dolphinscheduler.plugin.task.api.TaskCallBack;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.GrpcDynamicService;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.JSONDescriptorHelper;

import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class GrpcTask extends AbstractTask {

    private GrpcParameters grpcParameters;
    private TaskExecutionContext taskExecutionContext;

    /**
     * constructor
     *
     * @param taskExecutionContext taskExecutionContext
     */
    protected GrpcTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
        this.taskExecutionContext = taskExecutionContext;
    }

    @Override
    public void init() {
        this.grpcParameters = JSONUtils.parseObject(taskExecutionContext.getTaskParams(), GrpcParameters.class);
        log.info("Initialize gRPC task params: {}", JSONUtils.toPrettyJsonString(grpcParameters));

        if (grpcParameters == null || !grpcParameters.checkParameters()) {
            throw new RuntimeException("gRPC task params is not valid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            val channel = GrpcDynamicService.ChannelFactory.createChannel(grpcParameters.getUrl());
            val fileDesc = JSONDescriptorHelper.FileDescFromJSON(grpcParameters.getGrpcServiceDefinitionJSON());
            val stubService = new GrpcDynamicService(channel, fileDesc);
            stubService.call(grpcParameters.getMethodName(), grpcParameters.getMessage());
        } catch (Exception e) {
            throw new TaskException("grpc handle exception:", e);
        }

        // OkHttpResponse httpResponse = sendRequest();
        //
        // validateResponse(httpResponse.getBody(), httpResponse.getStatusCode());
    }

    @Override
    public void cancel() throws TaskException {
    }

    @Override
    public AbstractParameters getParameters() {
        return this.grpcParameters;
    }

}
