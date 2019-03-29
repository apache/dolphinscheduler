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
package cn.escheduler.api.controller;

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Result;
import cn.escheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProcessDefinitionControllerTest {
    private static Logger logger = LoggerFactory.getLogger(ProcessDefinitionControllerTest.class);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    @Test
    public void createProcessDefinition() throws Exception {
        //String json = "{\"globalParams\":[],\"tasks\":[{\"type\":\"SHELL\",\"id\":\"tasks-50438\",\"name\":\"shell_01\",\"params\":{\"resourceList\":[],\"localParams\":[],\"rawScript\":\"echo \\\"123\\\"\"},\"desc\":\"\",\"runFlag\":\"NORMAL\",\"dependence\":{\"self\":\"NO_DEP_PRE\",\"outer\":{\"strategy\":\"NONE\",\"taskList\":[]}},\"maxRetryTimes\":\"0\",\"retryInterval\":\"1\",\"preTasks\":[]}]}";
        String json = "{\n" +
                "    \"globalParams\": [ ], \n" +
                "    \"tasks\": [\n" +
                "        {\n" +
                "            \"type\": \"SHELL\", \n" +
                "            \"id\": \"tasks-50438\", \n" +
                "            \"name\": \"shell_01\", \n" +
                "            \"params\": {\n" +
                "                \"resourceList\": [ ], \n" +
                "                \"localParams\": [ ], \n" +
                "                \"rawScript\": \"echo \\\"123\\\"\"\n" +
                "            }, \n" +
                "            \"desc\": \"\", \n" +
                "            \"runFlag\": \"NORMAL\", \n" +
                "            \"dependence\": {\n" +
                "                \"self\": \"NO_DEP_PRE\", \n" +
                "                \"outer\": {\n" +
                "                    \"strategy\": \"NONE\", \n" +
                "                    \"taskList\": [ ]\n" +
                "                }\n" +
                "            }, \n" +
                "            \"maxRetryTimes\": \"0\", \n" +
                "            \"retryInterval\": \"1\", \n" +
                "            \"preTasks\": [ ]\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","shell_process_01_test");
        paramsMap.add("processDefinitionJson",json);

        MvcResult mvcResult = mockMvc.perform(post("/projects/{projectName}/process/save","project_test1")
                .header("sessionId", "08fae8bf-fe2d-4fc0-8129-23c37fbfac82")
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}