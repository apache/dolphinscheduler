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

import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.entity.WorkflowDefinitionLog;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class WorkflowDefinitionLogMapperTest extends BaseDaoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private WorkflowDefinitionLogMapper workflowDefinitionLogMapper;

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private WorkflowDefinitionLog insertOne() {
        // insertOne
        WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setName("def 1");
        processDefinitionLog.setProjectCode(1L);
        processDefinitionLog.setUserId(101);
        processDefinitionLog.setVersion(1);
        processDefinitionLog.setUpdateTime(new Date());
        processDefinitionLog.setCreateTime(new Date());
        workflowDefinitionLogMapper.insert(processDefinitionLog);
        return processDefinitionLog;
    }

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private WorkflowDefinitionLog insertTwo() {
        // insertOne
        WorkflowDefinitionLog processDefinitionLog = new WorkflowDefinitionLog();
        processDefinitionLog.setCode(1L);
        processDefinitionLog.setName("def 2");
        processDefinitionLog.setProjectCode(1L);
        processDefinitionLog.setUserId(101);
        processDefinitionLog.setVersion(2);

        processDefinitionLog.setUpdateTime(new Date());
        processDefinitionLog.setCreateTime(new Date());
        workflowDefinitionLogMapper.insert(processDefinitionLog);
        return processDefinitionLog;
    }

    @Test
    public void testInsert() {
        WorkflowDefinitionLog processDefinitionLog = insertOne();
        Assertions.assertNotEquals(0, processDefinitionLog.getId().intValue());
    }

    @Test
    public void testQueryByDefinitionName() {
        insertOne();
        Project project = new Project();
        project.setCode(1L);
        project.setName("ut project");
        project.setUserId(101);
        project.setCreateTime(new Date());
        projectMapper.insert(project);

        User user = new User();
        user.setUserName("hello");
        user.setUserPassword("pwd");
        user.setUserType(UserType.GENERAL_USER);
        user.setId(101);
        userMapper.insert(user);

        List<WorkflowDefinitionLog> processDefinitionLogs = workflowDefinitionLogMapper
                .queryByDefinitionName(1L, "def 1");
        Assertions.assertEquals(1, processDefinitionLogs.size());

    }

    @Test
    public void testQueryByDefinitionCode() {
        insertOne();

        List<WorkflowDefinitionLog> processDefinitionLogs = workflowDefinitionLogMapper
                .queryByDefinitionCode(1L);
        Assertions.assertNotEquals(0, processDefinitionLogs.size());
    }

    @Test
    public void testQueryByDefinitionCodeAndVersion() {
        insertOne();

        WorkflowDefinitionLog processDefinitionLogs = workflowDefinitionLogMapper
                .queryByDefinitionCodeAndVersion(1L, 1);
        Assertions.assertNotEquals(null, processDefinitionLogs);
    }

    @Test
    public void testQueryMaxVersionForDefinition() {
        insertOne();
        insertTwo();

        Integer version = workflowDefinitionLogMapper.queryMaxVersionForDefinition(1L);
        Assertions.assertEquals(2, version == null ? 1 : version);
    }

    @Test
    public void testQueryWorkflowDefinitionVersionsPaging() {
        insertOne();
        Page<WorkflowDefinitionLog> page = new Page(1, 3);
        IPage<WorkflowDefinitionLog> processDefinitionLogs =
                workflowDefinitionLogMapper.queryWorkflowDefinitionVersionsPaging(page, 1L, 1L);
        Assertions.assertNotEquals(0, processDefinitionLogs.getTotal());
    }

    @Test
    public void testDeleteByWorkflowDefinitionCodeAndVersion() {
        insertOne();
        Page<WorkflowDefinitionLog> page = new Page(1, 3);
        int processDefinitionLogs = workflowDefinitionLogMapper.deleteByWorkflowDefinitionCodeAndVersion(1L, 1);
        Assertions.assertNotEquals(0, processDefinitionLogs);
    }

    @Test
    public void testQueryMaxVersionDefinitionLog() {
        insertOne();
        insertTwo();

        WorkflowDefinitionLog processDefinitionLog2 = workflowDefinitionLogMapper.queryMaxVersionDefinitionLog(1L);
        Assertions.assertEquals(2, processDefinitionLog2.getVersion());
    }

}
