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

package org.apache.dolphinscheduler.plugin.task.api.loop.template.http.method;

import org.apache.dolphinscheduler.common.utils.OkHttpUtils;
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskInstanceInfo;
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskInstanceStatus;
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskQueryStatusMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.HttpLoopTaskInstanceStatus;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.HttpLoopTaskMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.utils.JsonPathUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

import lombok.NonNull;

public class HttpLoopTaskQueryStatusMethodDefinition extends HttpLoopTaskMethodDefinition
        implements
            LoopTaskQueryStatusMethodDefinition {

    private final String taskInstanceFinishedJPath;
    // inject the taskInstanceId into template
    private final String taskInstanceIdHolder = "${taskInstanceId}";

    public HttpLoopTaskQueryStatusMethodDefinition(String url,
                                                   String httpMethodType,
                                                   String dataType,
                                                   Map<String, String> httpHeaders,
                                                   Map<String, Object> requestParams,
                                                   Map<String, Object> requestBody,
                                                   String taskInstanceFinishedJPath) {
        super(url, httpMethodType, dataType, httpHeaders, requestParams, requestBody);
        this.taskInstanceFinishedJPath = taskInstanceFinishedJPath;
    }

    @Override
    public @NonNull LoopTaskInstanceStatus queryTaskInstanceStatus(@NonNull LoopTaskInstanceInfo loopTaskInstanceInfo) {
        // set the loopTaskInstanceId to request params;
        if (requestParams != null) {
            for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
                if (StringUtils.equals(entry.getValue().toString(), taskInstanceIdHolder)) {
                    entry.setValue(loopTaskInstanceInfo.getTaskInstanceId());
                }
            }
        }
        if (requestBody != null) {
            for (Map.Entry<String, Object> entry : requestBody.entrySet()) {
                if (StringUtils.equalsIgnoreCase(entry.getValue().toString(), taskInstanceIdHolder)) {
                    entry.setValue(loopTaskInstanceInfo.getTaskInstanceId());
                }
            }
        }

        String responseBody;
        try {
            if (StringUtils.equalsIgnoreCase("get", httpMethodType)) {
                responseBody = OkHttpUtils.get(url, httpHeaders, requestParams);
            } else if (StringUtils.equalsIgnoreCase("post", httpMethodType)) {
                responseBody = OkHttpUtils.post(url, httpHeaders, requestParams, requestBody);
            } else {
                throw new IllegalArgumentException(String.format("http method type: %s is not supported",
                        httpMethodType));
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Query loop task instance status failed", ex);
        }
        return new HttpLoopTaskInstanceStatus(JsonPathUtils.exist(responseBody, taskInstanceFinishedJPath));
    }
}
