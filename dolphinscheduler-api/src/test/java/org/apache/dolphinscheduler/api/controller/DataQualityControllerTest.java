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

import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.DqExecuteResultServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DqRuleServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.DqRule;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.RuleType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * process definition controller test
 */
@ExtendWith(MockitoExtension.class)
public class DataQualityControllerTest {

    @InjectMocks
    private DataQualityController dataQualityController;

    @Mock
    private DqRuleServiceImpl dqRuleService;

    @Mock
    private DqExecuteResultServiceImpl dqExecuteResultService;

    protected User user;

    @BeforeEach
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
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    private void putMsg(Map<String, Object> result, Status status, Object... statusParams) {
        result.put(Constants.STATUS, status);
        if (statusParams != null && statusParams.length > 0) {
            result.put(Constants.MSG, MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.put(Constants.MSG, status.getMsg());
        }
    }

    public void putMsg(Result result, Status status, Object... statusParams) {
        result.setCode(status.getCode());
        if (statusParams != null && statusParams.length > 0) {
            result.setMsg(MessageFormat.format(status.getMsg(), statusParams));
        } else {
            result.setMsg(status.getMsg());
        }
    }

    private List<DqRule> getRuleList() {
        List<DqRule> list = new ArrayList<>();
        DqRule rule = new DqRule();
        rule.setId(1);
        rule.setName("空值检测");
        rule.setType(RuleType.SINGLE_TABLE.getCode());
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

        PageInfo<DqRule> pageInfo = new PageInfo<>(1, 10);
        pageInfo.setTotal(10);
        pageInfo.setTotalList(getRuleList());

        Result result = new Result();
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        when(dqRuleService.queryRuleListPaging(
                user, searchVal, ruleType, start, end, 1, 10)).thenReturn(result);

        Result response = dataQualityController.queryRuleListPaging(user, searchVal, ruleType, start, end, 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryRuleList() throws Exception {

        Map<String, Object> result = new HashMap<>();
        putMsg(result, Status.SUCCESS);
        result.put(Constants.DATA_LIST, getRuleList());

        when(dqRuleService.queryAllRuleList()).thenReturn(result);

        Result response = dataQualityController.queryRuleList();
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }

    @Test
    public void testQueryResultListPaging() throws Exception {

        String searchVal = "";
        int ruleType = 0;
        String start = "2020-01-01 00:00:00";
        String end = "2020-01-02 00:00:00";

        PageInfo<DqRule> pageInfo = new PageInfo<>(1, 10);
        pageInfo.setTotal(10);

        Result result = new Result();
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        when(dqExecuteResultService.queryResultListPaging(
                user, searchVal, 0, ruleType, start, end, 1, 10)).thenReturn(result);

        Result response =
                dataQualityController.queryExecuteResultListPaging(user, searchVal, ruleType, 0, start, end, 1, 10);
        Assertions.assertEquals(Status.SUCCESS.getCode(), response.getCode().intValue());
    }
}
