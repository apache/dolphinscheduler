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

}
