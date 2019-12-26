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
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.dao.entity.Resource;
import org.apache.dolphinscheduler.dao.entity.UdfFunc;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.ResourceMapper;
import org.apache.dolphinscheduler.dao.mapper.UdfFuncMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class UdfFuncServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UdfFuncServiceTest.class);

    @Autowired
    private UdfFuncService udfFuncService;
    @Autowired
    private ResourceMapper resourceMapper;
    @Autowired
    private UdfFuncMapper udfFuncMapper;


    @Before
    public void setUp() {
        remove();
    }

    @After
    public void after(){
        remove();
    }

    @Test
    public  void testCreateUdfFunction(){

        //if res.upload.startup.type is null, can not create udf
        if ("NONE".equalsIgnoreCase(PropertyUtils.getString(Constants.RES_UPLOAD_STARTUP_TYPE))){
            Result result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String", "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
            logger.info(result.toString());
            Assert.assertEquals(result.getMsg(),Status.HDFS_NOT_STARTUP.getMsg());
        }else {
            //check resource
            Result result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String", "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, Integer.MAX_VALUE);
            logger.info(result.toString());
            Assert.assertEquals(result.getMsg(),Status.RESOURCE_NOT_EXIST.getMsg());
            // add Resource
            addResource();
            result = udfFuncService.createUdfFunction(getLoginUser(), "UdfFuncServiceTest", "org.apache.dolphinscheduler.api.service.UdfFuncServiceTest", "String", "UdfFuncServiceTest", "UdfFuncServiceTest", UdfType.HIVE, getResourceId());
            logger.info(result.toString());
            Assert.assertEquals(result.getMsg(),Status.SUCCESS.getMsg());
            //remove resouce
            removeResource();
        }

    }

    @Test
    public  void testQueryUdfFuncDetail(){

        //add
        add();
        int id = getUdfFuncId();
        Map<String, Object> result = udfFuncService.queryUdfFuncDetail(id);
        logger.info(result.toString());
        Assert.assertEquals(result.get(Constants.STATUS),Status.SUCCESS);
    }

    @Test
    public  void testUpdateUdfFunc(){
        //TODO local has not hdfs ,cant not test
    }

    @Test
    public  void testQueryUdfFuncListPaging(){

        //add
        add();
        Map<String, Object> result = udfFuncService.queryUdfFuncListPaging(getLoginUser(),null,1,10);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testQueryResourceList(){

        //add
        add();
        Map<String, Object> result = udfFuncService.queryResourceList(getLoginUser(),1);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
    }

    @Test
    public  void testDelete(){
        Result result= udfFuncService.delete(122);
        logger.info(result.toString());
        Assert.assertEquals(Status.RESOURCE_NOT_EXIST.getMsg(),result.getMsg());
        // add
        add();
        //delete exist
        result= udfFuncService.delete(getUdfFuncId());
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
    }
    @Test
    public  void testVerifyUdfFuncByName(){

        Result result = udfFuncService.verifyUdfFuncByName("UdfFuncServiceTest");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getMsg(),result.getMsg());
    }

    /**
     * create admin user
     * @return
     */
    private User getLoginUser(){

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * add resource
     */
    private void  addResource(){

        Date now = new Date();
        Resource resource = new Resource("UdfFuncServiceTest","test","desc",99999999, ResourceType.UDF,1024,now,now);
        resourceMapper.insert(resource);
    }
    /**
     * get resourceId
     */
    private int getResourceId(){

        List<Resource> resourceList = resourceMapper.queryResourceList(null,getLoginUser().getId(),-1);
        if (CollectionUtils.isNotEmpty(resourceList)){
            for (Resource resource : resourceList) {
                return  resource.getId();
            }
        }
        return 0;
    }

    /**
     * remove resource
     */
    private void  removeResource(){
        List<Resource> resourceList = resourceMapper.queryResourceExceptUserId(getLoginUser().getId());
        if (CollectionUtils.isNotEmpty(resourceList)){
            for (Resource resource : resourceList) {
               resourceMapper.deleteById(resource.getId());
            }
        }
    }

    /**
     * add udf
     */
    private void add(){
        //addResource();
        UdfFunc udfFunc = new UdfFunc();
        udfFunc.setFuncName("UdfFuncServiceTest");
        udfFunc.setClassName("org.apache.dolphinscheduler.api.service.UdfFuncServiceTest");
        udfFunc.setResourceId(0);
        udfFunc.setResourceName("UdfFuncServiceTest");
        udfFunc.setCreateTime(new Date());
        udfFunc.setDatabase("database");
        udfFunc.setUpdateTime(new Date());
        udfFunc.setType(UdfType.HIVE);
        udfFuncMapper.insert(udfFunc);
    }

    /**
     *  remove  UdfFunc
     */
    private void remove(){

        Map<String,Object> map = new HashMap<>(1);
        map.put("func_name","UdfFuncServiceTest");
        udfFuncMapper.deleteByMap(map);
    }
    /**
     *  get UdfFunc id
     */
    private int getUdfFuncId(){
        List<UdfFunc> udfFuncs = udfFuncMapper.queryUdfByIdStr(null, "UdfFuncServiceTest");
        if (CollectionUtils.isNotEmpty(udfFuncs)){
           return udfFuncs.get(0).getId();
        }
        return 0;
    }
}