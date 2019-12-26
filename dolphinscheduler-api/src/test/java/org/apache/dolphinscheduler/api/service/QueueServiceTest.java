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

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class QueueServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceTest.class);

    @InjectMocks
    private QueueService queueService;
    @Mock
    private QueueMapper queueMapper;
    @Mock
    private UserMapper userMapper;
    private String queueName = "QueueServiceTest";

    @Before
    public void setUp() {
    }


    @After
    public void after(){
    }

    @Test
    public void testQueryList(){

        Mockito.when(queueMapper.selectList(null)).thenReturn(getQueueList());
        Map<String, Object> result = queueService.queryList(getLoginUser());
        logger.info(result.toString());
        List<Queue> queueList  = (List<Queue>) result.get(Constants.DATA_LIST);
        Assert.assertTrue(CollectionUtils.isNotEmpty(queueList));

    }
    @Test
    public void testQueryListPage(){

        IPage<Queue> page = new Page<>(1,10);
        page.setTotal(1L);
        page.setRecords(getQueueList());
        Mockito.when(queueMapper.queryQueuePaging(Mockito.any(Page.class), Mockito.eq(queueName))).thenReturn(page);
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

        Mockito.when(queueMapper.selectById(1)).thenReturn(getQueue());
        Mockito.when(queueMapper.queryAllQueueList("test", null)).thenReturn(getQueueList());
        Mockito.when(queueMapper.queryAllQueueList(null, "test")).thenReturn(getQueueList());
        Mockito.when(userMapper.queryUserListByQueue(queueName)).thenReturn(getUserList());

        // not exist
        Map<String, Object> result = queueService.updateQueue(getLoginUser(),0,"queue",queueName);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUEUE_NOT_EXIST.getCode(),((Status)result.get(Constants.STATUS)).getCode());
        //no need update
        result = queueService.updateQueue(getLoginUser(),1,queueName,queueName);
        logger.info(result.toString());
        Assert.assertEquals(Status.NEED_NOT_UPDATE_QUEUE.getCode(),((Status)result.get(Constants.STATUS)).getCode());
        //queue exist
        result = queueService.updateQueue(getLoginUser(),1,"test",queueName);
        logger.info(result.toString());
        Assert.assertEquals(Status.QUEUE_VALUE_EXIST.getCode(),((Status)result.get(Constants.STATUS)).getCode());
        // queueName exist
        result = queueService.updateQueue(getLoginUser(),1,"test1","test");
        logger.info(result.toString());
        Assert.assertEquals(Status.QUEUE_NAME_EXIST.getCode(),((Status)result.get(Constants.STATUS)).getCode());
        //success
        result = queueService.updateQueue(getLoginUser(),1,"test1","test1");
        logger.info(result.toString());
        Assert.assertEquals(Status.SUCCESS.getCode(),((Status)result.get(Constants.STATUS)).getCode());

    }
    @Test
    public void testVerifyQueue(){

        Mockito.when(queueMapper.queryAllQueueList(queueName, null)).thenReturn(getQueueList());
        Mockito.when(queueMapper.queryAllQueueList(null, queueName)).thenReturn(getQueueList());

        //queue null
        Result result = queueService.verifyQueue(null,queueName);
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode());

        //queueName null
        result = queueService.verifyQueue(queueName,null);
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.REQUEST_PARAMS_NOT_VALID_ERROR.getCode());

        //exist queueName
        result = queueService.verifyQueue(queueName,queueName);
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.QUEUE_NAME_EXIST.getCode());

        //exist queue
        result = queueService.verifyQueue(queueName,"test");
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.QUEUE_VALUE_EXIST.getCode());

        // success
        result = queueService.verifyQueue("test","test");
        logger.info(result.toString());
        Assert.assertEquals(result.getCode().intValue(), Status.SUCCESS.getCode());


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

    private List<User> getUserList(){
        List<User> list = new ArrayList<>();
        list.add(getLoginUser());
        return list;
    }


    /**
     * get queue
     * @return
     */
    private Queue getQueue(){
        Queue queue = new Queue();
        queue.setId(1);
        queue.setQueue(queueName);
        queue.setQueueName(queueName);
        return queue;
    }

    private List<Queue> getQueueList(){
        List<Queue> queueList = new ArrayList<>();
        queueList.add(getQueue());
        return queueList;
    }

}
