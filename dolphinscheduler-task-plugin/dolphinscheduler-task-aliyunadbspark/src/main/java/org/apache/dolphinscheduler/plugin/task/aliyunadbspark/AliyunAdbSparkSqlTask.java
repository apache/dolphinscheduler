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

public class AliyunAdbSparkSqlTask extends AliyunAdbSparkBaseTask {

    private AliyunAdbSparkSqlParameters aliyunAdbSparkSqlParameters;

    protected AliyunAdbSparkSqlTask(TaskExecutionContext taskExecutionContext) {
        super(taskExecutionContext);
    }

    @Override
    public void init() {
        final String taskParams = taskExecutionContext.getTaskParams();
        aliyunAdbSparkSqlParameters = JSONUtils.parseObject(taskParams, AliyunAdbSparkSqlParameters.class);

        if (this.aliyunAdbSparkSqlParameters == null || !this.aliyunAdbSparkSqlParameters.checkParameters()) {
            throw new AliyunAdbSparkTaskException("Task parameters for aliyun adb spark sql application are invalid");
        }

        super.init2(aliyunAdbSparkSqlParameters);
    }

    @Override
    protected SubmitSparkAppRequest buildSubmitSparkAppRequest() {
        SubmitSparkAppRequest submitSparkAppRequest = new SubmitSparkAppRequest();

        // Set necessary parameters
        submitSparkAppRequest.setDBClusterId(aliyunAdbSparkSqlParameters.getDbClusterId());
        submitSparkAppRequest.setResourceGroupName(aliyunAdbSparkSqlParameters.getResourceGroupName());
        submitSparkAppRequest.setData(aliyunAdbSparkSqlParameters.getData());

        // Set optional parameters if they are not empty
        if (StringUtils.isNotBlank(aliyunAdbSparkSqlParameters.getAppName())) {
            submitSparkAppRequest.setAppName(aliyunAdbSparkSqlParameters.getAppName());
        }
        if (StringUtils.isNotBlank(aliyunAdbSparkSqlParameters.getAppType())) {
            submitSparkAppRequest.setAppType(aliyunAdbSparkSqlParameters.getAppType());
        }

        return submitSparkAppRequest;
    }

    @Override
    public AbstractParameters getParameters() {
        return aliyunAdbSparkSqlParameters;
    }
}
