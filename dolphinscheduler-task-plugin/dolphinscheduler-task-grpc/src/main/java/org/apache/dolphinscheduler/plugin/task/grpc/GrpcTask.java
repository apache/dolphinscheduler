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

import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.TlsChannelCredentials;

@Slf4j
public class GrpcTask extends AbstractTask {

    private GrpcParameters grpcParameters;
    private TaskExecutionContext taskExecutionContext;
    private volatile Context.CancellableContext cancellableContext;

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
        ManagedChannel channel = null;
        try {
            if (grpcParameters.getChannelCredentialType() == GrpcCredentialType.TLS_DEFAULT) {
                TlsChannelCredentials creds = (TlsChannelCredentials) TlsChannelCredentials.create();
                channel = GrpcDynamicService.ChannelFactory.createChannel(grpcParameters.getUrl(), creds);
            } else {
                channel = GrpcDynamicService.ChannelFactory.createChannel(grpcParameters.getUrl());
            }
            Descriptors.FileDescriptor fileDesc =
                    JSONDescriptorHelper.fileDescFromJSON(grpcParameters.getGrpcServiceDefinitionJSON());

            // Attach a cancellable gRPC Context to support external cancellation.
            // This context propagates cancellation signals to the underlying RPC call.
            this.cancellableContext = Context.current().withCancellation();
            Context previous = this.cancellableContext.attach();

            try {
                GrpcDynamicService stubService = new GrpcDynamicService(channel, fileDesc);
                DynamicMessage message = stubService.call(grpcParameters.getMethodName(), grpcParameters.getMessage(),
                        grpcParameters.getConnectTimeoutMs());
                Printer printer = JsonFormat.printer().omittingInsignificantWhitespace();
                addDefaultOutput(printer.print(message));
                validateResponse(Status.OK);
            } finally {
                // Detach the cancellable context to restore the previous context and avoid leaks
                this.cancellableContext.detach(previous);
                this.cancellableContext = null;
            }
        } catch (StatusRuntimeException statusre) {
            if (statusre.getStatus().getCode() == Status.Code.CANCELLED) {
                setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
            } else {
                validateResponse(statusre.getStatus());
            }
        } catch (Exception e) {
            setExitStatusCode(TaskConstants.EXIT_CODE_FAILURE);
            throw new GrpcTaskException("gRPC handle exception:", e);
        } finally {
            // Gracefully shut down the gRPC channel to release network resources
            if (channel != null) {
                channel.shutdown();
            }
        }
    }

    @Override
    public void cancel() throws TaskException {
        // Read volatile reference once for thread safety (avoid repeated reads under race conditions)
        Context.CancellableContext ctx = this.cancellableContext;

        if (ctx != null && !ctx.isCancelled()) {
            try {
                log.info("Canceling gRPC task: method={}",
                        grpcParameters != null ? grpcParameters.getMethodName() : "unknown");

                // Trigger gRPC cancellation by canceling the context.
                // This interrupts the ongoing RPC and causes stubService.call() to throw CANCELLED.
                ctx.cancel(new TaskException("gRPC task was canceled by user"));

                // Record user intent: task was explicitly killed, not failed
                setExitStatusCode(TaskConstants.EXIT_CODE_KILL);
                log.info("gRPC task was successfully canceled");
            } catch (Exception ex) {
                log.error("Failed to cancel gRPC context", ex);
                throw new TaskException("Cancel gRPC task failed", ex);
            }
        } else {
            // No active context: task may not have started, already finished, or already canceled
            log.warn("gRPC task cancel requested, but no active cancellable context.");
        }
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
