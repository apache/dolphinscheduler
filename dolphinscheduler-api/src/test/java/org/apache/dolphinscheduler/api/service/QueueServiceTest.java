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

import static org.apache.dolphinscheduler.api.AssertionsHelper.assertDoesNotThrow;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_UPDATE;
import static org.mockito.Mockito.when;

import org.apache.dolphinscheduler.api.AssertionsHelper;
import org.apache.dolphinscheduler.api.enums.v2.BaseStatus;
import org.apache.dolphinscheduler.api.enums.v2.QueueStatus;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.permission.ResourcePermissionCheckService;
import org.apache.dolphinscheduler.api.service.impl.BaseServiceImpl;
import org.apache.dolphinscheduler.api.service.impl.QueueServiceImpl;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.collections4.CollectionUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * queue service test
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class QueueServiceTest {

    private static final Logger baseServiceLogger = LoggerFactory.getLogger(BaseServiceImpl.class);
    private static final Logger queueServiceImplLogger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @InjectMocks
    private QueueServiceImpl queueService;

    @Mock
    private QueueMapper queueMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private ResourcePermissionCheckService resourcePermissionCheckService;

    private static final String QUEUE = "queue";
    private static final String QUEUE_NAME = "queueName";
    private static final String EXISTS = "exists";
    private static final String NOT_EXISTS = "not_exists";
    private static final String NOT_EXISTS_FINAL = "not_exists_final";

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void after() {
    }

    @Test
    public void testQueryList() {
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE,
                getLoginUser().getId(), queueServiceImplLogger)).thenReturn(ids);
        when(queueMapper.selectBatchIds(Mockito.anySet())).thenReturn(getQueueList());
        assertDoesNotThrow(() -> queueService.queryList(getLoginUser()));

    }

    @Test
    public void testQueryListPage() {

        IPage<Queue> page = new Page<>(1, 10);
        page.setTotal(1L);
        page.setRecords(getQueueList());
        Set<Integer> ids = new HashSet<>();
        ids.add(1);
        when(resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE,
                getLoginUser().getId(), queueServiceImplLogger)).thenReturn(ids);
        when(queueMapper.queryQueuePaging(Mockito.any(Page.class), Mockito.anyList(), Mockito.eq(QUEUE_NAME)))
                .thenReturn(page);
        PageInfo<Queue> queuePageInfo = queueService.queryList(getLoginUser(), QUEUE_NAME, 1, 10);
        Assertions.assertTrue(CollectionUtils.isNotEmpty(queuePageInfo.getTotalList()));
    }

    @Test
    public void testCreateQueue() {
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.QUEUE,
                getLoginUser().getId(), YARN_QUEUE_CREATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.QUEUE, null, 0,
                baseServiceLogger)).thenReturn(true);

        // queue is null
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.createQueue(getLoginUser(), null, QUEUE_NAME));
        String formatter = MessageFormat.format(BaseStatus.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        // queueName is null
        exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.createQueue(getLoginUser(), QUEUE_NAME, null));
        formatter = MessageFormat.format(BaseStatus.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE_NAME);
        Assertions.assertEquals(formatter, exception.getMessage());

        // correct
        assertDoesNotThrow(() -> queueService.createQueue(getLoginUser(), QUEUE_NAME, QUEUE_NAME));
    }

    @Test
    public void testUpdateQueue() {
        when(queueMapper.selectById(1)).thenReturn(getQUEUE());
        when(queueMapper.existQueue(EXISTS, null)).thenReturn(true);
        when(queueMapper.existQueue(null, EXISTS)).thenReturn(true);
        when(resourcePermissionCheckService.operationPermissionCheck(AuthorizationType.QUEUE,
                getLoginUser().getId(), YARN_QUEUE_UPDATE, baseServiceLogger)).thenReturn(true);
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.QUEUE, new Object[]{0}, 0,
                baseServiceLogger)).thenReturn(true);

        // not exist
        Throwable exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.updateQueue(getLoginUser(), 0, QUEUE, QUEUE_NAME));
        String formatter = MessageFormat.format(QueueStatus.QUEUE_NOT_EXIST.getMsg(), QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        // no need update
        when(resourcePermissionCheckService.resourcePermissionCheck(AuthorizationType.QUEUE, new Object[]{1}, 0,
                baseServiceLogger)).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.updateQueue(getLoginUser(), 1, QUEUE_NAME, QUEUE_NAME));
        Assertions.assertEquals(QueueStatus.NEED_NOT_UPDATE_QUEUE.getMsg(), exception.getMessage());

        // queue exist
        exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.updateQueue(getLoginUser(), 1, EXISTS, QUEUE_NAME));
        formatter = MessageFormat.format(QueueStatus.QUEUE_VALUE_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // queueName exist
        exception = Assertions.assertThrows(ServiceException.class,
                () -> queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, EXISTS));
        formatter = MessageFormat.format(QueueStatus.QUEUE_NAME_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // success
        when(userMapper.existUser(Mockito.anyString())).thenReturn(false);
        assertDoesNotThrow(() -> queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, NOT_EXISTS));

        // success update with same queue name
        when(queueMapper.existQueue(NOT_EXISTS_FINAL, null)).thenReturn(false);
        assertDoesNotThrow(() -> queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS_FINAL, NOT_EXISTS));

        // success update with same queue value
        when(queueMapper.existQueue(null, NOT_EXISTS_FINAL)).thenReturn(false);
        assertDoesNotThrow(() -> queueService.updateQueue(getLoginUser(), 1, NOT_EXISTS, NOT_EXISTS_FINAL));
    }

    @Test
    public void testVerifyQueue() {
        // queue null
        Throwable exception =
                Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(null, QUEUE_NAME));
        String formatter = MessageFormat.format(BaseStatus.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE);
        Assertions.assertEquals(formatter, exception.getMessage());

        // queueName null
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(QUEUE_NAME, null));
        formatter = MessageFormat.format(BaseStatus.REQUEST_PARAMS_NOT_VALID_ERROR.getMsg(), Constants.QUEUE_NAME);
        Assertions.assertEquals(formatter, exception.getMessage());

        // exist queueName
        when(queueMapper.existQueue(EXISTS, null)).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(EXISTS, QUEUE_NAME));
        formatter = MessageFormat.format(QueueStatus.QUEUE_VALUE_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // exist queue
        when(queueMapper.existQueue(null, EXISTS)).thenReturn(true);
        exception = Assertions.assertThrows(ServiceException.class, () -> queueService.verifyQueue(QUEUE, EXISTS));
        formatter = MessageFormat.format(QueueStatus.QUEUE_NAME_EXIST.getMsg(), EXISTS);
        Assertions.assertEquals(formatter, exception.getMessage());

        // success
        AssertionsHelper.assertDoesNotThrow(() -> queueService.verifyQueue(NOT_EXISTS, NOT_EXISTS));
    }

    @Test
    public void testCreateQueueIfNotExists() {
        Queue queue;

        // queue exists
        when(queueMapper.queryQueueName(QUEUE, QUEUE_NAME)).thenReturn(getQUEUE());
        queue = queueService.createQueueIfNotExists(QUEUE, QUEUE_NAME);
        Assertions.assertEquals(getQUEUE(), queue);

        // queue not exists
        when(queueMapper.queryQueueName(QUEUE, QUEUE_NAME)).thenReturn(null);
        queue = queueService.createQueueIfNotExists(QUEUE, QUEUE_NAME);
        Assertions.assertEquals(new Queue(QUEUE_NAME, QUEUE), queue);
    }

    /**
     * create admin user
     */
    private User getLoginUser() {

        User loginUser = new User();
        loginUser.setUserType(UserType.ADMIN_USER);
        loginUser.setId(99999999);
        return loginUser;
    }

    /**
     * get queue
     */
    private Queue getQUEUE() {
        Queue queue = new Queue();
        queue.setId(1);
        queue.setQueue(QUEUE_NAME);
        queue.setQueueName(QUEUE_NAME);
        return queue;
    }

    private List<Queue> getQueueList() {
        List<Queue> queueList = new ArrayList<>();
        queueList.add(getQUEUE());
        return queueList;
    }

}
