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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DqExecuteResultServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DqRuleServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.enums.dq.RuleType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.User;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * process definition controller test
 */
@RunWith(MockitoJUnitRunner.Silent.class)
public class DataQualityControllerTest {

    private static Logger logger = LoggerFactory.getLogger(DataQualityControllerTest.class);

    @InjectMocks
    private DataQualityController dataQualityController;

    @Mock
    private DqRuleServiceImpl dqRuleService;

    @Mock
    private DqExecuteResultServiceImpl dqExecuteResultService;

    protected User user;

    @Before
    public void before() {
        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.GENERAL_USER);
        loginUser.setUserName("admin");

        user = loginUser;
    }

    @Test
    public void testGetRuleFormCreateJsonById() throws Exception {

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, 1);

        Mockito.when(dqRuleService.getRuleFormCreateJsonById(1)).thenReturn(result);

        Result response = dataQualityController.getRuleFormCreateJsonById(1);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    private List<DqRule> getRuleList() {
        List<DqRule> list = new ArrayList<>();
        DqRule rule = new DqRule();
        rule.setId(1);
        rule.setName("空值检测");
        rule.setType(RuleType.SINGLE_TABLE);
        rule.setUserId(1);
        rule.setUserName("admin");
        rule.setCreateTime(new Date());
        rule.setUpdateTime(new Date());

        list.add(rule);

        return list;
    }

    @Test
    public void testQueryRuleListPaging() throws Exception {

        String searchVal = "";
        int ruleType = 0;
        String start = "2020-01-01 00:00:00";
        String end = "2020-01-02 00:00:00";

        PageInfo<DqRule> pageInfo = new PageInfo<>(1,10);
        pageInfo.setTotalCount(10);
        pageInfo.setLists(getRuleList());

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pageInfo);

        when(dqRuleService.queryRuleListPaging(
                eq(user), eq(searchVal), eq(ruleType), eq(start), eq(end),eq(1), eq(10))).thenReturn(result);

        Result response = dataQualityController.queryRuleListPaging(user, searchVal, ruleType,start,end,1,10);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryRuleList() throws Exception {

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, getRuleList());

        when(dqRuleService.queryAllRuleList()).thenReturn(result);

        Result response = dataQualityController.queryRuleList();
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryResultListPaging() throws Exception {

        String searchVal = "";
        int ruleType = 0;
        String start = "2020-01-01 00:00:00";
        String end = "2020-01-02 00:00:00";

        PageInfo<DqRule> pageInfo = new PageInfo<>(1,10);
        pageInfo.setTotalCount(10);

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, pageInfo);

        when(dqExecuteResultService.queryResultListPaging(
                eq(user), eq(searchVal), any(),eq(ruleType), eq(start), eq(end),eq(1), eq(10))).thenReturn(result);

        Result response = dataQualityController.queryExecuteResultListPaging(user, searchVal, ruleType,0,start,end,1,10);
        Assert.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }
}
