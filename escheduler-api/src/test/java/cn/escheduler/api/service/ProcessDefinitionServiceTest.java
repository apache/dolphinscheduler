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
package cn.escheduler.api.service;

import cn.escheduler.api.ApiApplicationServer;
import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.common.enums.UserType;
import cn.escheduler.dao.model.User;
import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class ProcessDefinitionServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ProcessDefinitionServiceTest.class);

    @Autowired
    ProcessDefinitionService processDefinitionService;

    @Test
    public void queryProccessDefinitionList() throws Exception {

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);

        Map<String, Object> map = processDefinitionService.queryProccessDefinitionList(loginUser,"project_test1");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }

    @Test
    public void queryProcessDefinitionListPagingTest() throws Exception {

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> map = processDefinitionService.queryProcessDefinitionListPaging(loginUser, "project_test1", "",1, 5,0);

        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }

    @Test
    public void deleteProcessDefinitionByIdTest() throws Exception {

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> map = processDefinitionService.deleteProcessDefinitionById(loginUser, "li_sql_test", 6);

        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }

    @Test
    public void batchDeleteProcessDefinitionByIds() throws Exception {

        User loginUser = new User();
        loginUser.setId(-1);
        loginUser.setUserType(UserType.GENERAL_USER);
        Map<String, Object> map = processDefinitionService.batchDeleteProcessDefinitionByIds(loginUser, "li_test_1", "2,3");
        Assert.assertEquals(Status.PROJECT_NOT_FOUNT, map.get(Constants.STATUS));
        logger.info(JSON.toJSONString(map));
    }
}