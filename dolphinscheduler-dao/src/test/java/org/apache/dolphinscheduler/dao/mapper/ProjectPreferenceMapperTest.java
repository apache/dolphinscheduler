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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Date;
import org.apache.dolphinscheduler.dao.BaseDaoTest;
import org.apache.dolphinscheduler.dao.entity.ProjectPreference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ProjectPreferenceMapperTest extends BaseDaoTest {

    @Autowired
    private ProjectPreferenceMapper projectPreferenceMapper;

    private ProjectPreference insertOne(long code, String name, long projectCode) {
        ProjectPreference projectPreference = new ProjectPreference();
        projectPreference.setCode(code);
        projectPreference.setProjectCode(projectCode);
        projectPreference.setPreferences("value");
        projectPreference.setCreateTime(new Date());
        projectPreference.setUpdateTime(new Date());
        projectPreference.setUserId(1);
        projectPreferenceMapper.insert(projectPreference);
        return projectPreference;
    }

    @Test
    public void testUpdate() {
        ProjectPreference projectPreference = insertOne(1, "name", 1);
        projectPreference.setUpdateTime(new Date());

        int update = projectPreferenceMapper.updateById(projectPreference);
        Assertions.assertEquals(1, update);
    }


    @Test
    public void testQueryByProjectCode() {
        insertOne(1, "name1", 1);
        insertOne(2, "name2", 2);

        ProjectPreference projectPreference = projectPreferenceMapper
            .selectOne(new QueryWrapper<ProjectPreference>().lambda().eq(ProjectPreference::getProjectCode, 1));
        Assertions.assertEquals(1, projectPreference);
    }

}
