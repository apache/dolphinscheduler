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

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.Queue;
import org.apache.dolphinscheduler.dao.entity.User;

import java.util.Map;

/**
 * queue service
 */
public interface QueueService {

    /**
     * query queue list
     *
     * @param loginUser login user
     * @return queue list
     */
    Result queryList(User loginUser);

    /**
     * query queue list paging
     *
     * @param loginUser login user
     * @param pageNo page number
     * @param searchVal search value
     * @param pageSize page size
     * @return queue list
     */
    Result queryList(User loginUser, String searchVal, Integer pageNo, Integer pageSize);

    /**
     * create queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param queueName queue name
     * @return create result
     */
    Result createQueue(User loginUser, String queue, String queueName);

    /**
     * update queue
     *
     * @param loginUser login user
     * @param queue queue
     * @param id queue id
     * @param queueName queue name
     * @return update result code
     */
    Result updateQueue(User loginUser, int id, String queue, String queueName);

    /**
     * delete queue
     *
     * @param loginUser login user
     * @param id queue id
     * @return delete result code
     * @throws Exception exception
     */
    Map<String, Object> deleteQueueById(User loginUser, int id) throws Exception;

    /**
     * verify queue and queueName
     *
     * @param queue     queue
     * @param queueName queue name
     * @return true if the queue name not exists, otherwise return false
     */
    Result<Object> verifyQueue(String queue, String queueName);

    /**
     * Make sure queue with given name exists, and create the queue if not exists
     *
     * ONLY for python gateway server, and should not use this in web ui function
     *
     * @param queue queue value
     * @param queueName queue name
     * @return Queue object
     */
    Queue createQueueIfNotExists(String queue, String queueName);

}
