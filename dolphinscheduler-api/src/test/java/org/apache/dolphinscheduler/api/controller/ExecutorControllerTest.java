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

package org.apache.dolphinscheduler.api.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.ExecuteType;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.ExecutorService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.FailureStrategy;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * executor controller test
 */
public class ExecutorControllerTest extends AbstractControllerTest {

    private static Logger logger = LoggerFactory.getLogger(ExecutorControllerTest.class);

    @MockBean
    private ExecutorService executorService;

    @Ignore
    @Test
    public void testStartProcessInstance() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", "40");
        paramsMap.add("scheduleTime", "");
        paramsMap.add("failureStrategy", String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("startNodeList", "");
        paramsMap.add("taskDependType", "");
        paramsMap.add("execType", "");
        paramsMap.add("warningType", String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId", "");
        paramsMap.add("receivers", "");
        paramsMap.add("receiversCc", "");
        paramsMap.add("runMode", "");
        paramsMap.add("processInstancePriority", "");
        paramsMap.add("workerGroupId", "");
        paramsMap.add("timeout", "");

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/executors/start-process-instance", "cxc_1113")
            .header("sessionId", sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Ignore
    @Test
    public void testExecute() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processInstanceId", "40");
        paramsMap.add("executeType", String.valueOf(ExecuteType.NONE));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/executors/execute", "cxc_1113")
            .header("sessionId", sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testStartCheckProcessDefinition() throws Exception {
        Map<String, Object> mockResult = new HashMap<>();
        mockResult.put(Constants.STATUS, Status.SUCCESS);
        PowerMockito.when(executorService.startCheckByProcessDefinedCode(Mockito.anyLong())).thenReturn(mockResult);

        MvcResult mvcResult = mockMvc.perform(post("/projects/1/executors/start-check")
            .header(SESSION_ID, sessionId)
            .param("processDefinitionCode", "40"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

}
