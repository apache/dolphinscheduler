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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * process instance controller test
 */
public class ProcessInstanceControllerTest extends AbstractControllerTest {
    private static Logger logger = LoggerFactory.getLogger(ProcessInstanceControllerTest.class);

    @Test
    public void testQueryProcessInstanceList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processDefinitionId", "91");
        paramsMap.add("searchVal", "cxc");
        paramsMap.add("stateType", String.valueOf(ExecutionStatus.SUCCESS));
        paramsMap.add("host", "192.168.1.13");
        paramsMap.add("startDate", "2019-12-15 00:00:00");
        paramsMap.add("endDate", "2019-12-16 00:00:00");
        paramsMap.add("pageNo", "2");
        paramsMap.add("pageSize", "2");

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/list-paging","cxc_1113")
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
    public void testQueryTaskListByProcessId() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/task-list-by-process-id","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("processInstanceId","1203"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT,result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateProcessInstance() throws Exception {
        String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-36196\",\"name\":\"ssh_test1\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"aa=\\\"1234\\\"\\necho ${aa}\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"timeout\":{\"strategy\":\"\",\"interval\":null,\"enable\":false},\"taskInstancePriority\":\"MEDIUM\",\"workerGroupId\":-1,\"preTasks\":[]}],\"tenantId\":-1,\"timeout\":0}";
        String locations = "{\"tasks-36196\":{\"name\":\"ssh_test1\",\"targetarr\":\"\",\"x\":141,\"y\":70}}";

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("processInstanceJson", json);
        paramsMap.add("processInstanceId", "91");
        paramsMap.add("scheduleTime", "2019-12-15 00:00:00");
        paramsMap.add("syncDefine", "false");
        paramsMap.add("locations", locations);
        paramsMap.add("connects", "[]");
//        paramsMap.add("flag", "2");

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/instance/update","cxc_1113")
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
    public void testQueryProcessInstanceById() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/select-by-id","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("processInstanceId","1203"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }



    @Test
    public void testQuerySubProcessInstanceByTaskId() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/select-sub-process","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("taskId","1203"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.TASK_INSTANCE_NOT_EXISTS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryParentInstanceBySubId() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/select-parent-process","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("subId","1204"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.PROCESS_INSTANCE_NOT_SUB_PROCESS_INSTANCE.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testViewVariables() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/view-variables","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("processInstanceId","1204"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testDeleteProcessInstanceById() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/delete","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("processInstanceId","1204"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testBatchDeleteProcessInstanceByIds() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/projects/{projectName}/instance/batch-delete","cxc_1113")
                .header(SESSION_ID, sessionId)
                .param("processInstanceIds","1205，1206"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.DELETE_PROCESS_INSTANCE_BY_ID_ERROR.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
