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
import org.apache.dolphinscheduler.plugin.task.api.loop.LoopTaskSubmitTaskMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.HttpLoopTaskInstanceInfo;
import org.apache.dolphinscheduler.plugin.task.api.loop.template.http.HttpLoopTaskMethodDefinition;
import org.apache.dolphinscheduler.plugin.task.api.utils.JsonPathUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;

public class HttpLoopTaskSubmitTaskMethodDefinition extends HttpLoopTaskMethodDefinition
        implements
            LoopTaskSubmitTaskMethodDefinition {

    private final String taskInstanceIdJPath;

    public HttpLoopTaskSubmitTaskMethodDefinition(String url,
                                                  String httpMethodType,
                                                  String dataType,
                                                  Map<String, String> httpHeaders,
                                                  Map<String, Object> requestParams,
                                                  Map<String, Object> requestBody,
                                                  @NonNull String taskInstanceIdJPath) {
        super(url, httpMethodType, dataType, httpHeaders, requestParams, requestBody);
        this.taskInstanceIdJPath = taskInstanceIdJPath;
    }

    @Override
    public @NonNull LoopTaskInstanceInfo submitLoopTask() {
        // todo: call http api to submit task
        String responseBody;
        try {
            if (StringUtils.equalsIgnoreCase(httpMethodType, "GET")) {
                responseBody = OkHttpUtils.get(url, httpHeaders, requestParams);
            } else if (StringUtils.equalsIgnoreCase(httpMethodType, "POST")) {
                responseBody = OkHttpUtils.post(url, httpHeaders, requestParams, requestBody);
            } else {
                throw new IllegalArgumentException(String.format("The request method type: %s is not supported.",
                        httpMethodType));
            }
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Submit loop task error", ex);
        }
        Optional<String> taskInstanceIdOptional = JsonPathUtils.read(responseBody, taskInstanceIdJPath);
        String taskInstanceId = taskInstanceIdOptional.orElseThrow(() -> new RuntimeException(String.format(
                "Resolve the taskInstanceId error, responseBody: %s, taskInstanceIdJPath: %s",
                responseBody,
                taskInstanceIdJPath)));
        return new HttpLoopTaskInstanceInfo(taskInstanceId);
    }
}
