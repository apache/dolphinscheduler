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

package org.apache.dolphinscheduler.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.DqExecuteResultServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.DqExecuteResult;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.DqExecuteResultMapper;
import org.apache.dolphinscheduler.plugin.task.api.enums.dp.DqTaskState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SpringBootTest(classes = ApiApplicationServer.class)
public class DqExecuteResultServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(DqExecuteResultServiceTest.class);
    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);

    @InjectMocks
    private DqExecuteResultServiceImpl dqExecuteResultService;

    @Mock
    DqExecuteResultMapper dqExecuteResultMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    @Test
    public void testQueryResultListPaging() {

        String searchVal = "";
        int ruleType = 0;
        Date start = DateUtils.stringToDate("2020-01-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-01-02 00:00:00");

        User loginUser = new User();
        loginUser.setId(1);
        loginUser.setUserType(UserType.ADMIN_USER);
        Mockito.when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.DATA_QUALITY, null,
                loginUser.getId(), null, baseServiceLogger)).thenReturn(true);
        Mockito.when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.DATA_QUALITY, null, 0,
                baseServiceLogger)).thenReturn(true);
        Page<DqExecuteResult> page = new Page<>(1, 10);
        page.setTotal(1);
        page.setRecords(getExecuteResultList());
        when(dqExecuteResultMapper.queryResultListPaging(
                any(IPage.class), eq(""), eq(loginUser), any(), eq(ruleType), eq(start), eq(end))).thenReturn(page);

        Result result = dqExecuteResultService.queryResultListPaging(
                loginUser, searchVal, 1, 0, "2020-01-01 00:00:00", "2020-01-02 00:00:00", 1, 10);
        Assertions.assertEquals(Integer.valueOf(Status.SUCCESS.getCode()), result.getCode());
    }

    public List<DqExecuteResult> getExecuteResultList() {

        List<DqExecuteResult> list = new ArrayList<>();
        DqExecuteResult dqExecuteResult = new DqExecuteResult();
        dqExecuteResult.setId(1);
        dqExecuteResult.setState(DqTaskState.FAILURE.getCode());
        list.add(dqExecuteResult);

        return list;
    }
}
