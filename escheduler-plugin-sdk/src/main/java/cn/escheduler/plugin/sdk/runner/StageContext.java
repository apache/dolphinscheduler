/*
 * Copyright 2017 StreamSets Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.escheduler.plugin.sdk.runner;

import cn.escheduler.plugin.api.*;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import cn.escheduler.plugin.sdk.util.ContainerError;

import cn.escheduler.plugin.api.impl.ErrorMessage;
import cn.escheduler.plugin.api.impl.Utils;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class StageContext extends ProtoContext implements
        Command.Context {
    private static final String JOB_ID = "JOB_ID";
    private final int runnerId;
    private final List<Stage.Info> pipelineInfo;
    private final Stage.UserContext userContext;
    private final boolean isPreview;
    private final Stage.Info stageInfo;
    private ErrorSink errorSink;
    private volatile boolean stop;
    private final Map<String, Object> sharedRunnerMap;
    private final long startTime;

    //for SDK
    public StageContext(
            final String instanceName,
            int runnerId,
            boolean isPreview,
            Configuration configuration
    ) {
        super(
                configuration,
                new MetricRegistry(),
                "myPipeline",
                "0",
                0,
                "x"
        );
        // create dummy info for Stage Runners. This is required for stages that expose custom metrics
        this.stageInfo = new Stage.Info() {
            @Override
            public String getName() {
                return "x";
            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public String getInstanceName() {
                return instanceName;
            }

            @Override
            public String getLabel() {
                return instanceName;
            }
        };
        this.userContext = new Stage.UserContext(
        ) {
            @Override
            public String getUser() {
                return "sdk-user";
            }

            @Override
            public String getAliasName() {
                return "sdk-user";
            }
        };
        pipelineInfo = ImmutableList.of(stageInfo);
        this.runnerId = runnerId;
        this.isPreview = isPreview;
        errorSink = new ErrorSink();
        reportErrorDelegate = errorSink;
        this.sharedRunnerMap = new ConcurrentHashMap<>();
        // sample all records while testing
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public Stage.Info getStageInfo() {
        return stageInfo;
    }

    @Override
    public Stage.UserContext getUserContext() {
        return userContext;
    }

    ReportErrorDelegate reportErrorDelegate;
    public void setReportErrorDelegate(ReportErrorDelegate delegate) {
        this.reportErrorDelegate = delegate;
    }

    @Override
    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    public void reportError(Exception exception) {
        Preconditions.checkNotNull(exception, "exception cannot be null");
        if (exception instanceof StageException) {
            StageException stageException = (StageException)exception;
            reportErrorDelegate.reportError(stageInfo.getInstanceName(), new ErrorMessage(stageException.getErrorCode(), stageException.getParams()));
        } else {
            reportErrorDelegate.reportError(stageInfo.getInstanceName(), new ErrorMessage(ContainerError.CONTAINER_0001, exception.toString()));
        }
    }

    @Override
    public void reportError(String errorMessage) {
        Preconditions.checkNotNull(errorMessage, "errorMessage cannot be null");
        reportErrorDelegate.reportError(stageInfo.getInstanceName(), new ErrorMessage(ContainerError.CONTAINER_0002, errorMessage));
    }

    @Override
    public void reportError(ErrorCode errorCode, Object... args) {
        Preconditions.checkNotNull(errorCode, "errorId cannot be null");
        reportErrorDelegate.reportError(stageInfo.getInstanceName(), new ErrorMessage(errorCode, args));
    }

    @Override
    public boolean isStopped() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public String toString() {
        return Utils.format("StageContext[instance='{}']", stageInfo.getInstanceName());
    }
}
