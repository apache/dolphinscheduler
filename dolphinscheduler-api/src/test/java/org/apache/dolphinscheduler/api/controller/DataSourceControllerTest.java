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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 * data source controller test
 */
public class DataSourceControllerTest extends AbstractControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceControllerTest.class);

    @BeforeEach
    public void initSetUp() {
        setUp();
    }

    @AfterEach
    public void afterEach() throws Exception {
        after();
    }

    @Disabled("unknown yourself connection information")
    @Test
    public void testCreateDataSource() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "mysql");
        paramsMap.put("node", "mysql data source test");
        paramsMap.put("type", "mysql");
        paramsMap.put("host", "127.0.0.1");
        paramsMap.put("port", 3306);
        paramsMap.put("database", "mysql");
        paramsMap.put("userName", "root");
        paramsMap.put("password", "123456");
        paramsMap.put("other", new HashMap<>());
        paramsMap.put("testFlag", 1);
        paramsMap.put("bindTestId", null);
        MvcResult mvcResult = mockMvc.perform(post("/datasources")
                .header("sessionId", sessionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("unknown yourself connection information")
    @Test
    public void testUpdateDataSource() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("id", 2);
        paramsMap.put("name", "mysql");
        paramsMap.put("node", "mysql data source test");
        paramsMap.put("type", "mysql");
        paramsMap.put("host", "192.168.xxxx.xx");
        paramsMap.put("port", 3306);
        paramsMap.put("principal", "");
        paramsMap.put("database", "dolphinscheduler");
        paramsMap.put("userName", "root");
        paramsMap.put("password", "root@123");
        paramsMap.put("other", new HashMap<>());
        paramsMap.put("testFlag", 0);
        paramsMap.put("bindTestId", 1);
        MvcResult mvcResult = mockMvc.perform(put("/datasources/2")
                .header("sessionId", sessionId)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("unknown you datasources id")
    @Test
    public void testQueryDataSource() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/datasources/2")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @CsvSource({
            "type, MYSQL"
    })
    public void testQueryDataSourceList(String key, String dbType) throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add(key, dbType);
        paramsMap.add("testFlag", "0");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/list")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testQueryDataSourceListPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("searchVal", "mysql");
        paramsMap.add("pageNo", "1");
        paramsMap.add("pageSize", "1");
        MvcResult mvcResult = mockMvc.perform(get("/datasources")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("unknown yourself connection information")
    @Test
    public void testConnectDataSource() throws Exception {
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("name", "mysql data source");
        paramsMap.put("type", "mysql");
        paramsMap.put("host", "127.0.0.1");
        paramsMap.put("port", 3306);
        paramsMap.put("database", "mysql");
        paramsMap.put("userName", "root");
        paramsMap.put("password", "123456");
        paramsMap.put("other", null);
        paramsMap.put("testFlag", 1);
        paramsMap.put("bindTestId", null);
        MvcResult mvcResult = mockMvc.perform(post("/datasources/connect")
                .header("sessionId", sessionId)
                .content(JSONUtils.toJsonString(paramsMap)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("unknown your datasource id")
    @Test
    public void testConnectionTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/datasources/2/connect-test")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @ParameterizedTest
    @CsvSource({
            "name, sourceName"
    })
    public void testVerifyDataSourceName(String key, String dbType) throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add(key, dbType);
        MvcResult mvcResult = mockMvc.perform(get("/datasources/verify-name")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testAuthedDatasource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/authed-datasource")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testUnauthDatasource() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("userId", "2");
        MvcResult mvcResult = mockMvc.perform(get("/datasources/unauth-datasource")
                .header("sessionId", sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testGetKerberosStartupState() throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/datasources/kerberos-startup-state")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Disabled("unknown your datasource id")
    @Test
    public void testDelete() throws Exception {
        MvcResult mvcResult = mockMvc.perform(delete("/datasources/2")
                .header("sessionId", sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();
        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assertions.assertEquals(Status.SUCCESS.getCode(), result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
    }
}
