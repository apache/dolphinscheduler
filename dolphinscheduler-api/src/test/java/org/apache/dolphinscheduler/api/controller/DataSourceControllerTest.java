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
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.junit.Assert;
import org.junit.Ignore;
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
 * data source controller test
 */
public class DataSourceControllerTest extends AbstractControllerTest{

    private static Logger logger = LoggerFactory.getLogger(DataSourceControllerTest.class);

    @Ignore
    @Test
    public void testCreateDataSource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","mysql");
        paramsMap.add("node","mysql data source test");
        paramsMap.add("type","MYSQL");
        paramsMap.add("host","192.168.xxxx.xx");
        paramsMap.add("port","3306");
        paramsMap.add("principal","");
        paramsMap.add("database","dolphinscheduler");
        paramsMap.add("userName","root");
        paramsMap.add("password","root@123");
        paramsMap.add("other","");
        MvcResult mvcResult = mockMvc.perform(post("/datasources/create")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Ignore
    @Test
    public void testUpdateDataSource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","2");
        paramsMap.add("name","mysql");
        paramsMap.add("node","mysql data source test");
        paramsMap.add("type","MYSQL");
        paramsMap.add("host","192.168.xxxx.xx");
        paramsMap.add("port","3306");
        paramsMap.add("principal","");
        paramsMap.add("database","dolphinscheduler");
        paramsMap.add("userName","root");
        paramsMap.add("password","root@123");
        paramsMap.add("other","");
        MvcResult mvcResult = mockMvc.perform(post("/datasources/update")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }



    @Ignore
    @Test
    public void testQueryDataSource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","2");
        MvcResult mvcResult = mockMvc.perform(post("/datasources/update-ui")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryDataSourceList() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("type","MYSQL");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/list")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testQueryDataSourceListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal","mysql");
        paramsMap.add("pageNo","1");
        paramsMap.add("pageSize","1");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/list-paging")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Ignore
    @Test
    public void testConnectDataSource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","hive data source");
        paramsMap.add("type","HIVE");
        paramsMap.add("host","192.168.xx.xx");
        paramsMap.add("port","10000");
        paramsMap.add("database","default");
        paramsMap.add("userName","hive");
        paramsMap.add("password","");
        paramsMap.add("other","");
        MvcResult mvcResult = mockMvc.perform(post("/datasources/connect")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Ignore
    @Test
    public void testConnectionTest() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","2");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/connect-by-id")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }



    @Test
    public void testVerifyDataSourceName() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","mysql");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/verify-name")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testAuthedDatasource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","2");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/authed-datasource")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testUnauthDatasource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId","2");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/unauth-datasource")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }


    @Test
    public void testGetKerberosStartupState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/datasources/kerberos-startup-state")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }



    @Ignore
    @Test
    public void testDelete() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","16");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/delete")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
