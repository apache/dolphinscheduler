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

package org.apache.dolphinscheduler.api.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.exceptions.ServiceException;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.common.enums.UserType;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_CREATE;
import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.YARN_QUEUE_UPDATE;

/**
 * queue service impl
 */
@Service
public class QueueServiceImpl extends BaseServiceImpl implements QueueService {

    private static final Logger logger = LoggerFactory.getLogger(QueueServiceImpl.class);

    @Autowired
    private QueueMapper queueMapper;

    @Autowired
    private UserMapper userMapper;

    /**
     * Valid both queue and queueName when we want to create or update queue object
     *
     * @param queue queue value
     * @param queueName queue name
     */
    private void queueValid(String queue, String queueName) throws ServiceException {
        if (StringUtils.isEmpty(queue)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
        } else if (StringUtils.isEmpty(queueName)) {
            throw new ServiceException(Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
        } else if (checkQueueExist(queue)) {
            throw new ServiceException(Status.QUEUE_VALUE_EXIST, queue);
        } else if (checkQueueNameExist(queueName)) {
            throw new ServiceException(Status.QUEUE_NAME_EXIST, queueName);
        }
    }

    /**
     * Insert one single new Queue record to database
     *
     * @param queue queue value
     * @param queueName queue name
     * @return Queue
     */
    private Queue createObjToDB(String queue, String queueName) {
        Queue queueObj = new Queue();
        Date now = new Date();

        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setCreateTime(now);
        queueObj.setUpdateTime(now);
        // save
        queueMapper.insert(queueObj);
        return queueObj;
    }

    /**
     * query queue list
     *
     * @param loginUser login user
     * @return queue list
     */
    @Override
    public Map<String, Object> queryList(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE, loginUser.getId(), logger);
        if (loginUser.getUserType().equals(UserType.GENERAL_USER)) {
            ids = ids.isEmpty() ? new HashSet<>() : ids;
            ids.add(Constants.DEFAULT_QUEUE_ID);
        }
        List<Queue> queueList = queueMapper.selectBatchIds(ids);
        result.put(Constants.DATA_LIST, queueList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query queue list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return queue list
     */
    @Override
    public Result queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Result result = new Result();
        PageInfo<Queue> pageInfo = new PageInfo<>(pageNo, pageSize);
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE, loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.setData(pageInfo);
            putMsg(result, Status.SUCCESS);
            return result;
        }
        Page<Queue> page = new Page<>(pageNo, pageSize);
        IPage<Queue> queueList = queueMapper.queryQueuePaging(page, new ArrayList<>(ids), searchVal);
        Integer count = (int) queueList.getTotal();
        pageInfo.setTotal(count);
        pageInfo.setTotalList(queueList.getRecords());
        result.setData(pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * create queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param queueName queue name
     * @return create result
     */
    @Override
    @Transactional
    public Map<String, Object> createQueue(User loginUser, String queue, String queueName) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.QUEUE,YARN_QUEUE_CREATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }
        queueValid(queue, queueName);

        Queue newQueue = createObjToDB(queue, queueName);
        result.put(Constants.DATA_LIST, newQueue);
        putMsg(result, Status.SUCCESS);
        permissionPostHandle(AuthorizationType.QUEUE, loginUser.getId(), Collections.singletonList(newQueue.getId()), logger);
        return result;
    }

    /**
     * update queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param id queue id
     * @param queueName queue name
     * @return update result code
     */
    @Override
    public Map<String, Object> updateQueue(User loginUser, int id, String queue, String queueName) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,new Object[]{id}, AuthorizationType.QUEUE,YARN_QUEUE_UPDATE)) {
            throw new ServiceException(Status.USER_NO_OPERATION_PERM);
        }

        queueValid(queue, queueName);

        Queue queueObj = queueMapper.selectById(id);
        if (Objects.isNull(queueObj)) {
            throw new ServiceException(Status.QUEUE_NOT_EXIST, queue);
        }

        // whether queue value or queueName is changed
        if (queue.equals(queueObj.getQueue()) && queueName.equals(queueObj.getQueueName())) {
            throw new ServiceException(Status.NEED_NOT_UPDATE_QUEUE);
        }

        // check old queue using by any user
        if (checkIfQueueIsInUsing(queueObj.getQueueName(), queueName)) {
            //update user related old queue
            Integer relatedUserNums = userMapper.updateUserQueue(queueObj.getQueueName(), queueName);
            logger.info("old queue have related {} user, exec update user success.", relatedUserNums);
        }

        // update queue
        Date now = new Date();
        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setUpdateTime(now);

        queueMapper.updateById(queueObj);

        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * verify queue and queueName
     *
     * @param queue queue
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    @Override
    public Result<Object> verifyQueue(String queue, String queueName) {
        Result<Object> result = new Result<>();
        queueValid(queue, queueName);

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * check queue exist
     * if exists return true，not exists return false
     * check queue exist
     *
     * @param queue queue
     * @return true if the queue not exists, otherwise return false
     */
    private boolean checkQueueExist(String queue) {
        return queueMapper.existQueue(queue, null) == Boolean.TRUE;
    }

    /**
     * check queue name exist
     * if exists return true，not exists return false
     *
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    private boolean checkQueueNameExist(String queueName) {
        return queueMapper.existQueue(null, queueName) == Boolean.TRUE;
    }

    /**
     * check old queue name using by any user
     * if need to update user
     *
     * @param oldQueue old queue name
     * @param newQueue new queue name
     * @return true if need to update user
     */
    private boolean checkIfQueueIsInUsing(String oldQueue, String newQueue) {
        return !oldQueue.equals(newQueue) && userMapper.existUser(oldQueue) == Boolean.TRUE;
    }

    /**
     * Make sure queue with given name exists, and create the queue if not exists
     *
     * ONLY for python gateway server, and should not use this in web ui function
     *
     * @param queue queue value
     * @param queueName queue name
     * @return Queue object
     */
    @Override
    public Queue createQueueIfNotExists(String queue, String queueName) {
        queueValid(queue, queueName);
        Queue existsQueue = queueMapper.queryQueueName(queue, queueName);
        if (Objects.isNull(existsQueue)) {
            return createObjToDB(queue, queueName);
        }
        return existsQueue;
    }

}
