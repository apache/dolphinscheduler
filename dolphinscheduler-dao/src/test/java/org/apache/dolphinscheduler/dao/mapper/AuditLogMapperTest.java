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

package org.apache.dolphinscheduler.dao.mapper;

import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.AuditLog;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class AuditLogMapperTest extends BaseDaoTest {

    @Autowired
    AuditLogMapper logMapper;

    /**
     * insert
     * @return
     */
    private void insertOne() {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserId(1);
        auditLog.setTime(new Date());
        auditLog.setResourceType(0);
        auditLog.setOperation(0);
        auditLog.setResourceId(0);
        logMapper.insert(auditLog);
    }

    /**
     * test page query
     */
    @Test
    public void testQueryAuditLog() {
        insertOne();
        Page<AuditLog> page = new Page<>(1, 3);
        int[] resourceType = new int[0];
        int[] operationType = new int[0];

        IPage<AuditLog> logIPage = logMapper.queryAuditLog(page, resourceType, operationType, "", null, null);
        Assertions.assertNotEquals(0, logIPage.getTotal());
    }

    @Test
    public void testQueryResourceNameByType() {
        String resourceName = logMapper.queryResourceNameByType("USER", 1);
        Assertions.assertEquals("admin", resourceName);
    }
}
