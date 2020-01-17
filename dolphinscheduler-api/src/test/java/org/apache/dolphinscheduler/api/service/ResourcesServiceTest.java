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
package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.ApiApplicationServer;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ResourceType;
import org.apache.dolphinscheduler.common.enums.UdfType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
@Transactional
@Rollback(true)
public class ResourcesServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ResourcesServiceTest.class);

    @Autowired
    private ResourcesService resourcesService;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private UdfFuncMapper udfFuncMapper;
    @Autowired
    private UserMapper userMapper;

    @Test
    public void querytResourceList(){
        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> map = resourcesService.queryResourceList(loginUser, ResourceType.FILE);
        Assert.assertEquals(Status.SUCCESS, map.get(Constants.STATUS));
    }

    @Test
    public void testCreateResource(){
        //create user
        User loginUser = createGeneralUser("user1");
        String resourceName = "udf-resource-1.jar";
        String errorResourceName = "udf-resource-1";
        MockMultipartFile udfResource = new MockMultipartFile(resourceName, resourceName, "multipart/form-data", "some content".getBytes());
        Result result = resourcesService.createResource(loginUser, errorResourceName, "", ResourceType.UDF, udfResource);
        Assert.assertEquals(result.getCode().intValue(),Status.RESOURCE_SUFFIX_FORBID_CHANGE.getCode());
        List<Resource> resourceList = resourceMapper.queryResourceList(resourceName, loginUser.getId(), ResourceType.UDF.ordinal());
        Assert.assertTrue(resourceList.size() == 0);

    }

    @Test
    public void testDelete() throws Exception{
        //create user
        User loginUser = createGeneralUser("user1");
        //create resource
        Resource resource = createResource(loginUser,ResourceType.UDF,"udf-resource-1");
        //create UDF function
        UdfFunc udfFunc = createUdfFunc(loginUser, resource);
        //delete resource
        Result result = resourcesService.delete(loginUser, resource.getId());
        Assert.assertEquals(result.getCode().intValue(),Status.UDF_RESOURCE_IS_BOUND.getCode());
    }

    /**
     * create general user
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
     * create resource by user
     * @param user user
     * @return Resource
     */
    private Resource createResource(User user,ResourceType type,String name){
        //insertOne
        Resource resource = new Resource();
        resource.setAlias(String.format("%s-%s",name,user.getUserName()));
        resource.setType(type);
        resource.setUserId(user.getId());
        resourceMapper.insert(resource);
        return resource;
    }

    /**
     * insert one udf
     * @return
     */
    private UdfFunc createUdfFunc(User user, Resource resource){
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setUserId(user.getId());
        udfFunc.setFuncName("dolphin_udf_func");
        udfFunc.setClassName("org.apache.dolphinscheduler.test.mr");
        udfFunc.setType(UdfType.HIVE);
        udfFunc.setResourceId(resource.getId());
        udfFunc.setResourceName(resource.getAlias());
        udfFunc.setCreateTime(new Date());
        udfFunc.setUpdateTime(new Date());
        udfFuncMapper.insert(udfFunc);
        return udfFunc;
    }
}