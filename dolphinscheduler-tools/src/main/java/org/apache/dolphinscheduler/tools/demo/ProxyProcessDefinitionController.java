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

package org.apache.dolphinscheduler.tools.demo;

import org.apache.dolphinscheduler.common.enums.ProcessExecutionTypeEnum;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.common.utils.OkHttpUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProxyProcessDefinitionController {

    @Value("${demo.api-server-port}")
    private String ServerPort;

    @Value("${demo.domain-name}")
    private String DomainName;

    public ProxyResult createProcessDefinition(String token,
                                               long projectCode,
                                               String name,
                                               String description,
                                               String globalParams,
                                               String locations,
                                               int timeout,
                                               String tenantCode,
                                               String taskRelationJson,
                                               String taskDefinitionJson,
                                               ProcessExecutionTypeEnum executionType) {
        ProxyResult proxyResult = new ProxyResult();
        String url =
                "http://" + DomainName + ":" + ServerPort + "/dolphinscheduler/projects/" + projectCode
                        + "/process-definition";
        String responseBody;
        Map<String, Object> requestBodyMap = new HashMap<>();

        requestBodyMap.put("name", name);
        requestBodyMap.put("description", description);
        requestBodyMap.put("globalParams", globalParams);
        requestBodyMap.put("locations", locations);
        requestBodyMap.put("timeout", timeout);
        requestBodyMap.put("tenantCode", tenantCode);
        requestBodyMap.put("taskRelationJson", taskRelationJson);
        requestBodyMap.put("taskDefinitionJson", taskDefinitionJson);
        requestBodyMap.put("otherParamsJson", null);
        requestBodyMap.put("executionType", executionType);

        try {
            responseBody = OkHttpUtils.demoPost(url, token, requestBodyMap);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        proxyResult = JSONUtils.parseObject(responseBody, ProxyResult.class);

        return proxyResult;

    }
}
