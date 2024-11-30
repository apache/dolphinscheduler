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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.WorkflowExecutionStatus;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.dao.entity.WorkerGroup;
import org.apache.dolphinscheduler.dao.mapper.WorkerGroupMapper;
import org.apache.dolphinscheduler.dao.mapper.WorkflowInstanceMapper;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * worker group controller test
 */
public class WorkerGroupControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(WorkerGroupControllerTest.class);

    @MockBean(name = "workerGroupMapper")
    private WorkerGroupMapper workerGroupMapper;

    @MockBean(name = "processInstanceMapper")
    private WorkflowInstanceMapper workflowInstanceMapper;

    @MockBean(name = "registryClient")
    private RegistryClient registryClient;

    @Test
    public void testSaveWorkerGroup() throws Exception {
        Map<String, String> serverMaps = new HashMap<>();
        serverMaps.put("192.168.0.1", "192.168.0.1");
        serverMaps.put("192.168.0.2", "192.168.0.2");
        Mockito.when(registryClient.getServerMaps(RegistryNodeType.WORKER)).thenReturn(serverMaps);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", "cxc_work_group");
        paramsMap.add("addrList", "192.168.0.1,192.168.0.2");
        paramsMap.add("description", "");
        paramsMap.add("otherParamsJson", "");
        MvcResult mvcResult = mockMvc.perform(post("/worker-groups")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAllWorkerGroupsPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "2");
        paramsMap.add("searchVal", "cxc");
        paramsMap.add("pageSize", "2");
        MvcResult mvcResult = mockMvc.perform(get("/worker-groups")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryAllWorkerGroups() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        MvcResult mvcResult = mockMvc.perform(get("/worker-groups/all")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void queryWorkerAddressList() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/worker-groups/worker-address-list")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteById() throws Exception {
        WorkerGroup workerGroup = new WorkerGroup();
        workerGroup.setId(12);
        workerGroup.setName("测试");
        Mockito.when(workerGroupMapper.selectById(12)).thenReturn(workerGroup);
        Mockito.when(workflowInstanceMapper.queryByWorkerGroupNameAndStatus("测试",
                WorkflowExecutionStatus.getNotTerminalStatus()))
                .thenReturn(null);
        Mockito.when(workerGroupMapper.deleteById(12)).thenReturn(1);
        Mockito.when(workflowInstanceMapper.updateWorkflowInstanceByWorkerGroupName("测试", "")).thenReturn(1);

        MvcResult mvcResult = mockMvc.perform(delete("/worker-groups/{id}", "12")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertTrue(result != null && result.isSuccess());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
