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

import org.apache.dolphinscheduler.dao.entity.AuditLog;

import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import junit.framework.TestCase;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class AuditLogMapperTest extends TestCase {

    @Autowired
    AuditLogMapper logMapper;

    /**
     * insert
     * @return AuditLog
     */
    private AuditLog insertOne() {
        AuditLog auditLog = new AuditLog();
        auditLog.setUserName("name");
        auditLog.setTime(new Date());
        auditLog.setModule(0);
        auditLog.setOperation(0);
        logMapper.insert(auditLog);
        return auditLog;
    }

    /**
     * test page query
     */
    @Test
    public void testQueryAuditLog() {
        AuditLog auditLog = insertOne();
        Page<AuditLog> page = new Page<>(1, 3);
        int[] moduleType = new int[0];
        int[] operationType = new int[0];

        IPage<AuditLog> logIPage = logMapper.queryAuditLog(page, moduleType, operationType, auditLog.getUserName(), "", "", null, null);
        Assert.assertNotEquals(logIPage.getTotal(), 0);
    }
}