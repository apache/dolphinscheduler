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

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.QueueService;
import org.apache.dolphinscheduler.api.utils.PageInfo;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.AuthorizationType;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.dao.mapper.QueueMapper;
import org.apache.dolphinscheduler.dao.mapper.UserMapper;

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import static org.apache.dolphinscheduler.api.constants.ApiFuncIdentificationConstant.*;

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
     * query queue list
     *
     * @param loginUser login user
     * @return queue list
     */
    @Override
    public Map<String, Object> queryList(User loginUser) {
        Map<String, Object> result = new HashMap<>();
        Set<Integer> ids = resourcePermissionCheckService.userOwnedResourceIdsAcquisition(AuthorizationType.QUEUE, loginUser.getId(), logger);
        if (ids.isEmpty()) {
            result.put(Constants.DATA_LIST, Collections.emptyList());
            putMsg(result, Status.SUCCESS);
            return result;
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
        IPage<Queue> queueList = queueMapper.queryQueuePaging(page, searchVal);
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
    public Map<String, Object> createQueue(User loginUser, String queue, String queueName) {
        Map<String, Object> result = new HashMap<>();
        if (!canOperatorPermissions(loginUser,null, AuthorizationType.QUEUE,YARN_QUEUE_CREATE)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (StringUtils.isEmpty(queue)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
            return result;
        }

        if (StringUtils.isEmpty(queueName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
            return result;
        }

        if (checkQueueNameExist(queueName)) {
            putMsg(result, Status.QUEUE_NAME_EXIST, queueName);
            return result;
        }

        if (checkQueueExist(queue)) {
            putMsg(result, Status.QUEUE_VALUE_EXIST, queue);
            return result;
        }

        Queue queueObj = new Queue();
        Date now = new Date();

        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setCreateTime(now);
        queueObj.setUpdateTime(now);

        queueMapper.insert(queueObj);
        result.put(Constants.DATA_LIST, queueObj);
        putMsg(result, Status.SUCCESS);

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
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        if (StringUtils.isEmpty(queue)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
            return result;
        }

        if (StringUtils.isEmpty(queueName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
            return result;
        }

        Queue queueObj = queueMapper.selectById(id);
        if (queueObj == null) {
            putMsg(result, Status.QUEUE_NOT_EXIST, id);
            return result;
        }

        // whether queue value or queueName is changed
        if (queue.equals(queueObj.getQueue()) && queueName.equals(queueObj.getQueueName())) {
            putMsg(result, Status.NEED_NOT_UPDATE_QUEUE);
            return result;
        }

        // check queue name is exist
        if (!queueName.equals(queueObj.getQueueName())
                && checkQueueNameExist(queueName)) {
            putMsg(result, Status.QUEUE_NAME_EXIST, queueName);
            return result;
        }

        // check queue value is exist
        if (!queue.equals(queueObj.getQueue()) && checkQueueExist(queue)) {
            putMsg(result, Status.QUEUE_VALUE_EXIST, queue);
            return result;
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

        if (StringUtils.isEmpty(queue)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE);
            return result;
        }

        if (StringUtils.isEmpty(queueName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
            return result;
        }

        if (checkQueueNameExist(queueName)) {
            putMsg(result, Status.QUEUE_NAME_EXIST, queueName);
            return result;
        }

        if (checkQueueExist(queue)) {
            putMsg(result, Status.QUEUE_VALUE_EXIST, queue);
            return result;
        }

        putMsg(result, Status.SUCCESS);
        return result;
    }

    /**
     * query queue by queueName
     *
     * @param queueName queue name
     * @return queue object for provide queue name
     */
    @Override
    public Map<String, Object> queryQueueName(String queueName) {
        Map<String, Object> result = new HashMap<>();

        if (StringUtils.isEmpty(queueName)) {
            putMsg(result, Status.REQUEST_PARAMS_NOT_VALID_ERROR, Constants.QUEUE_NAME);
            return result;
        }

        if (!checkQueueNameExist(queueName)) {
            putMsg(result, Status.QUEUE_NOT_EXIST, queueName);
            return result;
        }

        List<Queue> queueList = queueMapper.queryQueueName(queueName);
        result.put(Constants.DATA_LIST, queueList);
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

}
