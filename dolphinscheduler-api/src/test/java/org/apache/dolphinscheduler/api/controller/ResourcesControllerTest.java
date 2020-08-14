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

import com.alibaba.fastjson.JSON;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.ProgramType;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import com.alibaba.fastjson.JSONObject;
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
 * resources controller test
 */
public class ResourcesControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(ResourcesControllerTest.class);

    @Test
    public void testQuerytResourceList() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/resources/list")
                .header(SESSION_ID, sessionId)
                .param("type", ResourceType.FILE.name()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryResourceListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(ResourceType.FILE));
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "test");
        paramsMap.add("pageSize", "1");

        MvcResult mvcResult = mockMvc.perform(get("/resources/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testVerifyResourceName() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","list_resources_1.sh");
        paramsMap.add("type","FILE");

        MvcResult mvcResult = mockMvc.perform(get("/resources/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testViewResource() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","5");
        paramsMap.add("skipLineNum","2");
        paramsMap.add("limit","100");


        MvcResult mvcResult = mockMvc.perform(get("/resources/view")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testOnlineCreateResource() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(ResourceType.FILE));
        paramsMap.add("fileName","test_file_1");
        paramsMap.add("suffix","sh");
        paramsMap.add("description","test");
        paramsMap.add("content","echo 1111");


        MvcResult mvcResult = mockMvc.perform(post("/resources/online-create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUpdateResourceContent() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        paramsMap.add("content","echo test_1111");


        MvcResult mvcResult = mockMvc.perform(post("/resources/update-content")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDownloadResource() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "5");

        MvcResult mvcResult = mockMvc.perform(get("/resources/download")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testCreateUdfFunc() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(UdfType.HIVE));
        paramsMap.add("funcName", "test_udf");
        paramsMap.add("className", "com.test.word.contWord");
        paramsMap.add("argTypes", "argTypes");
        paramsMap.add("database", "database");
        paramsMap.add("description", "description");
        paramsMap.add("resourceId", "1");


        MvcResult mvcResult = mockMvc.perform(post("/resources/udf-func/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testViewUIUdfFunction() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");

        MvcResult mvcResult = mockMvc.perform(get("/resources/udf-func/update-ui")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testUpdateUdfFunc() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");
        paramsMap.add("type", String.valueOf(UdfType.HIVE));
        paramsMap.add("funcName", "update_duf");
        paramsMap.add("className", "com.test.word.contWord");
        paramsMap.add("argTypes", "argTypes");
        paramsMap.add("database", "database");
        paramsMap.add("description", "description");
        paramsMap.add("resourceId", "1");

        MvcResult mvcResult = mockMvc.perform(post("/resources/udf-func/update")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        Assert.assertEquals(Status.TENANT_NOT_EXIST.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryUdfFuncList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo", "1");
        paramsMap.add("searchVal", "udf");
        paramsMap.add("pageSize", "1");

        MvcResult mvcResult = mockMvc.perform(get("/resources/udf-func/list-paging")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }



    @Test
    public void testQueryResourceList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", String.valueOf(UdfType.HIVE));

        MvcResult mvcResult = mockMvc.perform(get("/resources/udf-func/list")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testVerifyUdfFuncName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name", "test");

        MvcResult mvcResult = mockMvc.perform(get("/resources/udf-func/verify-name")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testAuthorizedFile() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/resources/authed-file")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testUnauthorizedFile() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/resources/unauth-file")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testAuthorizedUDFFunction() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/resources/authed-udf-func")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUnauthUDFFunc() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");

        MvcResult mvcResult = mockMvc.perform(get("/resources/unauth-udf-func")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testDeleteUdfFunc() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id", "1");

        MvcResult mvcResult = mockMvc.perform(get("/resources/udf-func/delete")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testDeleteResource() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/resources/delete")
                .header(SESSION_ID, sessionId)
                .param("id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testqueryResourceJarList() throws Exception {

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type", ResourceType.FILE.name());
        //paramsMap.add("programType", ProgramType.PYTHON.name());
        paramsMap.add("programType", "JAVA");


        MvcResult mvcResult = mockMvc.perform(get("/resources/list/jar")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        result.getCode().equals(Status.SUCCESS.getCode());
        JSONObject object = (JSONObject) JSON.parse(mvcResult.getResponse().getContentAsString());

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
