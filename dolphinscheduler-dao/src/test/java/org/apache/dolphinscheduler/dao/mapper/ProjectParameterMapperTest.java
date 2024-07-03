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
import org.apache.dolphinscheduler.dao.entity.ProjectParameter;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class ProjectParameterMapperTest extends BaseDaoTest {

    @Autowired
    private ProjectParameterMapper projectParameterMapper;

    private ProjectParameter insertOne(long code, String name, long projectCode) {
        ProjectParameter projectParameter = new ProjectParameter();
        projectParameter.setCode(code);
        projectParameter.setParamName(name);
        projectParameter.setProjectCode(projectCode);
        projectParameter.setParamValue("value");
        projectParameter.setCreateTime(new Date());
        projectParameter.setUpdateTime(new Date());
        projectParameter.setUserId(1);
        projectParameterMapper.insert(projectParameter);
        return projectParameter;
    }

    @Test
    public void testUpdate() {
        ProjectParameter projectParameter = insertOne(1, "name", 1);
        projectParameter.setUpdateTime(new Date());

        int update = projectParameterMapper.updateById(projectParameter);
        Assertions.assertEquals(1, update);
    }

    @Test
    public void testQueryByCode() {
        ProjectParameter projectParameter = insertOne(1, "name", 1);
        ProjectParameter res = projectParameterMapper.queryByCode(projectParameter.getCode());
        Assertions.assertEquals(projectParameter.getCode(), res.getCode());
    }

    @Test
    public void testQueryByCodes() {
        insertOne(1, "name1", 1);
        insertOne(2, "name2", 1);

        List<ProjectParameter> res = projectParameterMapper.queryByCodes(Arrays.asList(1L, 2L));
        Assertions.assertEquals(2, res.size());
    }

    @Test
    public void testQueryByProjectCode() {
        insertOne(1, "name1", 1);
        insertOne(2, "name2", 2);

        List<ProjectParameter> res = projectParameterMapper.queryByProjectCode(1);
        Assertions.assertEquals(1, res.size());
    }

    @Test
    public void testQueryProjectParameterListPaging() {
        insertOne(1, "name1", 1);
        insertOne(2, "name2", 2);

        Page<ProjectParameter> page = new Page(1, 3);
        IPage<ProjectParameter> res = projectParameterMapper.queryProjectParameterListPaging(page, 1, null, null, null);
        Assertions.assertEquals(1, res.getRecords().size());
    }
}
