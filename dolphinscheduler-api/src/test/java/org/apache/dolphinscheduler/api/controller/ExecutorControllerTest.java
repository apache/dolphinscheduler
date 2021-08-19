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
import org.junit.Test;
import org.mockito.Mockito;
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

    @Test
    public void testStartProcessInstance() throws Exception {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(executorService.execProcessInstance(Mockito.any(), Mockito.anyLong(), Mockito.anyLong(), Mockito.any(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(),
                Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.any())).thenReturn(resultData);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionCode", "1");
        paramsMap.add("scheduleTime", "");
        paramsMap.add("failureStrategy", String.valueOf(FailureStrategy.CONTINUE));
        paramsMap.add("startNodeList", "");
        paramsMap.add("taskDependType", "");
        paramsMap.add("execType", "");
        paramsMap.add("warningType", String.valueOf(WarningType.NONE));
        paramsMap.add("warningGroupId", "1");
        paramsMap.add("receivers", "");
        paramsMap.add("receiversCc", "");
        paramsMap.add("runMode", "");
        paramsMap.add("processInstancePriority", "");
        paramsMap.add("workerGroupId", "1");
        paramsMap.add("timeout", "");

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/executors/start-process-instance", 1L)
            .header("sessionId", sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testExecute() throws Exception {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(executorService.execute(Mockito.any(), Mockito.anyLong(), Mockito.anyInt(), Mockito.any())).thenReturn(resultData);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processInstanceId", "40");
        paramsMap.add("executeType", String.valueOf(ExecuteType.NONE));

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/executors/execute", 1L)
            .header("sessionId", sessionId)
            .params(paramsMap))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testStartCheck() throws Exception {
        Map<String, Object> resultData = new HashMap<>();
        resultData.put(Constants.STATUS, Status.SUCCESS);
        Mockito.when(executorService.startCheckByProcessDefinedCode(Mockito.anyLong())).thenReturn(resultData);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectCode}/executors/start-check", 1L)
            .header(SESSION_ID, sessionId)
            .param("processDefinitionCode", "1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
            .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

}
