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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.impl.AuditServiceImpl;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.dao.entity.AuditLog;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.AuditLogMapper;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * audit service test
 */
@ExtendWith(MockitoExtension.class)
public class AuditServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(AuditServiceTest.class);

    @InjectMocks
    private AuditServiceImpl auditService;

    @Mock
    private AuditLogMapper auditLogMapper;

    @Test
    public void testQueryLogListPaging() {
        Date start = DateUtils.stringToDate("2020-11-01 00:00:00");
        Date end = DateUtils.stringToDate("2020-11-02 00:00:00");

        IPage<AuditLog> page = new Page<>(1, 10);
        page.setRecords(getLists());
        page.setTotal(1L);
        when(auditLogMapper.queryAuditLog(Mockito.any(Page.class), Mockito.any(), Mockito.any(),
                Mockito.eq(""), eq(start), eq(end)))
                        .thenReturn(page);
        Result result = auditService.queryLogListPaging(new User(), null, null, "2020-11-01 00:00:00",
                "2020-11-02 00:00:00", "", 1, 10);
        logger.info(result.toString());
        Assertions.assertEquals(Status.SUCCESS.getCode(), (int) result.getCode());
    }

    private List<AuditLog> getLists() {
        List<AuditLog> list = new ArrayList<>();
        list.add(getAuditLog());
        return list;
    }

    private AuditLog getAuditLog() {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserName("testName");
        auditLog.setOperation(0);
        auditLog.setResourceType(0);
        return auditLog;
    }
}
