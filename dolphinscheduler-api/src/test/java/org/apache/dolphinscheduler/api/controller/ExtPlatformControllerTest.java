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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.yss.henghe.platform.tools.constraint.SourceCodeConstraint;

/**
 * calendar controller test
 */
@SourceCodeConstraint.AddedBy(SourceCodeConstraint.Author.ZHANGLONG)
public class ExtPlatformControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(ExtPlatformControllerTest.class);


    @Test
    public void testCreateExtPlatform() throws Exception {

        Map map = new HashMap<String,Object>();
        map.put("url","http://127.0.0.1:8080/list.json");
        map.put("urlType","0");

        String connectParam = JSONUtils.toJson(map);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","平台名称");
        paramsMap.add("platformType","0");
        paramsMap.add("connectParam",connectParam);
        paramsMap.add("description","ext platform description");

        MvcResult mvcResult = mockMvc.perform(post("/platform/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        System.out.println(JSONUtils.toJsonString(result));

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }


    @Test
    public void testExtlist() throws Exception {


        Map map = new HashMap<String,Object>();


        map.put("name","task");
        map.put("urlType",0);
        map.put("url","http://127.0.0.1:8080/task.json");

        String connectParam = JSONUtils.toJson(map);


        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("connectParam",connectParam);

        MvcResult mvcResult = mockMvc.perform(get("/platform/extDetail")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);

        System.out.println(JSONUtils.toJsonString(result));

        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }
}
