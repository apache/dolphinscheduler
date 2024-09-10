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
import org.apache.dolphinscheduler.dao.entity.ProjectWorkerGroup;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

public class ProjectWorkerGroupMapperTest extends BaseDaoTest {

    @Autowired
    private ProjectWorkerGroupMapper projectWorkerGroupMapper;

    /**
     * insert
     *
     * @return ProjectWorkerGroup
     */
    private ProjectWorkerGroup insertOne() {
        // insertOne
        ProjectWorkerGroup projectWorkerGroup = new ProjectWorkerGroup();

        projectWorkerGroup.setProjectCode(1L);
        projectWorkerGroup.setWorkerGroup("WorkerGroup1");;
        projectWorkerGroupMapper.insert(projectWorkerGroup);
        return projectWorkerGroup;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate() {
        // insertOne
        ProjectWorkerGroup projectWorkerGroup = insertOne();
        projectWorkerGroup.setCreateTime(new Date());
        // update
        int update = projectWorkerGroupMapper.updateById(projectWorkerGroup);
        Assertions.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete() {
        ProjectWorkerGroup projectWorkerGroup = insertOne();
        int delete = projectWorkerGroupMapper.deleteById(projectWorkerGroup.getId());
        Assertions.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProjectWorkerGroup projectWorkerGroup = insertOne();
        // query
        List<ProjectWorkerGroup> projectUsers = projectWorkerGroupMapper.selectList(null);
        Assertions.assertNotEquals(0, projectUsers.size());
    }

    /**
     * test delete the relation of project and worker group
     */
    @Test
    public void testDeleteProjectWorkerGroupRelation() {

        ProjectWorkerGroup projectWorkerGroup = insertOne();
        int delete = projectWorkerGroupMapper.delete(new QueryWrapper<ProjectWorkerGroup>()
                .lambda()
                .eq(ProjectWorkerGroup::getProjectCode, projectWorkerGroup.getProjectCode())
                .eq(ProjectWorkerGroup::getWorkerGroup, projectWorkerGroup.getWorkerGroup()));

        Assertions.assertTrue(delete >= 1);
    }

    /**
     * test query the relation of project and worker group
     */
    @Test
    public void testQueryProjectWorkerGroupRelation() {
        ProjectWorkerGroup projectWorkerGroup = insertOne();
        projectWorkerGroup = projectWorkerGroupMapper.selectOne(new QueryWrapper<ProjectWorkerGroup>()
                .lambda()
                .eq(ProjectWorkerGroup::getProjectCode, projectWorkerGroup.getProjectCode())
                .eq(ProjectWorkerGroup::getWorkerGroup, projectWorkerGroup.getWorkerGroup()));

        Assertions.assertNotEquals(null, projectWorkerGroup);
    }
}
