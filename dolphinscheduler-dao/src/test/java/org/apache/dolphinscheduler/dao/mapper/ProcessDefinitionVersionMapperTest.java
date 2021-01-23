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

import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessDefinitionVersion;

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

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProcessDefinitionVersionMapperTest {


    @Autowired
    ProcessDefinitionMapper processDefinitionMapper;

    @Autowired
    ProcessDefinitionVersionMapper processDefinitionVersionMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    QueueMapper queueMapper;

    @Autowired
    TenantMapper tenantMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * insert
     *
     * @return ProcessDefinition
     */
    private ProcessDefinitionVersion insertOne() {
        // insertOne
        ProcessDefinitionVersion processDefinitionVersion
                = new ProcessDefinitionVersion();
        processDefinitionVersion.setProcessDefinitionId(66);
        processDefinitionVersion.setVersion(10);
        processDefinitionVersion.setProcessDefinitionJson(StringUtils.EMPTY);
        processDefinitionVersion.setDescription(StringUtils.EMPTY);
        processDefinitionVersion.setGlobalParams(StringUtils.EMPTY);
        processDefinitionVersion.setCreateTime(new Date());
        processDefinitionVersion.setLocations(StringUtils.EMPTY);
        processDefinitionVersion.setConnects(StringUtils.EMPTY);
        processDefinitionVersion.setTimeout(10);
        processDefinitionVersion.setResourceIds("1,2");
        processDefinitionVersionMapper.insert(processDefinitionVersion);
        return processDefinitionVersion;
    }

    /**
     * insert
     *
     * @return ProcessDefinitionVersion
     */
    private ProcessDefinitionVersion insertTwo() {
        // insertTwo
        ProcessDefinitionVersion processDefinitionVersion
                = new ProcessDefinitionVersion();
        processDefinitionVersion.setProcessDefinitionId(67);
        processDefinitionVersion.setVersion(11);
        processDefinitionVersion.setProcessDefinitionJson(StringUtils.EMPTY);
        processDefinitionVersion.setDescription(StringUtils.EMPTY);
        processDefinitionVersion.setGlobalParams(StringUtils.EMPTY);
        processDefinitionVersion.setCreateTime(new Date());
        processDefinitionVersion.setLocations(StringUtils.EMPTY);
        processDefinitionVersion.setConnects(StringUtils.EMPTY);
        processDefinitionVersion.setTimeout(10);
        processDefinitionVersion.setResourceIds("1,2");
        processDefinitionVersionMapper.insert(processDefinitionVersion);
        return processDefinitionVersion;
    }

    /**
     * test insert
     */
    @Test
    public void testInsert() {
        ProcessDefinitionVersion processDefinitionVersion = insertOne();
        Assert.assertTrue(processDefinitionVersion.getId() > 0);
    }

    /**
     * test query
     */
    @Test
    public void testQueryMaxVersionByProcessDefinitionId() {
        ProcessDefinitionVersion processDefinitionVersion = insertOne();

        Long version = processDefinitionVersionMapper.queryMaxVersionByProcessDefinitionId(
                processDefinitionVersion.getProcessDefinitionId());
        // query
        Assert.assertEquals(10, (long) version);
    }

    @Test
    public void testQueryProcessDefinitionVersionsPaging() {
        insertOne();
        insertTwo();

        Page<ProcessDefinitionVersion> page = new Page<>(1, 3);

        IPage<ProcessDefinitionVersion> processDefinitionVersionIPage =
                processDefinitionVersionMapper.queryProcessDefinitionVersionsPaging(page, 10);

        Assert.assertTrue(processDefinitionVersionIPage.getSize() >= 2);
    }

    @Test
    public void testDeleteByProcessDefinitionIdAndVersion() {
        ProcessDefinitionVersion processDefinitionVersion = insertOne();
        int i = processDefinitionVersionMapper.deleteByProcessDefinitionIdAndVersion(
                processDefinitionVersion.getProcessDefinitionId(), processDefinitionVersion.getVersion());
        Assert.assertEquals(1, i);
    }

    @Test
    public void testQueryByProcessDefinitionIdAndVersion() {
        ProcessDefinitionVersion processDefinitionVersion1 = insertOne();
        ProcessDefinitionVersion processDefinitionVersion3 = processDefinitionVersionMapper.queryByProcessDefinitionIdAndVersion(
                processDefinitionVersion1.getProcessDefinitionId(), 10);

        ProcessDefinitionVersion processDefinitionVersion2 = insertTwo();
        ProcessDefinitionVersion processDefinitionVersion4 = processDefinitionVersionMapper.queryByProcessDefinitionIdAndVersion(
                processDefinitionVersion2.getProcessDefinitionId(), 11);

        Assert.assertEquals(processDefinitionVersion1.getProcessDefinitionId(),
                processDefinitionVersion3.getProcessDefinitionId());
        Assert.assertEquals(processDefinitionVersion2.getProcessDefinitionId(),
                processDefinitionVersion4.getProcessDefinitionId());

    }

}