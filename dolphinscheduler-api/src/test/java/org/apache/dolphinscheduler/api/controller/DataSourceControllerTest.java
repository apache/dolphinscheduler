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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.SessionService;
import org.apache.dolphinscheduler.api.service.UsersService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;

import org.apache.dolphinscheduler.dao.DaoConfiguration;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * data source controller test
 */
public class DataSourceControllerTest extends AbstractControllerTest{
    private static final Logger logger = LoggerFactory.getLogger(DataSourceControllerTest.class);


    @Ignore
    @Test
    public void testCreateDataSource() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name","mysql");
        paramsMap.put("node","mysql data source test");
        paramsMap.put("type","mysql");
        paramsMap.put("host","192.168.xxxx.xx");
        paramsMap.put("port",3306);
        paramsMap.put("database","dolphinscheduler");
        paramsMap.put("userName","root");
        paramsMap.put("password","root@123");
        paramsMap.put("other",new HashMap<>());
        MvcResult mvcResult = mockMvc.perform(post("/datasources")
                        .header("sessionId", sessionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Ignore
    @ParameterizedTest
    @ValueSource(ints = {2})
    public void testUpdateDataSource(int args) throws Exception {
        setUp();
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("id",args);
        paramsMap.put("name","mysql");
        paramsMap.put("node","mysql data source test");
        paramsMap.put("type","mysql");
        paramsMap.put("host","192.168.xxxx.xx");
        paramsMap.put("port",3306);
        paramsMap.put("principal","");
        paramsMap.put("database","dolphinscheduler");
        paramsMap.put("userName","root");
        paramsMap.put("password","root@123");
        paramsMap.put("other",new HashMap<>());
        MvcResult mvcResult = mockMvc.perform(put("/datasources/"+args)
                        .header("sessionId", sessionId)
                        .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Ignore
    @ParameterizedTest
    @ValueSource(ints = {2})
    public void testQueryDataSource(int id) throws Exception {
        setUp();
        MvcResult mvcResult = mockMvc.perform(get("/datasources/"+id)
                        .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        after();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @CsvSource({
            "type, MYSQL"
    })
    public void testQueryDataSourceList(String key , String dbType) throws Exception {
        setUp();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add(key,dbType);
        MvcResult mvcResult = mockMvc.perform(get("/datasources/list")
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
        MvcResult mvcResult = mockMvc.perform(get("/datasources")
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Ignore
    @ParameterizedTest
    @ValueSource(ints = {2})
    public void testConnectionTest(int id) throws Exception {
        setUp();
        MvcResult mvcResult = mockMvc.perform(get("/datasources/"+id+"/connect-test")
                        .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @CsvSource({
            "type, MYSQL,/datasources/verify-name"
    })
    public void testVerifyDataSourceName(String key , String dbType,String url) throws Exception {
        setUp();
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add(key,dbType);
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header("sessionId", sessionId)
                        .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
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
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @ValueSource(strings = {"/datasources/kerberos-startup-state"})
    public void testGetKerberosStartupState(String url) throws Exception {
        setUp();
        MvcResult mvcResult = mockMvc.perform(get(url)
                        .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Ignore
    @ParameterizedTest
    @ValueSource(ints = {2})
    public void testDelete(int id) throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/datasources/"+id)
                        .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
