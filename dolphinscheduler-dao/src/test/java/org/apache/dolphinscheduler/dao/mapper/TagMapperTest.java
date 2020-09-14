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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.apache.dolphinscheduler.dao.entity.Project;
import org.apache.dolphinscheduler.dao.entity.Tag;
import org.apache.dolphinscheduler.dao.entity.User;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
public class TagMapperTest {

    @Autowired
    TagMapper tagMapper;

    @Autowired
    UserMapper userMapper;

    @Autowired
    ProjectMapper projectMapper;

    /**
     * insert
     * @return Tag
     */
    private Tag insertOne(){
        //insertOne
        Tag tag = new Tag();
        tag.setName("ut tag");
        tag.setUserId(111);
        tag.setProjectId(1010);
        tagMapper.insert(tag);
        return tag;
    }
    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Tag tag = insertOne();
        //update
        int update = tagMapper.updateById(tag);
        Assert.assertEquals(update, 1);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Tag tag = insertOne();
        int delete = tagMapper.deleteById(tag.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Tag tag = insertOne();
        //query
        List<Tag> tags = tagMapper.selectList(null);
        Assert.assertNotEquals(tags .size(), 0);
    }
    @Test
    public void queryByName() {
        User user = new User();
        user.setUserName("ut user");
        userMapper.insert(user);

        Project project = new Project();
        project.setName("ut project");
        project.setUserId(user.getId());
        projectMapper.insert(project);

        Tag tag = insertOne();
        tag.setProjectId(project.getId());
        tag.setUserId(user.getId());
        tagMapper.updateById(tag);
        Tag tag1 = tagMapper.queryByName(tag.getName());
        Assert.assertNotEquals(tag1,null);
    }

    @Test
    public void queryTagListPaging() {
        Tag tag = insertOne();
        Page<Tag> page = new Page(1,3);
        IPage<Tag> tagIPage = tagMapper.queryTagListPaging(page,111,1101,null);
        Assert.assertNotEquals(tagIPage.getTotal(), 0);
    }
}