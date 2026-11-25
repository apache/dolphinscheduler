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
import org.apache.dolphinscheduler.plugin.task.api.TaskConstants;
import org.apache.dolphinscheduler.plugin.task.api.TaskException;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.DataType;
import org.apache.dolphinscheduler.plugin.task.api.enums.Direct;
import org.apache.dolphinscheduler.plugin.task.api.model.Property;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.GrpcDynamicService;
import org.apache.dolphinscheduler.plugin.task.grpc.protobufjs.JSONDescriptorHelper;

import lombok.extern.slf4j.Slf4j;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import com.google.protobuf.util.JsonFormat.Printer;

import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;

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
            throw new GrpcTaskException(
                    "gRPC task params is not valid, method definition may not corresponds to message or method name is invalid");
        }
    }

    @Override
    public void handle(TaskCallBack taskCallBack) throws TaskException {
        try {
            ManagedChannel channel;
            if (grpcParameters.getChannelCredentialType() == GrpcCredentialType.TLS_DEFAULT) {
                TlsChannelCredentials creds = (TlsChannelCredentials) TlsChannelCredentials.create();
                channel = GrpcDynamicService.ChannelFactory.createChannel(grpcParameters.getUrl(), creds);
            } else {
                channel = GrpcDynamicService.ChannelFactory.createChannel(grpcParameters.getUrl());
            }
            Descriptors.FileDescriptor fileDesc =
                    JSONDescriptorHelper.fileDescFromJSON(grpcParameters.getGrpcServiceDefinitionJSON());
            GrpcDynamicService stubService = new GrpcDynamicService(channel, fileDesc);
            DynamicMessage message = stubService.call(grpcParameters.getMethodName(), grpcParameters.getMessage(),
                    grpcParameters.getConnectTimeoutMs());
            Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
            addDefaultOutput(printer.print(message));
        } catch (StatusRuntimeException statusre) {
            validateResponse(statusre.getStatus());
            return;
        } catch (Exception e) {
            throw new GrpcTaskException("gRPC handle exception:", e);
        }
        validateResponse(Status.OK);
    }

    @Override
    public void cancel() throws TaskException {
        // Do nothing when task to be canceled
    }

    private void validateResponse(Status statusCode) {
        switch (grpcParameters.getGrpcCheckCondition()) {
            case STATUS_CODE_DEFAULT:
                if (!statusCode.isOk()) {
                    log.error(
                            "grpc request failed, url: {}, method: {}, statusCode: {} (expected OK), checkCondition: {}",
                            grpcParameters.getUrl(), grpcParameters.getMethodName(), statusCode.getCode(),
                            GrpcCheckCondition.STATUS_CODE_DEFAULT.name());
                    exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
                    return;
                }
                break;
            case STATUS_CODE_CUSTOM:
                try {
                    Status.Code codeEnum = Status.Code.valueOf(grpcParameters.getCondition());
                    Status expectedCode = Status.fromCode(codeEnum);
                    if (statusCode != expectedCode) {
                        log.error(
                                "grpc request failed, url: {}, method: {}, statusCode: {} (expect {}), checkCondition: {}",
                                grpcParameters.getUrl(), grpcParameters.getMethodName(), statusCode.getCode(),
                                expectedCode,
                                GrpcCheckCondition.STATUS_CODE_DEFAULT.name());
                        exitStatusCode = TaskConstants.EXIT_CODE_FAILURE;
                        return;
                    }
                } catch (IllegalArgumentException e) {
                    throw new GrpcTaskException(
                            String.format("grpc unrecogenized condition %s", grpcParameters.getCondition()));
                }
                break;
            default:
                throw new GrpcTaskException(String.format("grpc check condition %s not supported",
                        grpcParameters.getGrpcCheckCondition()));
        }
        // default success log
        log.info("grpc request success, url: {}, method: {}, statusCode: {}", grpcParameters.getUrl(),
                grpcParameters.getMethodName(), statusCode.getCode());
        exitStatusCode = TaskConstants.EXIT_CODE_SUCCESS;
    }

    @Override
    public AbstractParameters getParameters() {
        return this.grpcParameters;
    }

    public void addDefaultOutput(String response) {
        Property outputProperty = new Property();
        outputProperty.setProp(String.format("%s.%s", taskExecutionContext.getTaskName(), "response"));
        outputProperty.setDirect(Direct.OUT);
        outputProperty.setType(DataType.VARCHAR);
        outputProperty.setValue(response);
        grpcParameters.addPropertyToValPool(outputProperty);
        log.info("grpc task output added to val pool: {}", outputProperty.getProp());
    }
}
