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


import org.apache.dolphinscheduler.dao.entity.ProjectUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class ProjectUserMapperTest {


    @Autowired
    ProjectUserMapper projectUserMapper;

    /**
     * insert
     * @return ProjectUser
     */
    private ProjectUser insertOne(){
        //insertOne
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(1010);
        projectUser.setUserId(111);
        projectUserMapper.insert(projectUser);
        return projectUser;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        ProjectUser projectUser = insertOne();
        projectUser.setCreateTime(new Date());
        //update
        int update = projectUserMapper.updateById(projectUser);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        ProjectUser projectUserMap = insertOne();
        int delete = projectUserMapper.deleteById(projectUserMap.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        ProjectUser projectUser = insertOne();
        //query
        List<ProjectUser> projectUsers = projectUserMapper.selectList(null);
        Assert.assertNotEquals(projectUsers.size(), 0);
    }

    /**
     * test delete project relation
     */
    @Test
    public void testDeleteProjectRelation() {


        ProjectUser projectUser = insertOne();
        int delete = projectUserMapper.deleteProjectRelation(projectUser.getProjectId(), projectUser.getUserId());
        assertThat(delete,greaterThanOrEqualTo(1));

    }

    /**
     * test query project relation
     */
    @Test
    public void testQueryProjectRelation() {
        ProjectUser projectUser = insertOne();
        ProjectUser projectUser1 = projectUserMapper.queryProjectRelation(projectUser.getProjectId(), projectUser.getUserId());
        Assert.assertNotEquals(projectUser1, null);

    }
}