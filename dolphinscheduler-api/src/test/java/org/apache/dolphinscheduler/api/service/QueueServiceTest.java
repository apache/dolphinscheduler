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
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ApiApplicationServer.class)
public class QueueServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceTest.class);

    @Autowired
    private QueueService queueService;
    @Autowired
    private QueueMapper queueMapper;

    private String queueName = "QueueServiceTest";

    @Before
    public void setUp() {
        remove();
    }


    @After
    public void after(){
        remove();
    }

    @Test
    public void testQueryList(){

        add();
        Map<String, Object> result = queueService.queryList(getLoginUser());
        logger.info(result.toString());
        List<Queue> queueList  = (List<Queue>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(queueList));

    }
    @Test
    public void testQueryListPage(){

        add();
        Map<String, Object> result = queueService.queryList(getLoginUser(),queueName,1,10);
        logger.info(result.toString());
        PageInfo<Queue>  pageInfo = (PageInfo<Queue>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(pageInfo.getLists()));
    }
    @Test
    public void testCreateQueue(){

        // queue is null
        Map<String, Object> result = queueService.createQueue(getLoginUser(),null,queueName);
        logger.info(result.toString());
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR,result.get(Constants.STATUS));
        // queueName is null
        result = queueService.createQueue(getLoginUser(),queueName,null);
        logger.info(result.toString());
        Assert.assertEquals(Status.REQUEST_PARAMS_NOT_VALID_ERROR,result.get(Constants.STATUS));
        // correct
        result = queueService.createQueue(getLoginUser(),queueName,queueName);
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));

    }
    @Test
    public void testUpdateQueue(){
        add();
        Queue queue = getQueue();
        Map<String, Object> result = queueService.updateQueue(getLoginUser(),queue.getId(),"queue",queueName);
        Assert.assertEquals(Status.SUCCESS,result.get(Constants.STATUS));
        //get update queue ,check update
        queue =queueMapper.selectById(queue.getId());
        Assert.assertEquals("queue",queue.getQueue());
    }
    @Test
    public void testVerifyQueue(){
        //not exist
        Result result = queueService.verifyQueue(queueName,queueName);
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.SUCCESS.getCode());

        add();
        //exist queue
        result = queueService.verifyQueue(queueName,queueName);
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.QUEUE_NAME_EXIST.getCode());

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
     * remove queue
     */
    private void remove(){
        Map<String,Object> map = new HashMap<>(1);
        map.put("queue_name",queueName);
        queueMapper.deleteByMap(map);
    }

    /**
     * get queue
     * @return
     */
    private Queue getQueue(){
        List<Queue> queueList = queueMapper.queryAllQueueList(queueName, queueName);
        if (CollectionUtils.isNotEmpty(queueList)){
            return queueList.get(0);
        }
        return new Queue();
    }

    /**
     * create queue
     */
    private void add(){
        queueService.createQueue(getLoginUser(),queueName,queueName);
    }


}
