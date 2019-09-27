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
package cn.escheduler.dao.mapper;


import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.dao.entity.ProjectUser;
import cn.escheduler.dao.entity.ProjectUser;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProjectUserMapperTest {


    @Autowired
    ProjectUserMapper projectUserMapper;

    private ProjectUser insertOne(){
        //insertOne
        ProjectUser projectUser = new ProjectUser();
        projectUser.setProjectId(1010);
        projectUser.setUserId(111);
        projectUserMapper.insert(projectUser);
        return projectUser;
    }

    @Test
    public void testUpdate(){
        //insertOne
        ProjectUser projectUser = insertOne();
        projectUser.setCreateTime(new Date());
        //update
        int update = projectUserMapper.updateById(projectUser);
        Assert.assertEquals(update, 1);
        projectUserMapper.deleteById(projectUser.getId());
    }

    @Test
    public void testDelete(){
        ProjectUser projectUserMap = insertOne();
        int delete = projectUserMapper.deleteById(projectUserMap.getId());
        Assert.assertEquals(delete, 1);
    }

    @Test
    public void testQuery() {
        ProjectUser projectUser = insertOne();
        //query
        List<ProjectUser> projectUsers = projectUserMapper.selectList(null);
        Assert.assertNotEquals(projectUsers.size(), 0);
        projectUserMapper.deleteById(projectUser.getId());
    }

    @Test
    public void testDeleteProjectRelation() {


        ProjectUser projectUser = insertOne();
        int delete = projectUserMapper.deleteProjectRelation(projectUser.getProjectId(), projectUser.getUserId());
        Assert.assertEquals(delete, 1);

    }

    @Test
    public void testQueryProjectRelation() {
        ProjectUser projectUser = insertOne();
        ProjectUser projectUser1 = projectUserMapper.queryProjectRelation(projectUser.getProjectId(), projectUser.getUserId());
        Assert.assertNotEquals(projectUser1, null);

        projectUserMapper.deleteById(projectUser.getId());
    }
}