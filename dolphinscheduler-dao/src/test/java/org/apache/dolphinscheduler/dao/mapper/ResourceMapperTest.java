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
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.ResourcesUser;
import org.apache.dolphinscheduler.dao.entity.Tenant;
import org.apache.dolphinscheduler.dao.entity.User;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@Rollback(true)
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
        resource.setAlias("ut-resource");
        resource.setFullName("/ut-resource");
        resource.setPid(-1);
        resource.setDirectory(false);
        resource.setType(ResourceType.FILE);
        resource.setUserId(111);
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * create resource by user
     * @param user user
     * @return Resource
     */
    private Resource createResource(User user,boolean isDirectory,ResourceType resourceType,int pid,String alias,String fullName){
        //insertOne
        Resource resource = new Resource();
        resource.setDirectory(isDirectory);
        resource.setType(resourceType);
        resource.setAlias(alias);
        resource.setFullName(fullName);
        resource.setUserId(user.getId());
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * create resource by user
     * @param user user
     * @return Resource
     */
    private Resource createResource(User user){
        //insertOne
        String alias = String.format("ut-resource-%s",user.getUserName());
        String fullName = String.format("/%s",alias);

        Resource resource = createResource(user, false, ResourceType.FILE, -1, alias, fullName);
        return resource;
    }

    /**
     * create user
     * @return User
     */
    private User createGeneralUser(String userName){
        User user = new User();
        user.setUserName(userName);
        user.setUserPassword("1");
        user.setEmail("xx@123.com");
        user.setUserType(UserType.GENERAL_USER);
        user.setCreateTime(new Date());
        user.setTenantId(1);
        user.setUpdateTime(new Date());
        userMapper.insert(user);
        return user;
    }

    /**
     * create resource user
     * @return ResourcesUser
     */
    private ResourcesUser createResourcesUser(Resource resource,User user){
        //insertOne
        ResourcesUser resourcesUser = new ResourcesUser();
        resourcesUser.setCreateTime(new Date());
        resourcesUser.setUpdateTime(new Date());
        resourcesUser.setUserId(user.getId());
        resourcesUser.setResourcesId(resource.getId());
        resourcesUser.setPerm(7);
        resourceUserMapper.insert(resourcesUser);
        return resourcesUser;
    }

    @Test
    public void testInsert(){
        Resource resource = insertOne();
        assertNotNull(resource.getId());
        assertThat(resource.getId(),greaterThan(0));
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
        Assert.assertEquals(1, update);
    }

    /**
     * test delete
     */
    @Test
    public void testDelete(){
        Resource resourceMap = insertOne();
        int delete = resourceMapper.deleteById(resourceMap.getId());
        Assert.assertEquals(1, delete);
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
                0,
                -1,
                resource.getType().ordinal(),
                ""
        );
        IPage<Resource> resourceIPage1 = resourceMapper.queryResourcePaging(
                page,
                1110,
                -1,
                resource.getType().ordinal(),
                ""
        );
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
        resourcesUser.setPerm(Constants.AUTHORIZE_WRITABLE_PERM);
        resourceUserMapper.insert(resourcesUser);

        List<Resource> resources1 = resourceMapper.queryAuthorizedResourceList(1110);

        Assert.assertEquals(0, resources.size());
        Assert.assertNotEquals(0, resources1.size());

    }

    /**
     * test authed resource list
     */
    @Test
    public void testQueryAuthorizedResourceList() {
        Resource resource = insertOne();

        List<Resource> resources = resourceMapper.queryAuthorizedResourceList(resource.getUserId());

        resourceMapper.deleteById(resource.getId());
        Assert.assertEquals(0, resources.size());
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
                resource.getFullName(),ResourceType.FILE.ordinal()
        );


        Assert.assertEquals("ut tenant code for resource", resource1);

    }

    @Test
    public void testListAuthorizedResource(){
        // create a general user
        User generalUser1 = createGeneralUser("user1");
        User generalUser2 = createGeneralUser("user2");
        // create one resource
        Resource resource = createResource(generalUser2);
        Resource unauthorizedResource = createResource(generalUser1);

        // need download resources
        String[] resNames = new String[]{resource.getFullName(), unauthorizedResource.getFullName()};

        List<Resource> resources = resourceMapper.listAuthorizedResource(generalUser2.getId(), resNames);

        Assert.assertEquals(generalUser2.getId(),resource.getUserId());
        Assert.assertFalse(resources.stream().map(t -> t.getFullName()).collect(toList()).containsAll(Arrays.asList(resNames)));



        // authorize object unauthorizedResource to generalUser
        createResourcesUser(unauthorizedResource,generalUser2);
        List<Resource> authorizedResources = resourceMapper.listAuthorizedResource(generalUser2.getId(), resNames);
        Assert.assertTrue(authorizedResources.stream().map(t -> t.getFullName()).collect(toList()).containsAll(Arrays.asList(resNames)));

    }

    @Test
    public void deleteIdsTest(){
        // create a general user
        User generalUser1 = createGeneralUser("user1");

        Resource resource = createResource(generalUser1);
        Resource resource1 = createResource(generalUser1);

        List<Integer> resourceList = new ArrayList<>();
        resourceList.add(resource.getId());
        resourceList.add(resource1.getId());
        int result = resourceMapper.deleteIds(resourceList.toArray(new Integer[resourceList.size()]));
        Assert.assertEquals(result,2);
    }

    @Test
    public void queryResourceListAuthoredTest(){
        // create a general user
        User generalUser1 = createGeneralUser("user1");
        User generalUser2 = createGeneralUser("user2");
        // create resource
        Resource resource = createResource(generalUser1);
        createResourcesUser(resource, generalUser2);

        List<Resource> resourceList = resourceMapper.queryResourceListAuthored(generalUser2.getId(), ResourceType.FILE.ordinal(), 0);
        Assert.assertNotNull(resourceList);

        resourceList = resourceMapper.queryResourceListAuthored(generalUser2.getId(), ResourceType.FILE.ordinal(), 4);
        Assert.assertFalse(resourceList.contains(resource));
    }

    @Test
    public void batchUpdateResourceTest(){
        // create a general user
        User generalUser1 = createGeneralUser("user1");
        // create resource
        Resource resource = createResource(generalUser1);
        resource.setFullName(String.format("%s-update",resource.getFullName()));
        resource.setUpdateTime(new Date());
        List<Resource> resourceList = new ArrayList<>();
        resourceList.add(resource);
        int result = resourceMapper.batchUpdateResource(resourceList);
        Assert.assertTrue(result>0);
    }
}