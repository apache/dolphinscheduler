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
import org.apache.dolphinscheduler.dao.entity.ProcessTaskRelationLog;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessTaskRelationLogMapperTest extends BaseDaoTest {

    @Autowired
    private ProcessTaskRelationLogMapper processTaskRelationLogMapper;

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private ProcessTaskRelationLog insertOne() {
        // insertOne
        ProcessTaskRelationLog processTaskRelationLog = new ProcessTaskRelationLog();
        processTaskRelationLog.setName("def 1");
        processTaskRelationLog.setProcessDefinitionVersion(1);
        processTaskRelationLog.setProjectCode(1L);
        processTaskRelationLog.setProcessDefinitionCode(1L);
        processTaskRelationLog.setPostTaskCode(3L);
        processTaskRelationLog.setPreTaskCode(2L);
        processTaskRelationLog.setUpdateTime(new Date());
        processTaskRelationLog.setCreateTime(new Date());
        processTaskRelationLogMapper.insert(processTaskRelationLog);
        return processTaskRelationLog;
    }

    @Test
    public void testQueryByProcessCodeAndVersion() {
        ProcessTaskRelationLog processTaskRelationLog = insertOne();
        List<ProcessTaskRelationLog> processTaskRelationLogs = processTaskRelationLogMapper
                .queryByProcessCodeAndVersion(1L, 1);
        Assertions.assertNotEquals(0, processTaskRelationLogs.size());
    }

}
