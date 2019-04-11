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

import cn.escheduler.api.enums.Status;
import cn.escheduler.api.utils.Constants;
import cn.escheduler.api.utils.PageInfo;
import cn.escheduler.dao.mapper.QueueMapper;
import cn.escheduler.dao.model.Queue;
import cn.escheduler.dao.model.User;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * queue service
 */
@Service
public class QueueService extends BaseService {


    @Autowired
    private QueueMapper queueMapper;

    /**
     * query queue list
     *
     * @param loginUser
     * @return
     */
    public Map<String, Object> queryList(User loginUser) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        List<Queue> queueList = queueMapper.queryAllQueue();
        result.put(Constants.DATA_LIST, queueList);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * query queue list paging
     *
     * @param loginUser
     * @param searchVal
     * @param pageNo
     * @param pageSize
     * @return
     */
    public Map<String, Object> queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        Integer count = queueMapper.countQueuePaging(searchVal);

        PageInfo<Queue> pageInfo = new PageInfo<>(pageNo, pageSize);

        List<Queue> queueList = queueMapper.queryQueuePaging(searchVal, pageInfo.getStart(), pageSize);

        pageInfo.setTotalCount(count);
        pageInfo.setLists(queueList);
        result.put(Constants.DATA_LIST, pageInfo);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * create queue
     *
     * @param loginUser
     * @param queue
     * @param queueName
     * @return
     */
    public Map<String, Object> createQueue(User loginUser, String queue, String queueName) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }

        if (checkQueueExists(queue)) {
            putMsg(result, Status.QUEUE_EXIST, queue);
            return result;
        }

        if (checkQueueExists(queueName)) {
            putMsg(result, Status.QUEUE_NAME_EXIST, queueName);
            return result;
        }

        Queue queueObj = new Queue();
        Date now = new Date();

        queueObj.setQueue(queue);
        queueObj.setQueueName(queueName);
        queueObj.setCreateTime(now);
        queueObj.setUpdateTime(now);

        queueMapper.insert(queueObj);
        putMsg(result, Status.SUCCESS);

        return result;
    }

    /**
     * update queue
     *
     * @param loginUser
     * @param id
     * @param queue
     * @param queueName
     * @return
     */
    public Map<String, Object> updateQueue(User loginUser, int id, String queue, String queueName) {
        Map<String, Object> result = new HashMap<>(5);
        if (checkAdmin(loginUser, result)) {
            return result;
        }


        Queue queueObj = queueMapper.queryById(id);
        if (queueObj == null) {
            putMsg(result, Status.QUEUE_NOT_EXIST, id);
            return result;
        }

        //update queue

        if (StringUtils.isNotEmpty(queue)) {
            if (!queue.equals(queueObj.getQueue()) && checkQueueExists(queue)) {
                putMsg(result, Status.QUEUE_EXIST, queue);
                return result;
            }
            queueObj.setQueue(queue);
        }
        if (StringUtils.isNotEmpty(queueName)) {
            queueObj.setQueueName(queueName);
        }
        Date now = new Date();

        queueObj.setUpdateTime(now);

        queueMapper.update(queueObj);
        putMsg(result, Status.SUCCESS);

        return result;
    }


    /**
     * check queue exists
     *
     * @param queue
     * @return
     */
    private boolean checkQueueExists(String queue) {
        return queueMapper.queryByQueue(queue) == null ? false : true;
    }

}
