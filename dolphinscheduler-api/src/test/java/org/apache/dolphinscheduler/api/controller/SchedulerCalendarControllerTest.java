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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.dolphinscheduler.api.dto.CalendarParam;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.DateUtils;
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
public class SchedulerCalendarControllerTest extends AbstractControllerTest{
    private static Logger logger = LoggerFactory.getLogger(SchedulerCalendarControllerTest.class);

    private static Date dateOf(int y, int m, int d) {
        DateTime dt = new DateTime(y, m, d, 0, 0, 0);
        return dt.toDate();
    }

    @Test
    public void testCreateCalendar() throws Exception {

        Map map = new HashMap<String,Object>();
        map.put("startTime","20200401");
        map.put("endTime","20200410");

        List list = new LinkedList<Date>();
        list.add(dateOf(2020, 4, 1));
        list.add(dateOf(2020, 4, 2));
        list.add(dateOf(2020, 4, 3));
        list.add(dateOf(2020, 4, 4));
        list.add(dateOf(2020, 4, 5));
        list.add(dateOf(2020, 4, 6));


        map.put("extTime",list);


        String calendarInfo = JSONUtils.toJson(map);

        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("name","name");
        paramsMap.add("calendarInfo",calendarInfo);
        paramsMap.add("description","calendar description");

        MvcResult mvcResult = mockMvc.perform(post("/calendar/create")
                .header(SESSION_ID, sessionId)
                .params(paramsMap))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());

    }

    @Test
    public void testQueryCalendarlistPaging() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("pageNo","1");
        paramsMap.add("searchVal","calendar");
        paramsMap.add("pageSize","30");

        MvcResult mvcResult = mockMvc.perform(get("/calendar/list-paging")
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
    public void testUpdateCalendar() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","9");
        paramsMap.add("calendarCode","cxc_te");
        paramsMap.add("calendarName","calendar_update_2");
        paramsMap.add("queueId","1");
        paramsMap.add("description","calendar description");

        MvcResult mvcResult = mockMvc.perform(post("/calendar/update")
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
    public void testVerifyCalendarCode() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("calendarCode","cxc_test");

        MvcResult mvcResult = mockMvc.perform(get("/calendar/verify-calendar-code")
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
    public void testQueryCalendarlist() throws Exception {

        MvcResult mvcResult = mockMvc.perform(get("/calendar/list")
                .header(SESSION_ID, sessionId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andReturn();

        Result result = JSONUtils.parseObject(mvcResult.getResponse().getContentAsString(), Result.class);
        Assert.assertEquals(Status.SUCCESS.getCode(),result.getCode().intValue());
        logger.info(mvcResult.getResponse().getContentAsString());
        logger.info(mvcResult.getResponse().getContentAsString());
    }

    @Test
    public void testDeleteCalendarById() throws Exception {
        MultiValueMap<String, String> paramsMap = new LinkedMultiValueMap<>();
        paramsMap.add("id","64");

        MvcResult mvcResult = mockMvc.perform(post("/calendar/delete")
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
    public  void doDetailsInfo() throws Exception {

//
//
//        Map map = new HashMap<String,Object>();
//
//        map.put("startTime",new Date());
//        map.put("endTime",new Date());
//
//        List list = new LinkedList<Date>();
//        list.add(sdf.parse("20200101"));
//        list.add(sdf.parse("20200102"));
//        list.add(sdf.parse("20200103"));
//        list.add(sdf.parse("20200104"));
//
//        map.put("extTime",list);
//
//
//        String calendarInfo = JSONUtils.toJson(map);
//
//        System.out.println(calendarInfo);
//
//        CalendarParam calendarParam = JSONUtils.parseObject(calendarInfo, CalendarParam.class);
//
//
//        System.out.println("CalendarParam  ==> " + calendarParam);




    }






}
