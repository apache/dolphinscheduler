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
import org.apache.dolphinscheduler.dao.entity.ProjectPreference;

import java.util.Date;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class ProjectPreferenceMapperTest extends BaseDaoTest {

    @Autowired
    private ProjectPreferenceMapper projectPreferenceMapper;

    private ProjectPreference insertOne(long code, long projectCode) {
        ProjectPreference projectPreference = new ProjectPreference();
        projectPreference.setCode(code);
        projectPreference.setProjectCode(projectCode);
        projectPreference.setPreferences("{workerGroup:{availableOptions:[],default:1}}");
        projectPreference.setCreateTime(new Date());
        projectPreference.setUpdateTime(new Date());
        projectPreference.setUserId(1);
        projectPreferenceMapper.insert(projectPreference);
        return projectPreference;
    }

    @Test
    public void testUpdate() {
        ProjectPreference projectPreference = insertOne(1, 1);
        projectPreference.setUpdateTime(new Date());

        int update = projectPreferenceMapper.updateById(projectPreference);
        Assertions.assertEquals(1, update);
    }

    @Test
    public void testQueryByProjectCode() {
        long projectCode = 2;
        ProjectPreference expectedProjectPreference = insertOne(2, projectCode);

        ProjectPreference projectPreference = projectPreferenceMapper
                .selectOne(new QueryWrapper<ProjectPreference>().lambda().eq(ProjectPreference::getProjectCode,
                        projectCode));
        Assertions.assertEquals(expectedProjectPreference, projectPreference);
    }

}
