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


import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.dao.entity.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
public class ResourceMapperTest {

    @Autowired
    ResourceMapper resourceMapper;

    @Autowired
    ResourceUserMapper resourceUserMapper;

    @Autowired
    TenantMapper tenantMapper;

    @Autowired
    UserMapper userMapper;

    /**
     * insert
     * @return Resource
     */
    private Resource insertOne(){
        //insertOne
        Resource resource = new Resource();
        resource.setAlias("ut resource");
        resource.setType(ResourceType.FILE);
        resource.setUserId(111);
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * test update
     */
    @Test
    public void testUpdate(){
        //insertOne
        Resource resource = insertOne();
        resource.setCreateTime(new Date());
        //update
        int update = resourceMapper.updateById(resource);
        Assert.assertEquals(update, 1);
        resourceMapper.deleteById(resource.getId());
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Resource resourceMap = insertOne();
        int delete = resourceMapper.deleteById(resourceMap.getId());
        Assert.assertEquals(delete, 1);
    }

    /**
     * test query
     */
    @Test
    public void testQuery() {
        Resource resource = insertOne();
        //query
        List<Resource> resources = resourceMapper.selectList(null);
        Assert.assertNotEquals(resources.size(), 0);
        resourceMapper.deleteById(resource.getId());
    }

    /**
     * test query resource list
     */
    @Test
    public void testQueryResourceList() {

        Resource resource = insertOne();

        String alias = "";
        int userId = resource.getUserId();
        int type = resource.getType().ordinal();
        List<Resource> resources = resourceMapper.queryResourceList(
                    alias,
                    userId,
                    type
        );

        Assert.assertNotEquals(resources.size(), 0);
        resourceMapper.deleteById(resource.getId());
    }

    /**
     * test page
     */
    @Test
    public void testQueryResourcePaging() {
        Resource resource = insertOne();
        ResourcesUser resourcesUser = new ResourcesUser();
        resourcesUser.setResourcesId(resource.getId());
        resourcesUser.setUserId(1110);
        resourceUserMapper.insert(resourcesUser);

        Page<Resource> page = new Page(1, 3);

        IPage<Resource> resourceIPage = resourceMapper.queryResourcePaging(
                page,
                resource.getUserId(),
                resource.getType().ordinal(),
                ""
        );
        IPage<Resource> resourceIPage1 = resourceMapper.queryResourcePaging(
                page,
                1110,
                resource.getType().ordinal(),
                ""
        );
        resourceMapper.deleteById(resource.getId());
        resourceUserMapper.deleteById(resourcesUser.getId());
        Assert.assertNotEquals(resourceIPage.getTotal(), 0);
        Assert.assertNotEquals(resourceIPage1.getTotal(), 0);

    }

    /**
     * test authed resource list
     */
    @Test
    public void testQueryResourceListAuthored() {
        Resource resource = insertOne();

        List<Resource> resources = resourceMapper.queryAuthorizedResourceList(resource.getUserId());

        ResourcesUser resourcesUser = new ResourcesUser();

        resourcesUser.setResourcesId(resource.getId());
        resourcesUser.setUserId(1110);
        resourceUserMapper.insert(resourcesUser);

        List<Resource> resources1 = resourceMapper.queryAuthorizedResourceList(1110);

        resourceUserMapper.deleteById(resourcesUser.getId());
        resourceMapper.deleteById(resource.getId());
        Assert.assertEquals(resources.size(), 0);
        Assert.assertNotEquals(resources1.size(), 0);

    }

    /**
     * test authed resource list
     */
    @Test
    public void testQueryAuthorizedResourceList() {
        Resource resource = insertOne();

        List<Resource> resources = resourceMapper.queryAuthorizedResourceList(resource.getUserId());

        resourceMapper.deleteById(resource.getId());
        Assert.assertEquals(resources.size(), 0);
    }

    /**
     * test query resource expect userId
     */
    @Test
    public void testQueryResourceExceptUserId() {
        Resource resource = insertOne();
        List<Resource> resources = resourceMapper.queryResourceExceptUserId(
                11111
        );
        Assert.assertNotEquals(resources.size(), 0);
        resourceMapper.deleteById(resource.getId());
    }

    /**
     * test query tenant code by resource name
     */
    @Test
    public void testQueryTenantCodeByResourceName() {


        Tenant tenant = new Tenant();
        tenant.setTenantName("ut tenant ");
        tenant.setTenantCode("ut tenant code for resource");
        tenantMapper.insert(tenant);

        User user = new User();
        user.setTenantId(tenant.getId());
        user.setUserName("ut user");
        userMapper.insert(user);

        Resource resource = insertOne();
        resource.setUserId(user.getId());
        resourceMapper.updateById(resource);

        String resource1 = resourceMapper.queryTenantCodeByResourceName(
                resource.getAlias()
        );


        Assert.assertEquals(resource1, "ut tenant code for resource");
        resourceMapper.deleteById(resource.getId());

    }
}