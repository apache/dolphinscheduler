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

package org.apache.dolphinscheduler.plugin.task.emrserverless;

import org.apache.dolphinscheduler.plugin.task.api.model.ResourceInfo;
import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EmrServerlessParameters extends AbstractParameters {

    /**
     * EMR Serverless Application ID (required).
     * The application must be in STARTED or CREATED state.
     */
    private String applicationId;

    /**
     * IAM execution role ARN for the job run (required).
     */
    private String executionRoleArn;

    /**
     * Optional job name. If empty, defaults to the task name.
     */
    private String jobName;

    /**
     * StartJobRun request JSON defining jobDriver and configurationOverrides.
     * The applicationId and executionRoleArn fields in this JSON will be overridden
     * by the top-level parameters above.
     *
     * @see <a href="https://docs.aws.amazon.com/emr-serverless/latest/APIReference/API_StartJobRun.html">API_StartJobRun</a>
     */
    private String startJobRunRequestJson;

    @Override
    public boolean checkParameters() {
        return StringUtils.isNotEmpty(applicationId)
                && StringUtils.isNotEmpty(executionRoleArn)
                && StringUtils.isNotEmpty(startJobRunRequestJson);
    }

    @Override
    public List<ResourceInfo> getResourceFilesList() {
        return Collections.emptyList();
    }

    @Override
    public String toString() {
        return "EmrServerlessParameters{"
                + "applicationId='" + applicationId + '\''
                + ", executionRoleArn='" + executionRoleArn + '\''
                + ", jobName='" + jobName + '\''
                + ", startJobRunRequestJson='" + startJobRunRequestJson + '\''
                + '}';
    }
}
