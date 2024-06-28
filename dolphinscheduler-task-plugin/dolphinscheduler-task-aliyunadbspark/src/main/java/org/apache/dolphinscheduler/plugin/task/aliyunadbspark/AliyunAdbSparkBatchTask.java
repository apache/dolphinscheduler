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

package org.apache.dolphinscheduler.plugin.task.aliyunadbspark;

import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import com.aliyun.adb20211201.models.SubmitSparkAppRequest;

public class AliyunAdbSparkBatchTask extends AliyunAdbSparkBaseTask {

    private AliyunAdbSparkBatchParameters aliyunAdbSparkBatchParameters;

    protected AliyunAdbSparkBatchTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        aliyunAdbSparkBatchParameters = JSONUtils.parseObject(taskParams, AliyunAdbSparkBatchParameters.class);

        if (this.aliyunAdbSparkBatchParameters == null || !this.aliyunAdbSparkBatchParameters.checkParameters()) {
            throw new AliyunAdbSparkTaskException("Task parameters for aliyun adb spark batch application are invalid");
        }

        super.init2(aliyunAdbSparkBatchParameters);
    }

    @Override
    protected SubmitSparkAppRequest buildSubmitSparkAppRequest() {
        SubmitSparkAppRequest submitSparkAppRequest = new SubmitSparkAppRequest();

        // Set necessary parameters
        submitSparkAppRequest.setDBClusterId(aliyunAdbSparkBatchParameters.getDbClusterId());
        submitSparkAppRequest.setResourceGroupName(aliyunAdbSparkBatchParameters.getResourceGroupName());
        submitSparkAppRequest.setData(aliyunAdbSparkBatchParameters.getData().toPrettyString());

        // Set optional parameters if they are not empty
        if (StringUtils.isNotBlank(aliyunAdbSparkBatchParameters.getAppName())) {
            submitSparkAppRequest.setAppName(aliyunAdbSparkBatchParameters.getAppName());
        }
        if (StringUtils.isNotBlank(aliyunAdbSparkBatchParameters.getAppType())) {
            submitSparkAppRequest.setAppType(aliyunAdbSparkBatchParameters.getAppType());
        }

        return submitSparkAppRequest;
    }

    @Override
    public AbstractParameters getParameters() {
        return aliyunAdbSparkBatchParameters;
    }
}
